package Top;

import BufferManager.BufferManager;
import CatalogManager.Table;
import Foundation.Blocks.BPlusTreeBlock;
import Foundation.Blocks.Block;
import Foundation.Blocks.Converter;
import Foundation.Enumeration.DataType;
import Foundation.MemoryStorage.BPlusTreePointer;
import Foundation.MemoryStorage.MetadataAttribute;
import Foundation.MemoryStorage.Tuple;

import java.util.Vector;

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
        MetadataAttribute metadataAttribute;
        try {
            NKSql nkSql = new NKSql();
            metadataAttribute = new MetadataAttribute("test attribute", DataType.IntegerType, true, true);
            Vector<MetadataAttribute> attributes = new Vector<>();
            Vector<String> itemsToBeInserted = new Vector<>();
            itemsToBeInserted.add(String.valueOf(429));
            attributes.add(metadataAttribute);
            Table table = new Table("testTable", attributes);
            table.insertAttributes(new Tuple(itemsToBeInserted));
            nkSql.close();
        } catch (Exception exception) {
            System.out.println("Fuck the world.");
            exception.printStackTrace();
        }

    }

}
