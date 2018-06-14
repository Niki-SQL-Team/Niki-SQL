package Top;

import Foundation.Blocks.BPlusTreeBlock;
import Foundation.Blocks.Block;
import Foundation.Enumeration.DataType;
import Foundation.MemoryStorage.BPlusTreePointer;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Main {

//    public static void main(String args[]) {
//        System.out.println("Congratulations!");
//        System.out.println("This is Frost's branch.");
//        System.out.println("And, have fun!");
//    }

    public static void main(String args[]) {
        BPlusTreeBlock bPlusTreeBlock = new
                BPlusTreeBlock("identifier", DataType.IntegerType, false);
        Integer integer = 429;
        byte[] bytes = toBytes(integer);
        bPlusTreeBlock.insert(new BPlusTreePointer(1,1), bytes, new BPlusTreePointer(2,2),0);
        bPlusTreeBlock.outputAttributes();
    }

    public static byte[] toBytes(Integer integer) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            dataOutputStream.writeInt(integer);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

}
