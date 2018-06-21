package Top;

import BufferManager.BufferManager;
import CatalogManager.CatalogManager;
import CatalogManager.Table;
import Foundation.Blocks.BPlusTreeBlock;
import Foundation.Blocks.Block;
import Foundation.Blocks.Converter;
import Foundation.Enumeration.CompareCondition;
import Foundation.Enumeration.DataType;
import Foundation.Exception.NKInterfaceException;
import Foundation.MemoryStorage.*;
import javafx.scene.control.Tab;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
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
        try {
            NKSql nkSql = new NKSql();

            MetadataAttribute attribute_1 = new MetadataAttribute("test_integer",
                    DataType.IntegerType, false, false, false);
            MetadataAttribute attribute_2 = new MetadataAttribute("test_float",
                    DataType.FloatType, false, false, false);
            MetadataAttribute attribute_3 = new MetadataAttribute("test_string",
                    DataType.StringType, 29, false, false, false);
            Vector<MetadataAttribute> attributes = new Vector<>();
            attributes.add(attribute_1);
            attributes.add(attribute_2);
            attributes.add(attribute_3);
            Metadata metadata = new Metadata(attributes);
            Table table = new Table("test_table", metadata);

            String[] items_1 = {"1", "3.1415926", "Apple Inc."};
            Vector<String> dataItems_1 = new Vector<>();
            Collections.addAll(dataItems_1, items_1);
            Tuple tuple = new Tuple(dataItems_1);
            table.insertAttributes(tuple);

            String[] items_2 = {"2", "2.71818", "Alphabet Inc."};
            Vector<String> dataItems_2 = new Vector<>();
            Collections.addAll(dataItems_2, items_2);
            tuple = new Tuple(dataItems_2);
            table.insertAttributes(tuple);

            String[] item_3 = {"3", "6.67408", "Tesla Inc."};
            Vector<String> dataItems_3 = new Vector<>();
            Collections.addAll(dataItems_3, item_3);
            tuple = new Tuple(dataItems_3);
            table.insertAttributes(tuple);

            ConditionalAttribute condition = new ConditionalAttribute("test_search",
                    "test_integer", "2", CompareCondition.EqualTo);
            ArrayList<ConditionalAttribute> conditions = new ArrayList<>();
            conditions.add(condition);
            Vector<Tuple> result = table.searchFor(conditions);

            nkSql.close();

            System.out.println("Done.");
        } catch (Exception exception) {
            System.out.println("Fuck the world.");
            exception.printStackTrace();
        }
    }

}
