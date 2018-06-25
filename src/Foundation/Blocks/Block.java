package Foundation.Blocks;

import Foundation.Exception.NKInterfaceException;
import Foundation.Exception.NKInternalException;
import Foundation.MemoryStorage.Metadata;
import Foundation.MemoryStorage.MetadataAttribute;
import Foundation.MemoryStorage.Tuple;

import java.io.Serializable;
import java.util.Vector;

public class Block implements Serializable {

    /*
     * Here's the storage property of class Blocks
     * fileIdentifier is the corresponding file name of the block
     * index is the index of the block in its storage structure
     * attribute length is the length of the attribute in the block, the unit is byte
     * isFullyOccupied indicates whether the block have extra space for another attribute
     * isDiscardable indicates whether the block is empty and could be discarded
     * firstAvailablePosition is the first empty place of the block, empty list are used
     * capacity is the maximum number of attributes that the block can store
     * storageData is used to store the data of the block
     */
    public static final Integer blockSize = 8165;
    public String fileIdentifier;
    public Integer index;
    public Integer tupleLength;
    public Boolean isFullyOccupied;
    public Boolean isDiscardable;
    public Integer currentSize;
    public Integer capacity;
    public byte[] storageData = new byte[blockSize];
    private Integer firstAvailablePosition;

    public Block(String fileIdentifier, Integer index, Metadata metadata) {
        this.fileIdentifier = fileIdentifier;
        this.index = index;
        this.isFullyOccupied = false;
        this.isDiscardable = true;
        this.tupleLength = metadata.getTupleLength();
        this.firstAvailablePosition = 0;
        this.currentSize = 0;
        this.capacity = blockSize / tupleLength;
        initializeEmptyList();
    }

    /*
     * The following methods are standard block operations
     * getAllTuples returns all tuples stored in the block
     * getAllTupleIndices returns tuple indices in the block, this is useful during deletion
     * removeTupleAt helps to remove item at a certain data item at an index
     * writeTuple automatically write the item to an empty place in the clock
     */
    public Vector<Tuple> getAllTuples(Metadata metadata) {
        Vector<Tuple> allTuples = new Vector<>();
        Vector<Integer> allTupleIndices = getAllTupleIndices();
        for (Integer index : allTupleIndices) {
            allTuples.add(getTupleAt(index, metadata));
        }
        return allTuples;
    }

    public Vector<Integer> getAllTupleIndices() {
        Vector<Integer> allTupleIndices = new Vector<>();
        Vector<Integer> emptyIndices = getAllEmptyIndices();
        for (int index = 0; index < this.capacity; index ++) {
            if (!emptyIndices.contains(index)) {
                allTupleIndices.add(index);
            }
        }
        return allTupleIndices;
    }

    public Tuple getTupleAt(Integer index, Metadata metadata) {
        Vector<String> dataItems = new Vector<>();
        Integer initialOffset = this.tupleLength * index;
        for (int i = 0; i < metadata.numberOfAttributes; i ++) {
            Integer offset = initialOffset + metadata.getTupleOffsetAt(i);
            dataItems.add(getAttributeAt(offset, metadata.getMetadataAttributeAt(i)));
        }
        return new Tuple(dataItems);
    }

    public void removeTupleAt(Integer index) {
        try {
            releaseOccupancy(index);
        } catch (Exception exception) {
            handleInternalException(exception, "removeTupleAt");
        }
    }

    public Integer writeTuple(Tuple tuple, Metadata metadata) throws NKInterfaceException {
        try {
            Integer initialOffset = declareOccupancy() * this.tupleLength;
            for (int i = 0; i < metadata.numberOfAttributes; i ++) {
                MetadataAttribute attribute = metadata.getMetadataAttributeAt(i);
                Integer offset = initialOffset + metadata.getTupleOffsetAt(i);
                String item = tuple.get(i);
                writeAttributeToBlock(item, attribute, offset);
            }
            return initialOffset / this.tupleLength;
        } catch (NKInternalException exception) {
            handleInternalException(exception, "writeTuple");
        }
        return -1;
    }

    /*
     * The following 3 methods are read methods of Blocks
     * These methods read the corresponding data type from position with offset blockOffset
     * blockOffset starts from 0, the unit is byte
     */
    Integer getInteger(Integer blockOffset) {
        Integer integerSize = Integer.SIZE / 8;
        byte[] integerBytes = readFromStorage(blockOffset, integerSize);
        Converter converter = new Converter();
        return converter.convertToInteger(integerBytes);
    }

    Float getFloat(Integer blockOffset) {
        Integer floatSize = Float.SIZE / 8;
        byte[] floatBytes = readFromStorage(blockOffset, floatSize);
        Converter converter = new Converter();
        return converter.convertToFloat(floatBytes);
    }

    String getString(Integer blockOffset, Integer length) {
        byte[] stringBytes = readFromStorage(blockOffset, length);
        Converter converter = new Converter();
        return converter.convertToString(stringBytes);
    }

    /*
     * The following 3 methods are write methods of block
     * These methods write the declared data (newValue) to the position with offset blockOffset
     * blockOffset starts from 0, the unit is byte
     */
    void writeInteger(Integer newValue, Integer blockOffset) {
        Converter converter = new Converter();
        byte[] bytes = converter.convertToBytes(newValue);
        writeToStorage(bytes, blockOffset);
    }

    void writeFloat(Float newValue, Integer blockOffset) {
        Converter converter = new Converter();
        byte[] bytes = converter.convertToBytes(newValue);
        writeToStorage(bytes, blockOffset);
    }

    void writeString(String newValue, Integer blockOffset, Integer length) {
        Converter converter = new Converter();
        byte[] bytes = converter.convertToBytes(newValue, length);
        writeToStorage(bytes, blockOffset);
    }

    /*
     * The following 2 methods are used for finding or dropping an available position
     * for an attribute
     * The index's unit is attribute, not byte
     * When inserting or deleting an attribute from block
     * make sure that you've implemented the following methods first
     */
    private Integer declareOccupancy() throws NKInternalException {
        if (this.isFullyOccupied) {
            throw new NKInternalException("Inserting into a fully occupied block.");
        }
        Integer available = this.firstAvailablePosition;
        Integer nextAvailable = getInteger(firstAvailablePosition * tupleLength);
        this.firstAvailablePosition = nextAvailable;
        this.currentSize ++;
        this.isFullyOccupied = (nextAvailable < 0);
        this.isDiscardable = false;
        return available;
    }

    private void releaseOccupancy(Integer index) throws NKInternalException {
        if (index < 0 || index >= this.capacity) {
            throw new NKInternalException("Wrong index format.");
        }
        this.isFullyOccupied = false;
        this.currentSize --;
        this.isDiscardable = (this.currentSize == 0);
        Integer nextAvailable = this.firstAvailablePosition;
        writeInteger(nextAvailable, index * tupleLength);
        this.firstAvailablePosition = index;
    }

    /*
     * The following are some private supportive methods
     */
    private String getAttributeAt(Integer offset, MetadataAttribute attribute) {
        switch (attribute.dataType) {
            case IntegerType: return String.valueOf(getInteger(offset));
            case FloatType: return String.valueOf(getFloat(offset));
            case StringType: return getString(offset, attribute.length);
        }
        return null;
    }

    private void initializeEmptyList() {
        for (int i = 0; i < this.capacity - 1; i ++) {
            writeInteger(i + 1, i * tupleLength);
        }
        writeInteger(-1, (this.capacity - 1) * tupleLength);
    }

    byte[] readFromStorage(int blockOffset, int length) {
        byte[] loadedBytes = new byte[length];
        System.arraycopy(storageData, blockOffset, loadedBytes, 0, length);
        return loadedBytes;
    }

    void writeToStorage(byte[] bytes, int blockOffset) {
        System.arraycopy(bytes, 0, storageData, blockOffset, bytes.length);
    }

    private void writeAttributeToBlock(String item, MetadataAttribute attribute, Integer offset)
            throws NKInterfaceException {
        try {
            switch (attribute.dataType) {
                case IntegerType: writeInteger(Integer.valueOf(item), offset); break;
                case FloatType: writeFloat(Float.valueOf(item), offset); break;
                case StringType: writeString(item, offset, attribute.length); break;
            }
        } catch (Exception exception) {
            throw new NKInterfaceException(item + " is not the data type expected.");
        }
    }

    private Vector<Integer> getAllEmptyIndices() {
        Vector<Integer> emptyIndices = new Vector<>();
        Integer nextPosition = this.firstAvailablePosition;
        while (!nextPosition.equals(-1)) {
            emptyIndices.add(nextPosition);
            nextPosition = getInteger(this.tupleLength * nextPosition);
        }
        return emptyIndices;
    }

    void handleInternalException(Exception exception, String methodName) {
        System.out.println("Error in " + methodName + " method, class Blocks.");
        exception.printStackTrace();
    }

}
