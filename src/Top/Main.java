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
import Foundation.MemoryStorage.*;
import Foundation.Enumeration.*;

import IndexManager.*;

import IndexManager.*;

import java.util.ArrayList;
import java.util.Vector;

public class Main {
//
//    public static void main(String args[]) {
//        try {
//            NKSql nkSql = new NKSql();
//
//            MetadataAttribute attribute = new MetadataAttribute("test_attribute",
//                    DataType.IntegerType, false, false, false);
//            ArrayList<MetadataAttribute> attributes = new ArrayList<>();
//            attributes.add(attribute);
//            nkSql.createTable("test_table", attributes);
//
//            Vector<String> dataItem = new Vector<>();
//            dataItem.add("115168");
//            Tuple testTuple = new Tuple(dataItem);
//            for (int i = 0; i < 100000; i ++) {
//                nkSql.insertTuple(testTuple, "test_table");
//                if (i % 1000 == 0) {
//                    System.out.print("\r" + i / 1000 + "% Done.");
//                }
//            }
//            System.out.print("\r");
//
//            nkSql.close();
//
//            System.out.println("Done.");
//        } catch (Exception exception) {
//            System.out.println("Fuck the world.");
//            exception.printStackTrace();
//        }
//    }

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

        for(int i = 30000; i >= 0; i--){
            byte[] key = convert.convertToBytes(i);
            BPlusTreePointer p1 = new BPlusTreePointer(i, i);
            indexManager.insertNewKey(bTree, key, p1);
        }

       /* for(int i = 0 ; i < 250; i++){
            byte[] key = convert.convertToBytes(i);
            bTree.deleteKey(key);
        }*/



//        BPlusTreeBlock brb = (BPlusTreeBlock)bufferManager.getBlock("index_T1_A1", 0);
//        //brb.outputAttributes();
//
//        brb = (BPlusTreeBlock)bufferManager.getBlock("index_T1_A1", 1);
//        //brb.outputAttributes();
//
//        brb = (BPlusTreeBlock)bufferManager.getBlock("index_T1_A1", 2);
//        //brb.outputAttributes();

        bufferManager.close();
    }

}
