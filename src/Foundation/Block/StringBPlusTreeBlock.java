package Foundation.Block;

import Foundation.MemoryStorage.BPlusTreePointer;
import NKSql;

public class StringBPlusTreeBlock extends BPlusTreeBlock {

    public StringBPlusTreeBlock(String fileIdentifier, Boolean isLeafNode) {
        super(fileIdentifier, NKSql.maxLengthOfString
                + BPlusTreeBlock.singlePointerSize * 2, isLeafNode);
    }

    public BPlusTreePointer searchFor(String dataItem) {
        return this.isLeafNode ? searchLeafFor(dataItem) : searchInternalFor(dataItem);
    }

    public String getMarker(Integer index) {
        Integer offset = index * attributeLength + 2 * singlePointerSize;
        return getString(offset);
    }

    private BPlusTreePointer searchInternalFor(String dataItem) {
        for (int i = 0; i < this.currentSize; i ++ ) {
            Integer offset = this.attributeLength * i + 2 * singlePointerSize;
            if (getString(offset).compareTo(dataItem) > 0) {
                return getPointer(i);
            }
        }
        return getPointer(currentSize);
    }

    private BPlusTreePointer searchLeafFor(String dataItem) {
        for (int i = 0; i < this.currentSize; i ++) {
            Integer offset = this.attributeLength * i + 2 * singlePointerSize;
            if (getString(offset).equals(dataItem)) {
                return getPointer(i);
            }
        }
        return null;
    }

}
