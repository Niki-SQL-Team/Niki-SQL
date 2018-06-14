package Foundation.Blocks;

import Foundation.Enumeration.DataType;
import Foundation.Exception.NKInternalException;
import Foundation.MemoryStorage.BPlusTreePointer;
import Top.NKSql;

public class BPlusTreeBlock extends Block {

    /*
     * Here's the storage property of class BPlusTreeBlock
     * isLeafNode indicates that whether the node is a leaf node in a B+ tree
     * markerCapacity is the maximum capacity of markers in the node, it's one less than capacity
     * markerLength is the length of the marker, the attributeLength is markerLength + pointer length
     * dataType stores the data type of the node
     * The pointer field consists of two pointers, index and offset, the pointers are stored
     * in front of its corresponding marker
     */
    public Boolean isLeafNode;
    public Integer markerCapacity;
    private Integer markerLength;
    private DataType dataType;
    private static final Integer singlePointerSize = Integer.SIZE / 8;

    public BPlusTreeBlock(String fileIdentifier, DataType dataType, Boolean isLeafNode) {
        super(fileIdentifier, getAttributeLength(dataType));
        this.dataType = dataType;
        this.isLeafNode = isLeafNode;
        this.markerCapacity = this.capacity - 1;
        this.markerLength = attributeLength - 2 * singlePointerSize;
    }

    public BPlusTreePointer getPointer(Integer index) {
        return this.isLeafNode ? getAttributePointer(index) : getInternalPointer(index);
    }

    public BPlusTreePointer getTailPointer() {
        Integer indexOffset = this.attributeLength * this.markerCapacity + 2 * singlePointerSize;
        return getPointerByOffset(indexOffset, -1);
    }

    public void setTailPointer(Integer blockIndex) {
        Integer tailOffset = this.attributeLength * markerCapacity;
        writeInteger(blockIndex, tailOffset);
    }

    public BPlusTreePointer searchFor(byte[] dataItem) {
        Integer index = searchIndexFor(dataItem, !this.isLeafNode);
        return index == null ? null : getPointer(index);
    }

    public Integer searchIndexFor(byte[] dataItem, Boolean isMandatory) {
        Converter converter = new Converter();
        Integer pointerIndex = -1;
        switch (this.dataType) {
            case IntegerType:
                Integer integerItem = converter.convertToInteger(dataItem);
                pointerIndex = searchIntegerFor(integerItem, isMandatory);
                break;
            case FloatType:
                Float floatItem = converter.convertToFloat(dataItem);
                pointerIndex = searchFloatFor(floatItem, isMandatory);
                break;
            case StringType:
                String stringItem = converter.convertToString(dataItem);
                pointerIndex = searchStringFor(stringItem, isMandatory);
                break;
        }
        return pointerIndex;
    }

    public void insert(BPlusTreePointer left, byte[] dataItem, BPlusTreePointer right) {
        Integer index = searchIndexFor(dataItem, true);
        index = index < 0 ? (- index - 1) : index;
        insert(dataItem, left);
        setPointer(index + 1, right);
    }

    public void insert(byte[] dataItem, BPlusTreePointer relatedPointer) {
        Integer index = searchIndexFor(dataItem, true);
        index = index < 0 ? (- index - 1) : index;
        try {
            shiftRight(index);
        } catch (Exception exception) {
            handleInternalException(exception, "insert");
        }
        setPointer(index, relatedPointer);
        Integer attributeOffset = index * attributeLength + 2 * singlePointerSize;
        writeToStorage(dataItem, attributeOffset);
        this.currentSize ++;
    }

    public void remove(byte[] dataItem, Boolean isLeftPointerPreserved) throws NKInternalException {
        Integer index = searchIndexFor(dataItem, false);
        if (index == null) {
            throw new NKInternalException("dataItem not found when intend to delete.");
        }
        if (index < currentSize - 1) {
            shiftAttributeLeft(index + 1);
            if (isLeftPointerPreserved) {
                shiftPointerLeft(index + 1);
            } else if (index < currentSize - 2) {
                shiftPointerLeft(index + 2);
            }
        }
        this.currentSize --;
    }

    public void outputAttributes() {
        for (int i = 0; i < this.currentSize; i ++) {
            Integer offset = attributeLength * i + 2 * singlePointerSize;
            switch (this.dataType) {
                case IntegerType: System.out.println(getInteger(offset)); break;
                case FloatType: System.out.println(getFloat(offset)); break;
                case StringType: System.out.println(getString(offset)); break;
            }
        }
        outputPointerAttributes();
    }

    /*
     * The following are some private supportive methods
     */
    private void setPointer(Integer index, BPlusTreePointer pointer) {
        Integer indexOffset = attributeLength * index;
        Integer offsetOffset = indexOffset + singlePointerSize;
        writeInteger(pointer.blockIndex, indexOffset);
        writeInteger(pointer.blockOffset, offsetOffset);
    }

    private void shiftRight(Integer fromIndex) throws NKInternalException {
        if (this.currentSize.equals(this.markerCapacity)) {
            throw new NKInternalException("Shift on a full block");
        }
        shiftAttributesRight(fromIndex);
        shiftPointerRight(fromIndex);
    }

    private void shiftAttributesRight(Integer fromIndex) {
        for (int i = this.currentSize - 1; i >= fromIndex; i --) {
            Integer fromOffset = attributeLength * i + 2 * singlePointerSize;
            Integer toOffset = fromOffset + attributeLength;
            copyStorage(fromOffset, toOffset, markerLength);
        }
    }

    private void shiftPointerRight(Integer fromIndex) {
        for (int i = this.currentSize; i >= fromIndex; i --) {
            Integer fromOffset = attributeLength * i;
            Integer toOffset = fromOffset + attributeLength;
            copyStorage(fromOffset, toOffset, 2 * singlePointerSize);
        }
    }

    private void shiftAttributeLeft(Integer fromIndex) {
        for (int i = fromIndex; i < this.currentSize; i ++) {
            Integer fromOffset = attributeLength * i + 2 * singlePointerSize;
            Integer toOffset = fromOffset - attributeLength;
            copyStorage(fromOffset, toOffset, markerLength);
        }
    }

    private void shiftPointerLeft(Integer fromIndex) {
        for (int i = fromIndex; i <= this.currentSize; i ++) {
            Integer fromOffset = attributeLength * i;
            Integer toOffset = fromOffset - attributeLength;
            copyStorage(fromOffset, toOffset, 2 * singlePointerSize);
        }
    }

    private void copyStorage(Integer fromOffset, Integer toOffset, Integer forLength) {
        byte[] bytes = readFromStorage(fromOffset, forLength);
        writeToStorage(bytes, toOffset);
    }

    private Integer searchIntegerFor(Integer dataItem, Boolean isMandatoryFound) {
        for (int i = 0; i < this.currentSize; i ++) {
            Integer offset = attributeLength * i + 2 * singlePointerSize;
            Integer content = getInteger(offset);
            if (content.equals(dataItem) || (content >= dataItem && isMandatoryFound)) {
                return i;
            }
        }
        return isMandatoryFound ? currentSize : null;
    }

    private Integer searchFloatFor(Float dataItem, Boolean isMandatoryFound) {
        for (int i = 0; i < this.currentSize; i ++) {
            Integer offset = attributeLength * i + 2 * singlePointerSize;
            Float content = getFloat(offset);
            if (content.equals(dataItem) || (content >= dataItem && isMandatoryFound)) {
                return i;
            }
        }
        return isMandatoryFound ? currentSize : null;
    }

    private Integer searchStringFor(String dataItem, Boolean isMandatoryFound) {
        for (int i = 0; i < currentSize; i ++) {
            Integer offset = attributeLength * i + 2 * singlePointerSize;
            String content = getString(offset);
            if (content.equals(dataItem) || content.compareTo(dataItem) > 0 && isMandatoryFound) {
                return i;
            }
        }
        return isMandatoryFound ? currentSize : null;
    }

    private BPlusTreePointer getAttributePointer(Integer index) {
        Integer indexOffset = this.attributeLength * index;
        Integer offsetOffset = this.attributeLength * index + singlePointerSize;
        return getPointerByOffset(indexOffset, offsetOffset);
    }

    private BPlusTreePointer getInternalPointer(Integer index) {
        Integer indexOffset = this.attributeLength * index;
        return getPointerByOffset(indexOffset, -1);
    }

    private BPlusTreePointer getPointerByOffset(Integer indexOffset, Integer offsetOffset) {
        if (offsetOffset < 0) {
            return new BPlusTreePointer(getInteger(indexOffset));
        } else {
            return new BPlusTreePointer(getInteger(indexOffset), getInteger(offsetOffset));
        }
    }

    private static Integer getAttributeLength(DataType dataType) {
        switch (dataType) {
            case IntegerType: return Integer.SIZE / 8 + 2 * singlePointerSize;
            case FloatType: return Float.SIZE / 8 + 2 * singlePointerSize;
            case StringType: return NKSql.maxLengthOfString + 2 * singlePointerSize;
        }
        return -1;
    }

    private void outputPointerAttributes() {
        for (int i = 0; i <= this.currentSize; i ++) {
            Integer indexOffset = attributeLength * i;
            Integer offsetOffset = indexOffset + singlePointerSize;
            System.out.println(getInteger(indexOffset) + ", " + getInteger(offsetOffset));
        }
    }

}
