package Foundation.Blocks;

import Foundation.Exception.NKInternalException;
import Top.NKSql;

import java.io.Serializable;

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
    public final Integer blockSize = 4096;
    public String fileIdentifier;
    public Integer index;
    public Integer attributeLength;
    public Boolean isFullyOccupied;
    public Boolean isDiscardable;
    public Integer firstAvailablePosition;
    public Integer currentSize;
    public Integer capacity;
    public byte[] storageData = new byte[blockSize];

    public Block(String fileIdentifier, Integer attributeLength, Integer index) {
        this.fileIdentifier = fileIdentifier;
        this.attributeLength = attributeLength;
        this.index = index;
        this.isFullyOccupied = false;
        this.isDiscardable = true;
        this.firstAvailablePosition = 0;
        this.currentSize = 0;
        this.capacity = blockSize / attributeLength;
        initializeEmptyList();
    }

    /*
     * The following 3 methods are read methods of Blocks
     * These methods read the corresponding data type from position with offset blockOffset
     * blockOffset starts from 0, the unit is byte
     */
    public Integer getInteger(int blockOffset) {
        Integer integerSize = Integer.SIZE / 8;
        byte[] integerBytes = readFromStorage(blockOffset, integerSize);
        Converter converter = new Converter();
        return converter.convertToInteger(integerBytes);
    }

    public Float getFloat(int blockOffset) {
        Integer floatSize = Float.SIZE / 8;
        byte[] floatBytes = readFromStorage(blockOffset, floatSize);
        Converter converter = new Converter();
        return converter.convertToFloat(floatBytes);
    }

    public String getString(int blockOffset) {
        byte[] stringBytes = readFromStorage(blockOffset, NKSql.maxLengthOfString);
        Converter converter = new Converter();
        return converter.convertToString(stringBytes);
    }

    /*
     * The following 3 methods are write methods of block
     * These methods write the declared data (newValue) to the position with offset blockOffset
     * blockOffset starts from 0, the unit is byte
     */
    public void writeInteger(int newValue, int blockOffset) {
        Converter converter = new Converter();
        byte[] bytes = converter.convertToBytes(newValue);
        writeToStorage(bytes, blockOffset);
    }

    public void writeFloat(Float newValue, int blockOffset) {
        Converter converter = new Converter();
        byte[] bytes = converter.convertToBytes(newValue);
        writeToStorage(bytes, blockOffset);
    }

    public void writeString(String newValue, int blockOffset) {
        Converter converter = new Converter();
        byte[] bytes = converter.convertToBytes(newValue);
        writeToStorage(bytes, blockOffset);
    }

    /*
     * The following 2 methods are used for finding or dropping an available position
     * for an attribute
     * The index's unit is attribute, not byte
     * When inserting or deleting an attribute from block
     * make sure that you've implemented the following methods first
     */
    public Integer decleareOccupancy() throws NKInternalException {
        if (this.isFullyOccupied) {
            throw new NKInternalException("Inserting into a fully occupied block.");
        }
        Integer available = this.firstAvailablePosition;
        Integer nextAvailable = getInteger(firstAvailablePosition * attributeLength);
        this.firstAvailablePosition = nextAvailable;
        this.currentSize ++;
        this.isFullyOccupied = (nextAvailable < 0);
        this.isDiscardable = false;
        return available;
    }

    public void releaseOccupancy(Integer index) throws NKInternalException {
        if (index < 0 || index >= this.capacity) {
            throw new NKInternalException("Wrong index format.");
        }
        this.isFullyOccupied = false;
        this.currentSize --;
        this.isDiscardable = (this.currentSize == 0);
        Integer nextAvailable = this.firstAvailablePosition;
        writeInteger(nextAvailable, index * attributeLength);
        this.firstAvailablePosition = index;
    }

    /*
     * This method is used when writing the block back to the disk
     */
    public byte[] getStorageData() {
        return this.storageData;
    }

    /*
     * The following are some private supportive methods
     */
    private void initializeEmptyList() {
        for (int i = 0; i < this.capacity - 1; i ++) {
            writeInteger(i + 1, i * attributeLength);
        }
        writeInteger(-1, (this.capacity - 1) * attributeLength);
    }

    protected byte[] readFromStorage(int blockOffset, int length) {
        byte[] loadedBytes = new byte[length];
        System.arraycopy(storageData, blockOffset, loadedBytes, 0, length);
        return loadedBytes;
    }

    protected void writeToStorage(byte[] bytes, int blockOffset) {
        System.arraycopy(bytes, 0, storageData, blockOffset, bytes.length);
    }

    protected void handleInternalException(Exception exception, String methodName) {
        System.out.println("Error in " + methodName + " method, class Blocks.");
        exception.printStackTrace();
    }

}
