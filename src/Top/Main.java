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
//        try {
//            NKSql nkSql = new NKSql();
//
//
//            ConditionalAttribute condition = new ConditionalAttribute("test_search",
//                    "test_integer", "2", CompareCondition.NoGreaterThan);
//            ArrayList<ConditionalAttribute> conditions = new ArrayList<>();
//            conditions.add(condition);
//            ArrayList<String> attributeNames = new ArrayList<>();
//            attributeNames.add("test_string");
//            ArrayList<Tuple> result = nkSql.select("test_table", attributeNames, conditions);
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



        BPlusTreeBlock brb = (BPlusTreeBlock)bufferManager.getBlock("index_T1_A1", 0);
        //brb.outputAttributes();

        brb = (BPlusTreeBlock)bufferManager.getBlock("index_T1_A1", 1);
        //brb.outputAttributes();

        brb = (BPlusTreeBlock)bufferManager.getBlock("index_T1_A1", 2);
        //brb.outputAttributes();

        bufferManager.close();
    }

}
