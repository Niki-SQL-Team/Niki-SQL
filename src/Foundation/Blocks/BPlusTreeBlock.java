package Foundation.Blocks;

import Foundation.Enumeration.DataType;
import Foundation.Exception.NKInternalException;
import Foundation.MemoryStorage.BPlusTreeMetadata;
import Foundation.MemoryStorage.BPlusTreePointer;
import IndexManager.BPlusTree;

public class BPlusTreeBlock extends Block {

    /*
     * Here's the storage property of class BPlusTreeBlock
     * isLeafNode indicates that whether the node is a leaf node in a B+ tree
     * markerCapacity is the maximum capacity of markers in the node, it's one less than capacity
     * markerLength is the length of the marker, the tupleLength is markerLength + pointer length
     * dataType stores the data type of the node
     * The pointer field consists of two pointers, index and offset, the pointers are stored
     * in front of its corresponding marker
     */
    public Boolean isLeafNode;
    public Integer markerCapacity;
    private Integer markerLength;
    private DataType dataType;
    private static final Integer singlePointerSize = Integer.SIZE / 8;

    public BPlusTreeBlock(String fileIdentifier, DataType dataType, Integer index, Boolean isLeafNode) {
        super(fileIdentifier, index, new BPlusTreeMetadata(dataType));
        this.dataType = dataType;
        if (dataType.equals(DataType.StringType)) {
            System.out.println("String type should specify its length when constructing B+ tree.");
        }
        this.isLeafNode = isLeafNode;
        this.markerCapacity = this.capacity - 1;
        this.markerLength = tupleLength - 2 * singlePointerSize;
        this.isDiscardable = false;
    }

    public BPlusTreeBlock(String fileIdentifier, DataType dataType, Integer length,
                          Integer index, Boolean isLeafNode) {
        super(fileIdentifier, index, new BPlusTreeMetadata(dataType, length));
        this.dataType = dataType;
        if (!dataType.equals(DataType.StringType)) {
            System.out.println("Non-String type don't need to specify its length when constructing B+ tree.");
        }
        this.isLeafNode = isLeafNode;
        this.markerCapacity = this.capacity - 1;
        this.markerLength = tupleLength - 2 * singlePointerSize;
        this.isDiscardable = false;
    }

    public BPlusTreePointer getPointer(Integer index) {
        return this.isLeafNode ? getAttributePointer(index) : getInternalPointer(index);
    }

    public byte[] getAttribute(Integer index) {
        Integer offset = index * this.tupleLength + 2 * singlePointerSize;
        return readFromStorage(offset, this.markerLength);
    }

    /*
     * Tail Pointer is the pointer that is used to point to the sibling of a node in a B+ tree
     * The tail pointer is only a integer, indicating the block index of the sibling
     * The caller of the BPlusTreeBlock is responsible for the maintenance of the Tail Pointer
     */
    public BPlusTreePointer getTailPointer() {
        Integer indexOffset = this.tupleLength * this.markerCapacity + 2 * singlePointerSize;
        return getPointerByOffset(indexOffset, -1);
    }

    public void setTailPointer(Integer blockIndex) {
        Integer tailOffset = this.tupleLength * markerCapacity + 2 * singlePointerSize;
        writeInteger(blockIndex, tailOffset);
    }

    /*
     * The searchFor method is used to search an item in the BPlusTreeBlock
     * The return value is the pointer of the corresponding pointer
     * The method would automatically judge whether the node is a leaf node
     * If it's a leaf node, the method would return the pointer which corresponding dataItem is exactly
     * the search value, and returns null if the item not found
     * If it's not a leaf node, the method would return the pointer that points to the next node
     * The search methods are linear search currently, there might be some improvements
     */
    public BPlusTreePointer searchFor(byte[] dataItem) {
        Integer index = searchIndexFor(dataItem, !this.isLeafNode);
        return index == -1 ? null : getPointer(index);
    }

    /*
     * The method is used for finding the corresponding index of the dataItem
     * The parameter isMandatoryFound control the search condition
     * If isMandatoryFound is true, then the method would search for the dataItem that is exactly the
     * same with the provided dataItem, and returns null if not exist
     * If isMandatoryFound is false, then the method would search for the index of the pointer that
     * can be used to search for the next block in the B+ tree
     */
    public Integer searchIndexFor(byte[] dataItem, Boolean isMandatoryFound) {
        Converter converter = new Converter();
        Integer pointerIndex = -1;
        switch (this.dataType) {
            case IntegerType:
                Integer integerItem = converter.convertToInteger(dataItem);
                pointerIndex = searchIntegerFor(integerItem, isMandatoryFound);
                break;
            case FloatType:
                Float floatItem = converter.convertToFloat(dataItem);
                pointerIndex = searchFloatFor(floatItem, isMandatoryFound);
                break;
            case StringType:
                String stringItem = converter.convertToString(dataItem);
                pointerIndex = searchStringFor(stringItem, isMandatoryFound, this.markerLength);
                break;
        }
        return pointerIndex == null ? -1 : pointerIndex;
    }

    /*
     * The following two insert methods is used to insert data into the BPlusTreeBlock
     * The method would automatically maintain the order of the item in the block
     * The first method is used to insert into a internal node, you would have to provide the data and
     * the left and right pointers of the data
     * The second method is used to insert into a leaf node, you can only provide the data and its
     * corresponding pointer
     */
    public void insert(BPlusTreePointer left, byte[] dataItem, BPlusTreePointer right) {
        Integer index = searchIndexFor(dataItem, true);
        index = index < 0 ? (- index - 1) : index;
        insert(dataItem, left);
        setPointer(index + 1, right);
    }

    public void insert(byte[] dataItem, BPlusTreePointer relatedPointer) {
        Integer index = searchIndexFor(dataItem, true);
        index = index < 0 ? (- index - 1) : index;
        if (index < this.currentSize) {
            Integer initialOffset = index * this.tupleLength;
            copyStorage(initialOffset, initialOffset + this.tupleLength,
                    (currentSize - index) * this.tupleLength);
        }
        setPointer(index, relatedPointer);
        writeToStorage(dataItem, index * tupleLength + 2 * singlePointerSize);
        this.currentSize ++;
    }

    /*
     * The remove method can be used to remove an item which value is exactly the same with dataItem
     * If the item is not found in the block, an exception would be thrown
     * The parameter isLeftPointerPreserved is used to tell the method whether the left pointer or
     * the right pointer of the target dataItem is to be removed
     */
    public void remove(byte[] dataItem, Boolean isLeftPointerPreserved) throws NKInternalException {
        Integer index = searchIndexFor(dataItem, false);
        if (index == null) {
            throw new NKInternalException("dataItem not found when intend to delete.");
        }
        if (isLeftPointerPreserved && index < this.currentSize) {
            Integer initialOffset = index * this.tupleLength;
            copyStorage(initialOffset + this.tupleLength, initialOffset,
                    (currentSize - 1 - index) * this.tupleLength);
        } else {
            Integer initialOffset = index * this.tupleLength;
            Integer pointerLength = 2 * singlePointerSize;
            copyStorage(initialOffset + pointerLength + this.tupleLength,
                    initialOffset + pointerLength,
                    (currentSize - 1 - index) * this.tupleLength);
        }
        this.currentSize --;
    }

    /*
     * This method would split the block into two, in order to make it more convenient in the methods
     * implementation of B Plus tree
     * The return value of the methods is the later half of the block
     */
    public BPlusTreeBlock split(Integer newBlockIndex) {
        Integer splitIndex = this.currentSize / 2 + 1;
        BPlusTreeBlock splitBlock;
        if (this.dataType.equals(DataType.StringType)) {
            splitBlock = new BPlusTreeBlock(fileIdentifier, dataType, markerLength, newBlockIndex, isLeafNode);
        } else {
            splitBlock = new BPlusTreeBlock(fileIdentifier, dataType, newBlockIndex, isLeafNode);
        }
        System.arraycopy(storageData, splitIndex * tupleLength, splitBlock.storageData,
                0, (currentSize - splitIndex + 1) * tupleLength);
        splitBlock.currentSize = this.currentSize - this.currentSize / 2;
        this.currentSize /= 2;
        return splitBlock;
    }

    public void outputAttributes() {
        for (int i = 0; i < this.currentSize; i ++) {
            Integer offset = tupleLength * i + 2 * singlePointerSize;
            switch (this.dataType) {
                case IntegerType: System.out.println(getInteger(offset)); break;
                case FloatType: System.out.println(getFloat(offset)); break;
                case StringType: System.out.println(getString(offset, this.markerLength)); break;
            }
        }
        outputPointerAttributes();
    }

    /*
     * The following are some private supportive methods
     */
    private void setPointer(Integer index, BPlusTreePointer pointer) {
        Integer indexOffset = tupleLength * index;
        Integer offsetOffset = indexOffset + singlePointerSize;
        writeInteger(pointer.blockIndex, indexOffset);
        writeInteger(pointer.blockOffset, offsetOffset);
    }

    private void copyStorage(Integer fromOffset, Integer toOffset, Integer forLength) {
        System.arraycopy(this.storageData, fromOffset, this.storageData, toOffset, forLength);
    }

    private Integer searchIntegerFor(Integer dataItem, Boolean isMandatoryFound) {
        if (this.currentSize == 0) {
            return null;
        }
        Integer startIndex = 0, endIndex = this.currentSize - 1;
        Integer pointerSize = 2 * singlePointerSize;
        Integer startContent = getInteger(startIndex * tupleLength + pointerSize);
        Integer endContent = getInteger(endIndex * tupleLength + pointerSize);
        Integer returnValue = checkForEarlyQuitSearch(startContent, endContent, startIndex, endIndex,
                isMandatoryFound, dataItem);
        if (returnValue == null) {
            return null;
        } else if (returnValue != -1) {
            return returnValue;
        }
        while (endIndex - startIndex != 1) {
            Integer centerIndex = (endIndex + startIndex) / 2;
            Integer centerContent = getInteger(centerIndex * tupleLength + pointerSize);
            if (centerContent > dataItem) {
                endIndex = centerIndex;
            } else if (centerContent < dataItem) {
                startIndex = centerIndex;
            } else {
                return isMandatoryFound ? centerIndex + 1 : centerIndex;
            }
        }
        return isMandatoryFound ? endIndex : null;
    }

    private Integer searchFloatFor(Float dataItem, Boolean isMandatoryFound) {
        if (this.currentSize == 0) {
            return null;
        }
        Integer startIndex = 0, endIndex = this.currentSize - 1;
        Integer pointerSize = 2 * singlePointerSize;
        Float startContent = getFloat(startIndex * tupleLength + pointerSize);
        Float endContent = getFloat(endIndex * tupleLength + pointerSize);
        Integer returnValue = checkForEarlyQuitSearch(startContent, endContent, startIndex, endIndex,
                isMandatoryFound, dataItem);
        if (returnValue == null) {
            return null;
        } else if (returnValue != -1) {
            return returnValue;
        }
        while (endIndex - startIndex != 1) {
            Integer centerIndex = (endIndex + startIndex) / 2;
            Float centerContent = getFloat(centerIndex * tupleLength + pointerSize);
            if (centerContent > dataItem) {
                endIndex = centerIndex;
            } else if (centerContent < dataItem) {
                startIndex = centerIndex;
            } else {
                return isMandatoryFound ? centerIndex + 1 : centerIndex;
            }
        }
        return isMandatoryFound ? endIndex : null;
    }

    private Integer searchStringFor(String dataItem, Boolean isMandatoryFound, Integer length) {
        if (this.currentSize == 0) {
            return null;
        }
        Integer startIndex = 0, endIndex = this.currentSize - 1;
        Integer pointerSize = 2 * singlePointerSize;
        String startContent = getString(startIndex * tupleLength + pointerSize, length);
        String endContent = getString(endIndex * tupleLength + pointerSize, length);
        Integer returnValue = checkForEarlyQuitSearch(startContent, endContent, startIndex, endIndex,
                isMandatoryFound, dataItem);
        if (returnValue == null) {
            return null;
        } else if (returnValue != -1) {
            return returnValue;
        }
        while (endIndex - startIndex != 1) {
            Integer centerIndex = (endIndex + startIndex) / 2;
            String centerContent = getString(centerIndex * tupleLength, pointerSize);
            if (centerContent.compareTo(dataItem) > 0) {
                endIndex = centerIndex;
            } else if (centerContent.compareTo(dataItem) < 0) {
                startIndex = centerIndex;
            } else {
                return isMandatoryFound ? centerIndex + 1: centerIndex;
            }
        }
        return isMandatoryFound ? endIndex : null;
    }

    private <Type extends Comparable<? super Type>>Integer checkForEarlyQuitSearch
            (Type startContent, Type endContent, Integer startIndex, Integer endIndex,
             Boolean isMandatoryFound, Type dataItem) {
        if (startContent.equals(dataItem)) {
            return startIndex;
        } else if (endContent.equals(dataItem)) {
            return  endIndex;
        } else if (startContent.compareTo(dataItem) > 0) {
            return  isMandatoryFound ? 0 : null;
        } else if (endContent.compareTo(dataItem) < 0) {
            return isMandatoryFound ? endIndex + 1 : null;
        }
        return -1;
    }

    private BPlusTreePointer getAttributePointer(Integer index) {
        Integer indexOffset = this.tupleLength * index;
        Integer offsetOffset = this.tupleLength * index + singlePointerSize;
        return getPointerByOffset(indexOffset, offsetOffset);
    }

    private BPlusTreePointer getInternalPointer(Integer index) {
        Integer indexOffset = this.tupleLength * index;
        return getPointerByOffset(indexOffset, -1);
    }

    private BPlusTreePointer getPointerByOffset(Integer indexOffset, Integer offsetOffset) {
        if (offsetOffset < 0) {
            return new BPlusTreePointer(getInteger(indexOffset));
        } else {
            return new BPlusTreePointer(getInteger(indexOffset), getInteger(offsetOffset));
        }
    }

    private void outputPointerAttributes() {
        for (int i = 0; i <= this.currentSize; i ++) {
            Integer indexOffset = tupleLength * i;
            Integer offsetOffset = indexOffset + singlePointerSize;
            System.out.println(getInteger(indexOffset) + ", " + getInteger(offsetOffset));
        }
    }

}
