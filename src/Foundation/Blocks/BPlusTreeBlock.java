package Foundation.Blocks;

import Foundation.Enumeration.DataType;
import Foundation.Exception.NKInternalException;
import Foundation.MemoryStorage.BPlusTreePointer;
import Top.NKSql;

public class BPlusTreeBlock extends Block {

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

    public BPlusTreePointer searchFor(byte[] dataItem) {
        Integer index = searchInsertIndexFor(dataItem);
        return index>= 0 ? null : getPointer(index);
    }

    public Integer searchInsertIndexFor(byte[] dataItem) {
        Converter converter = new Converter();
        Integer pointerIndex = -1;
        switch (this.dataType) {
            case IntegerType:
                Integer integerItem = converter.convertToInteger(dataItem);
                pointerIndex = this.isLeafNode ? searchLeafIntegerFor(integerItem) :
                        searchInternalIntegerFor(integerItem);
                break;
            case FloatType:
                Float floatItem = converter.convertToFloat(dataItem);
                pointerIndex = this.isLeafNode ? searchLeafFloatFor(floatItem) :
                        searchInternalFloatFor(floatItem);
                break;
            case StringType:
                String stringItem = converter.convertToString(dataItem);
                pointerIndex = this.isLeafNode ? searchLeafStringFor(stringItem) :
                        searchInternalStringFor(stringItem);
                break;
        }
        return pointerIndex;
    }

    public void insert(BPlusTreePointer left, byte[] dataItem, BPlusTreePointer right) {
        Integer index = searchInsertIndexFor(dataItem);
        index = index < 0 ? (- index - 1) : index;
        insert(dataItem, left);
        setPointer(index + 1, right);
    }

    public void insert(byte[] dataItem, BPlusTreePointer relatedPointer) {
        Integer index = searchInsertIndexFor(dataItem);
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

    private Integer searchInternalIntegerFor(Integer dataItem) {
        for (int i = 0; i < this.currentSize; i ++) {
            Integer offset = attributeLength * i + 2 * singlePointerSize;
            if (getInteger(offset) > dataItem) {
                return i;
            }
        }
        return currentSize;
    }

    private Integer searchInternalFloatFor(Float dataItem) {
        for (int i = 0; i < this.currentSize; i ++) {
            Integer offset = attributeLength * i + 2 * singlePointerSize;
            if (getFloat(offset) > dataItem) {
                return i;
            }
        }
        return currentSize;
    }

    private Integer searchInternalStringFor(String dataItem) {
        for (int i = 0; i < currentSize; i ++) {
            Integer offset = attributeLength * i + 2 * singlePointerSize;
            if (getString(offset).compareTo(dataItem) > 0) {
                return i;
            }
        }
        return currentSize;
    }

    private Integer searchLeafIntegerFor(Integer dataItem) {
        for (int i = 0; i < this.currentSize; i ++) {
            Integer offset = attributeLength * i + 2 * singlePointerSize;
            if (getInteger(offset).equals(dataItem)) {
                return i;
            } else if (getInteger(offset) > dataItem) {
                return - i - 1;
            }
        }
        return -1;
    }

    private Integer searchLeafFloatFor(Float dataItem) {
        for (int i = 0; i < this.currentSize; i ++ ) {
            Integer offset = attributeLength * i + 2 * singlePointerSize;
            if (getFloat(offset).equals(dataItem)) {
                return i;
            } else if (getFloat(offset) > dataItem) {
                return - i - 1;
            }
        }
        return -1;
    }

    private Integer searchLeafStringFor(String dataItem) {
        for (int i = 0; i < currentSize; i ++) {
            Integer offset = attributeLength * i + 2 * singlePointerSize;
            if (getString(offset).equals(dataItem)) {
                return i;
            } else if (getString(offset).compareTo(dataItem) > 0) {
                return - i - 1;
            }
        }
        return -1;
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
