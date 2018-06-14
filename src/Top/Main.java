package Top;

import Foundation.Blocks.BPlusTreeBlock;
import Foundation.Blocks.Converter;
import Foundation.Enumeration.DataType;
import Foundation.MemoryStorage.BPlusTreePointer;

public class Main {

//    public static void main(String args[]) {
//        System.out.println("Congratulations!");
//        System.out.println("This is Frost's branch.");
//        System.out.println("And, have fun!");
//    }

    public static void main(String args[]) {
        BPlusTreeBlock bPlusTreeBlock = new
                BPlusTreeBlock("identifier", DataType.StringType, true);
        Converter converter = new Converter();
        String[] words = new String[] {"apple", "pear", "peach", "banana", "passion", "melon", "cherry",
        "berry", "pineapple", "coco"};
        for (int i = 0; i < 7; i ++) {
            bPlusTreeBlock.insert(converter.convertToBytes(words[i]), new BPlusTreePointer(i));
        }
        bPlusTreeBlock.outputAttributes();
    }

}
