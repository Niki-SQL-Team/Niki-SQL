package Top;

import BufferManager.BufferManager;
import Foundation.Blocks.BPlusTreeBlock;
import Foundation.Blocks.Block;
import Foundation.Blocks.Converter;
import Foundation.Enumeration.DataType;
import Foundation.MemoryStorage.BPlusTreePointer;

public class Main {

//    public static void main(String args[]) {
//        System.out.println("Congratulations!");
//        System.out.println("This is Frost's branch.");
//        System.out.println("And, have fun!");
//    }

//    public static void main(String args[]) {
//        BPlusTreeBlock bPlusTreeBlock = new
//                BPlusTreeBlock("identifier", DataType.StringType, 0, true);
//        Converter converter = new Converter();
//        String[] words = new String[] {"apple", "pear", "peach", "banana", "passion", "melon", "cherry",
//        "berry", "pineapple", "coco"};
//        for (int i = 0; i < 7; i ++) {
//            bPlusTreeBlock.insert(converter.convertToBytes(words[i]), new BPlusTreePointer(i));
//        }
//        bPlusTreeBlock.outputAttributes();
//        try {
//            bPlusTreeBlock.remove(converter.convertToBytes("melon"), true);
//        } catch (Exception exception) {
//            System.out.println("Fuck the world.");
//        }
//        bPlusTreeBlock.outputAttributes();
//    }

    public static void main(String args[]) {
        Block block_1 = new Block("1th block", 4, 0);
        block_1.writeString("Good job", 0);
        Block block_2 = new Block("2th block", 4, 1);
        BPlusTreeBlock bPlusTreeBlock = new BPlusTreeBlock("index_someblock", DataType.IntegerType, 0, true);
        Converter converter = new Converter();
        bPlusTreeBlock.insert(converter.convertToBytes("apple"), new BPlusTreePointer(1, 1));
        Block block_3 = new Block("3th block", 4, 2);
        Block block_4 = new Block("4th block", 4, 3);
        Block block_5 = new Block("5th block", 4, 4);
        Block block_6 = new Block("6th block", 4, 5);
        Block block_7 = new Block("7th block", 4, 6);
        Block block_8 = new Block("8th block", 4, 7);
        Block block_9 = new Block("9th block", 4, 8);
        Block block_10 = new Block("10th block", 4, 9);
        NKSql nkSql = null;
        try {
            nkSql = new NKSql();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        assert nkSql != null;
        BufferManager bufferManager = BufferManager.sharedInstance;
        bufferManager.storeBlock(block_1);
        bufferManager.storeBlock(bPlusTreeBlock);
        bufferManager.storeBlock(block_2);
        bufferManager.storeBlock(block_3);
        bufferManager.storeBlock(block_4);
        bufferManager.storeBlock(block_5);
        bufferManager.storeBlock(block_6);
        bufferManager.storeBlock(block_7);
        bufferManager.storeBlock(block_8);
        bufferManager.storeBlock(block_9);
        bufferManager.storeBlock(block_10);
        Block anotherBlock = bufferManager.getBlock("1th block", 0);
        System.out.println(anotherBlock.getString(0));
        BPlusTreeBlock treeBlock = (BPlusTreeBlock)bufferManager.getBlock("index_someblock", 0);
        System.out.println(treeBlock.isLeafNode);
        bufferManager.close();
    }

}
