package Foundation.Block;

import Foundation.MemoryStorage.BPlusTreePointer;

public class IntegerBPlusTreeBlock extends BPlusTreeBlock {

    public IntegerBPlusTreeBlock(String fileIdentifier, Boolean isLeafNode) {
        super(fileIdentifier, Integer.SIZE / 8 + BPlusTreeBlock.singlePointerSize * 2,
                isLeafNode);
    }

    public BPlusTreePointer searchFor(Integer dataItem) {
        return this.isLeafNode ? searchLeafFor(dataItem) : searchInternalFor(dataItem);
    }

    public Integer getMarker(Integer index) {
        Integer offset = index * attributeLength + 2 * singlePointerSize;
        return getInteger(offset);
    }

    private BPlusTreePointer searchInternalFor(Integer dataItem) {
        for (int i = 0; i < this.currentSize; i ++) {
            Integer offset = attributeLength * i + 2 * singlePointerSize;
            if (getInteger(offset) > dataItem) {
                return getPointer(i);
            }
        }
        return getPointer(currentSize);
    }

    private BPlusTreePointer searchLeafFor(Integer dataItem) {
        for (int i = 0; i < this.currentSize; i ++) {
            Integer offset = attributeLength * i + 2 * singlePointerSize;
            if (getInteger(offset).equals(dataItem)) {
                return getPointer(i);
            }
        }
        return null;
    }

}
