package Foundation.MemoryStorage;

public class TupleStoragePointer {

    short blockIndex;
    short blockOffset;

    TupleStoragePointer(short blockIndex, short blockOffset) {
        this.blockIndex = blockIndex;
        this.blockOffset = blockOffset;
    }

}
