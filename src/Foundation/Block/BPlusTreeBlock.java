package Foundation.Block;

import Foundation.Enumeration.DataType;
import Foundation.MemoryStorage.BPlusTreePointer;
import Top.NKSql;

import java.lang.reflect.Type;

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
        switch (this.dataType) {
            case IntegerType:
                Integer integerItem = convertToIntegerFrom(dataItem);
                return this.isLeafNode ? searchLeafIntegerFor(integerItem) :
                        searchInternalIntegerFor(integerItem);
            case FloatType:
                Float floatItem = convertToFloatFrom(dataItem);
                return this.isLeafNode ? searchInternalFloatFor(floatItem) :
                        searchLeafFloatFor(floatItem);
            case StringType:
                String stringItem = new String(dataItem);
                return this.isLeafNode ? searchInternalStringFor(stringItem) :
                        searchLeafStringFor(stringItem);
        }
        return null;
    }

    private BPlusTreePointer searchInternalIntegerFor(Integer dataItem) {
        for (int i = 0; i < this.currentSize; i ++) {
            Integer offset = attributeLength * i + 2 * singlePointerSize;
            if (getInteger(offset) > dataItem) {
                return getPointer(i);
            }
        }
        return getPointer(currentSize);
    }

    private BPlusTreePointer searchInternalFloatFor(Float dataItem) {
        for (int i = 0; i < this.currentSize; i ++) {
            Integer offset = attributeLength * i + 2 * singlePointerSize;
            if (getFloat(offset) > dataItem) {
                return getPointer(i);
            }
        }
        return getPointer(currentSize);
    }

    private BPlusTreePointer searchInternalStringFor(String dataItem) {
        for (int i = 0; i < currentSize; i ++) {
            Integer offset = attributeLength * i + 2 * singlePointerSize;
            if (getString(offset).compareTo(dataItem) > 0) {
                return getPointer(i);
            }
        }
        return getPointer(currentSize);
    }

    private BPlusTreePointer searchLeafIntegerFor(Integer dataItem) {
        for (int i = 0; i < this.currentSize; i ++) {
            Integer offset = attributeLength * i + 2 * singlePointerSize;
            if (getInteger(offset).equals(dataItem)) {
                return getPointer(i);
            }
        }
        return null;
    }

    private BPlusTreePointer searchLeafFloatFor(Float dataItem) {
        for (int i = 0; i < this.currentSize; i ++ ) {
            Integer offset = attributeLength * i + 2 * singlePointerSize;
            if (getFloat(offset).equals(dataItem)) {
                return getPointer(i);
            }
        }
        return null;
    }

    private BPlusTreePointer searchLeafStringFor(String dataItem) {
        for (int i = 0; i < currentSize; i ++) {
            Integer offset = attributeLength * i + 2 * singlePointerSize;
            if (getString(offset).equals(dataItem)) {
                return getPointer(i);
            }
        }
        return null;
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
            case IntegerType: return Integer.SIZE / 8;
            case FloatType: return Float.SIZE / 8;
            case StringType: return NKSql.maxLengthOfString;
        }
        return -1;
    }

}
