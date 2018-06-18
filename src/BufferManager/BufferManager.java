package BufferManager;

import Foundation.Blocks.Block;
import Top.NKSql;

import java.util.regex.Pattern;

public class BufferManager {

    /*
     ************************************* IMPORTANT *****************************************
     * You should mind the fileIdentifier of blocks:
     * For blocks that is used to store the data, the fileIdentifier should be
     * data_<tableName>
     * For blocks that is used to store the B Plus Tree node, the fileIdentifier should be
     * index_<tableName>_<attributeName>
     * Wrong fileIdentifier would cause the file stored with wrong postfix and in the wrong directory
     */

    /*
     ************************************* IMPORTANT *****************************************
     * You should use the BufferManager in the following way
     * BufferManager bufferManager = BufferManager.sharedInstance;
     * bufferManager.(do something)
     * Since there should be only one Buffer Manager in the database system, you're not supposed to
     * instantiate the Buffer Manager whenever you use it, instead, a Buffer Manager is instantiated
     * in NKSql, and all modules should refer to it
     */

    /*
     * sharedInstance is a pointer points to the (supposed to be only) Buffer Manager
     * bufferedBlocks is the blocks in the buffer
     * referenceBit and referencePointer is used to help the clock algorithm
     * isDirty indicates whether it's necessary to write the block to the disk again
     */
    public static BufferManager sharedInstance;
    private Block[] bufferedBlocks = new Block[NKSql.bufferSize];
    private Boolean[] referenceBit = new Boolean[NKSql.bufferSize];
    private Boolean[] isDirty = new Boolean[NKSql.bufferSize];
    private Integer referencePointer;
    private FileManager<Block> fileManager;

    public BufferManager() {
        for (int i = 0; i < NKSql.bufferSize; i ++) {
            bufferedBlocks[i] = null;
            referenceBit[i] = false;
            isDirty[i] = false;
        }
        this.referencePointer = 0;
        this.fileManager = new FileManager<Block>();
        setSharedInstance();
    }

    /*
     * You can get a block with it's identifier and index in the following method
     * A type cast would be needed if the block is BPlusTreeBlock or some other blocks
     */
    public Block getBlock(String identifier, Integer index) {
        String path = createPath(identifier, index);
        Block block = getBlockFromBuffer(identifier, index);
        if (block != null) {
            return block;
        } else {
            block = getBlockFromStorage(path);
            substituteBlock(block, false);
            return block;
        }
    }

    /*
     * You can store a block in the following way, no matter it's a new block or it's
     * an old block that is rewritten
     * The method would automatically decide whether the block has the same identifier and index
     * with blocks in the buffer, if it is, then the old block would be substituted, it not, it
     * would be inserted as a new block
     */
    public void storeBlock(Block block) {
        substituteBlock(block, true);
    }

    public void removeBlock(String identifier, Integer index) {
        Integer indexInBuffer = blockIndexInBuffer(identifier, index);
        if (indexInBuffer != -1) {
            bufferedBlocks[indexInBuffer] = null;
        }
        String path = createPath(identifier, index);
        fileManager.dropFile(path);
    }

    /*
     * The block should be closed when it's to be recycled, however, the method would be implemented
     * by NKSql, users don't need to call the method
     */
    public void close() {
        for (int i = 0; i < NKSql.bufferSize; i ++){
            if (bufferedBlocks[i] != null && isDirty[i]) {
                writeBlockToStorage(bufferedBlocks[i]);
            }
        }
    }

    /*
     * The following are some private supportive methods
     */
    private void setSharedInstance() {
        sharedInstance = this;
    }

    private Block getBlockFromBuffer(String identifier, Integer index) {
        Integer indexInBuffer = blockIndexInBuffer(identifier, index);
        return indexInBuffer == -1 ? null : bufferedBlocks[indexInBuffer];
    }

    private Integer blockIndexInBuffer(String identifier, Integer index) {
        for (int i = 0; i < NKSql.bufferSize; i ++) {
            Block block = bufferedBlocks[i];
            if (block != null && block.fileIdentifier.equals(identifier)
                    && block.index.equals(index)) {
                return i;
            }
        }
        return -1;
    }

    private void writeBlockToStorage(Block block) {
        String path = createPath(block.fileIdentifier, block.index);
        this.fileManager.storeObject(block, path);
    }

    private void substituteBlock(Block newBlock, Boolean isDirty) {
        for (int i = 0; i < NKSql.bufferSize; i ++) {
            if (bufferedBlocks[i] == null) {
                bufferedBlocks[i] = newBlock;
                referenceBit[i] = true;
                this.isDirty[i] = isDirty;
                nextReferencePointer();
                return;
            }
        }
        while (true) {
            if (referenceBit[referencePointer]) {
                referenceBit[referencePointer] = false;
                nextReferencePointer();
            } else {
                if (this.isDirty[referencePointer]) {
                    writeBlockToStorage(bufferedBlocks[referencePointer]);
                }
                bufferedBlocks[referencePointer] = newBlock;
                referenceBit[referencePointer] = true;
                nextReferencePointer();
                return;
            }
        }
    }

    private Block getBlockFromStorage(String path) {
        return this.fileManager.getObject(path);
    }

    private void nextReferencePointer() {
        this.referencePointer ++;
        this.referencePointer %= NKSql.bufferSize;
    }

    private String createPath(String identifier, Integer index) {
        Boolean isBPlusTreeBlock = isBPlusTreeBlock(identifier);
        String path = isBPlusTreeBlock ? NKSql.indexHomeDirectory : NKSql.dataHomeDirectory;
        path += identifier + "_" + String.valueOf(index);
        path += isBPlusTreeBlock ? NKSql.indexFilePostfix : NKSql.dataFilePostfix;
        return path;
    }

    private Boolean isBPlusTreeBlock(String identifier) {
        String pattern = "^index_.*$";
        return Pattern.matches(pattern, identifier);
    }

}
