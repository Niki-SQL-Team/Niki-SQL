package Foundation.Block;

import Foundation.Block.Block;
import Foundation.MemoryStorage.BPlusTreePointer;

public class BPlusTreeBlock extends Block {

    public Boolean isLeafNode;
    public Integer markerCapacity;
    protected Integer markerLength;
    protected static final Integer singlePointerSize = Integer.SIZE / 8;

    public BPlusTreeBlock(String fileIdentifier, Integer attributeLength, Boolean isLeafNode) {
        super(fileIdentifier, attributeLength);
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

}
