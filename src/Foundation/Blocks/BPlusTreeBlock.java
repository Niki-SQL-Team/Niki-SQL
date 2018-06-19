package Foundation.Blocks;

import Foundation.Enumeration.DataType;
import Foundation.Exception.NKInternalException;
import Foundation.MemoryStorage.BPlusTreeMetadata;
import Foundation.MemoryStorage.BPlusTreePointer;
import Foundation.MemoryStorage.MetadataAttribute;
import Foundation.MemoryStorage.Tuple;
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

    public BPlusTreeBlock(String fileIdentifier, DataType dataType, Integer index, Boolean isLeafNode) {
        super(fileIdentifier, index, new BPlusTreeMetadata(dataType));
        this.dataType = dataType;
        if (dataType.equals(DataType.StringType)) {
            System.out.println("String type should specify its length when constructing B+ tree.");
        }
        this.isLeafNode = isLeafNode;
        this.markerCapacity = this.capacity - 1;
        this.markerLength = attributeLength - 2 * singlePointerSize;
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
        this.markerLength = attributeLength - 2 * singlePointerSize;
        this.isDiscardable = false;
    }

    public BPlusTreePointer getPointer(Integer index) {
        return this.isLeafNode ? getAttributePointer(index) : getInternalPointer(index);
    }

    public byte[] getAttribute(Integer index) {
        Integer offset = index * this.attributeLength + 2 * singlePointerSize;
        return readFromStorage(offset, this.markerLength);
    }

    /*
     * Tail Pointer is the pointer that is used to point to the sibling of a node in a B+ tree
     * The tail pointer is only a integer, indicating the block index of the sibling
     * The caller of the BPlusTreeBlock is responsible for the maintenance of the Tail Pointer
     */
    public BPlusTreePointer getTailPointer() {
        Integer indexOffset = this.attributeLength * this.markerCapacity + 2 * singlePointerSize;
        return getPointerByOffset(indexOffset, -1);
    }

    public void setTailPointer(Integer blockIndex) {
        Integer tailOffset = this.attributeLength * markerCapacity;
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
        return index == null ? null : getPointer(index);
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
        return pointerIndex;
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
                case StringType: System.out.println(getString(offset, this.markerLength)); break;
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

    private Integer searchStringFor(String dataItem, Boolean isMandatoryFound, Integer length) {
        for (int i = 0; i < currentSize; i ++) {
            Integer offset = attributeLength * i + 2 * singlePointerSize;
            String content = getString(offset, length);
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

    private void outputPointerAttributes() {
        for (int i = 0; i <= this.currentSize; i ++) {
            Integer indexOffset = attributeLength * i;
            Integer offsetOffset = indexOffset + singlePointerSize;
            System.out.println(getInteger(indexOffset) + ", " + getInteger(offsetOffset));
        }
    }

}
