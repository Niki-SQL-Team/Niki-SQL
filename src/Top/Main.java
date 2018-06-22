package Top;

import BufferManager.BufferManager;
import CatalogManager.CatalogManager;
import CatalogManager.Table;
import Foundation.Blocks.BPlusTreeBlock;
import Foundation.Blocks.Block;
import Foundation.Blocks.Converter;
import Foundation.Enumeration.DataType;
import Foundation.Exception.NKInterfaceException;
import Foundation.Exception.NKInternalException;
import Foundation.MemoryStorage.BPlusTreePointer;
import Foundation.MemoryStorage.MetadataAttribute;
import Foundation.MemoryStorage.Tuple;
import IndexManager.*;

import IndexManager.*;

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

 /*   public static void main(String args[]) {
        MetadataAttribute metadataAttribute;
        try {
            NKSql nkSql = new NKSql();
            CatalogManager catalogManager = new CatalogManager();
            metadataAttribute = new MetadataAttribute("test attribute", DataType.IntegerType, true, true);
            Vector<MetadataAttribute> attributes = new Vector<>();
            Vector<String> itemsToBeInserted = new Vector<>();
            itemsToBeInserted.add(String.valueOf(429));
            attributes.add(metadataAttribute);
            catalogManager.createTable("testTable", attributes);
            Table table = catalogManager.getTable("testTable");
            table.insertAttributes(new Tuple(itemsToBeInserted));
            nkSql.close();
        } catch (NKInterfaceException exception) {
            exception.describe();
        } catch (Exception exception) {
            System.out.println("Fuck the world.");
            exception.printStackTrace();
        }

    }*/

    public static void main(String args[]){


        BufferManager bufferManager = new BufferManager();
        Converter convert = new Converter();
        IndexManager indexManager = new IndexManager();

        Index bTree = indexManager.createBlankIndex("T1","A1",DataType.IntegerType);

        System.out.println("--------------------");
        System.out.println(bTree.table);
        System.out.println(bTree.attribute);
        System.out.println(bTree.dataType);
        System.out.println(bTree.blockOfTheIndexTree);
        System.out.println(bTree.currentNodeCount);
        System.out.println("--------------------");

        /*for(int i = 300; i >= 0; i--){
            byte[] key = convert.convertToBytes(i);
            BPlusTreePointer p1 = new BPlusTreePointer(i, i);
            indexManager.insertNewKey(bTree, key, p1);
        }
*/
       /* for(int i = 0 ; i < 250; i++){
            byte[] key = convert.convertToBytes(i);
            bTree.deleteKey(key);
        }*/



        BPlusTreeBlock brb = (BPlusTreeBlock)bufferManager.getBlock("index_T1_A1", 0);
        brb.outputAttributes();

        bufferManager.close();;
        return;
    }


}
