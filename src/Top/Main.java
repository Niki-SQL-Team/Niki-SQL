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


            ConditionalAttribute condition = new ConditionalAttribute("test_search",
                    "test_integer", "2", CompareCondition.NoGreaterThan);
            ArrayList<ConditionalAttribute> conditions = new ArrayList<>();
            conditions.add(condition);
            ArrayList<String> attributeNames = new ArrayList<>();
            attributeNames.add("test_string");
            ArrayList<Tuple> result = nkSql.select("test_table", attributeNames, conditions);

            nkSql.close();

            System.out.println("Done.");
        } catch (Exception exception) {
            System.out.println("Fuck the world.");
            exception.printStackTrace();
        }
    }

}
