package Foundation.MemoryStorage;

public class BPlusTreePointer {

    public Integer blockIndex;
    public Integer blockOffset;
    private Boolean isAttributePointer;

    // This init method is used for leaf node in the B+ tree
    // To obtain the name of the related table and the attribute
    // You should refer to fileIdentifier and attributeName of BPlusTreeNodeBlock
    public BPlusTreePointer(Integer blockIndex, Integer blockOffset) {
        this.blockIndex = blockIndex;
        this.blockOffset = blockOffset;
        this.isAttributePointer = true;
    }

    // This init method is used for internal node in the B+ tree
    public BPlusTreePointer(Integer blockIndex) {
        this.blockIndex = blockIndex;
        this.blockOffset = -1;
        this.isAttributePointer = false;
    }

    public Boolean isAttributePointer() {
        return isAttributePointer;
    }

}
