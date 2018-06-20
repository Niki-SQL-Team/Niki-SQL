package Top;

import BufferManager.BufferManager;
import CatalogManager.CatalogManager;
import CatalogManager.Table;
import Foundation.Blocks.BPlusTreeBlock;
import Foundation.Blocks.Block;
import Foundation.Blocks.Converter;
import Foundation.Enumeration.DataType;
import Foundation.Exception.NKInterfaceException;
import Foundation.MemoryStorage.BPlusTreePointer;
import Foundation.MemoryStorage.Metadata;
import Foundation.MemoryStorage.MetadataAttribute;
import Foundation.MemoryStorage.Tuple;
import javafx.scene.control.Tab;

import java.lang.reflect.Type;
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
            MetadataAttribute attribute_1 = new MetadataAttribute("test_integer",
                    DataType.IntegerType, true, true, true);
            MetadataAttribute attribute_2 = new MetadataAttribute("test_float",
                    DataType.FloatType, false, false, false);
            MetadataAttribute attribute_3 = new MetadataAttribute("test_string",
                    DataType.StringType, 29, false, false, false);
            Vector<MetadataAttribute> attributes = new Vector<>();
            attributes.add(attribute_1);
            attributes.add(attribute_2);
            attributes.add(attribute_3);
            Metadata metadata = new Metadata(attributes);
            Block block = new Block("test_block", 0, metadata);

            String[] items = {"429", "3.1415926", "Apple Inc."};
            Vector<String> dataItems = new Vector<>();
            Collections.addAll(dataItems, items);
            Tuple tuple = new Tuple(dataItems);
            block.writeTuple(tuple, metadata);
            Vector<Tuple> tuples = block.getAllTuples(metadata);
            System.out.println("Done.");
        } catch (Exception exception) {
            System.out.println("Fuck the world.");
            exception.printStackTrace();
        }
    }

}
