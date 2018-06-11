package Foundation.Block;

import Foundation.MemoryStorage.BPlusTreePointer;

public class FloatBPlusTreeBlock extends BPlusTreeBlock {

    public FloatBPlusTreeBlock(String fileIdentifier, Boolean isLeafNode) {
        super(fileIdentifier, Float.SIZE / 8 + BPlusTreeBlock.singlePointerSize * 2
                , isLeafNode);
    }

    public BPlusTreePointer searchFor(Float dataItem) {
        return this.isLeafNode ? searchLeafFor(dataItem) : searchInternalFor(dataItem);
    }

    public Integer getMarker(Integer index) {
        Integer offset = index * attributeLength + 2 * singlePointerSize;
        return getInteger(offset);
    }

    private BPlusTreePointer searchInternalFor(Float dataItem) {
        for (int i = 0; i < this.currentSize; i ++) {
            Integer offset = attributeLength * i + 2 * singlePointerSize;
            if (getFloat(offset) > dataItem) {
                return getPointer(i);
            }
        }
        return getPointer(currentSize);
    }

    private BPlusTreePointer searchLeafFor(Float dataItem) {
        for (int i = 0; i < this.currentSize; i ++) {
            Integer offset = attributeLength * i + 2 * singlePointerSize;
            if (getFloat(i).equals(dataItem)) {
                return getPointer(i);
            }
        }
        return null;
    }

}
