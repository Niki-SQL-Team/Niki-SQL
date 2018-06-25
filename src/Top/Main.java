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
import Interpreter.Interpreter;

import java.io.DataInputStream;
import java.util.*;
import java.util.concurrent.locks.Condition;

public class Main {

//    public static void main(String args[]) {
//        try {
//            NKSql nkSql = new NKSql();
//
//            ArrayList<MetadataAttribute> attributes = new ArrayList<>();
//            for (int i = 0; i < 32; i ++) {
//                attributes.add(new MetadataAttribute("pressure_" + String.valueOf(i),
//                        DataType.StringType, 255, false, false, false));
//            }
//            nkSql.createTable("test_table", attributes);
//            Vector<String> dataItem = new Vector<>();
//            for (int i = 0; i < 32; i ++) {
//                dataItem.add(getRandomString(255));
//            }
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
//
//    private static String getRandomString(int length){
//        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
//        Random random = new Random();
//        StringBuilder sb=new StringBuilder();
//        for(int i = 0; i < length; i ++){
//            int number = random.nextInt(62);
//            sb.append(str.charAt(number));
//        }
//        return sb.toString();
//    }

//    public static void main(String args[]) {
//        try {
//            NKSql nkSql = new NKSql();
//
//            ArrayList<MetadataAttribute> attributes = new ArrayList<>();
//            MetadataAttribute attribute_1 = new MetadataAttribute("test_int",
//                    DataType.IntegerType, true, true);
//            attribute_1.setIndexName("test_index");
//            MetadataAttribute attribute_2 = new MetadataAttribute("test_float",
//                    DataType.FloatType, false, true);
//            MetadataAttribute attribute_3 = new MetadataAttribute("test_string",
//                    DataType.StringType, 255, false, true);
//
//            attributes.add(attribute_1);
//            attributes.add(attribute_2);
//            attributes.add(attribute_3);
//
//            NKSql.createTable("test_table", attributes);
//
//            Vector<String> dataItem = new Vector<>();
//            Tuple tuple;
//
//            dataItem.add("3");
//            dataItem.add("3.1415");
//            dataItem.add("Apple Inc");
//            tuple = new Tuple(dataItem);
//            NKSql.insertTuple(tuple, "test_table");
//
//            dataItem.clear();
//            dataItem.add("1");
//            dataItem.add("2.7182");
//            dataItem.add("Alphabet Inc");
//            tuple = new Tuple(dataItem);
//            NKSql.insertTuple(tuple, "test_table");
//
//            dataItem.clear();
//            dataItem.add("2");
//            dataItem.add("6.67");
//            dataItem.add("Tesla Inc");
//            tuple = new Tuple(dataItem);
//            NKSql.insertTuple(tuple, "test_table");
//
//
//
//            ArrayList<String> selectTarget = new ArrayList<>();
//            ArrayList<ConditionalAttribute> conditions = new ArrayList<>();
//            selectTarget.add("*");
//            conditions.add(new ConditionalAttribute("test_table", "test_int",
//                    "2", CompareCondition.NotEqualTo));
//            ArrayList<Tuple> result = NKSql.select("test_table", selectTarget, conditions);
//
//
//
//            NKSql.close();
//        } catch (Exception exception) {
//            exception.printStackTrace();
//        }
//
//    }

    public static void main(String args[]) {
        Interpreter interpreter = new Interpreter();
        Interpreter.startInterpreter(args);
    }


}
