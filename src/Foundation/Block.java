package Foundation;

import javax.xml.crypto.Data;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Vector;

public class Block {

    final Integer blockSize = 4096;     // The block size is 4 Kb
    String fileIdentifier;     // The name of the file that contains the block
    Integer attributeLength;     // The length of the attribute stored in the block
    Integer firstAvailablePosition;     // If this is -1, then no available place

    byte[] storageData = new byte[blockSize];

    Block() {
        // Do some initialize work
    }

    // The blockOffset is the index of the attribute in the block
    // The index starts from 1
    public Integer getInteger(int blockOffset) {
        byte[] integerBytes = new byte[4];
        Integer integerSize = Integer.SIZE / 8;
        System.arraycopy(storageData, (blockOffset - 1) * attributeLength,
                integerBytes, 0, integerSize);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(integerBytes);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
        Integer returnValue = 0;
        try {
            returnValue = dataInputStream.readInt();
        } catch (Exception exception) {
            System.out.println("Error in readInteger method.");
            exception.printStackTrace();
        }
        return returnValue;
    }

    public Float getFloat(int blockOffset) {
        byte[] floatBytes = new byte[8];
        Integer floatSize = Float.SIZE / 8;
        System.arraycopy(storageData, (blockOffset - 1) * attributeLength,
                floatBytes, 0, floatSize);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(floatBytes);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
        Float returnValue = (float) 0;
        try {
            returnValue = dataInputStream.readFloat();
        } catch (Exception exception) {
            System.out.println("Error in readFloat method.");
            exception.printStackTrace();
        }
        return returnValue;
    }

    public String getString(int blockOffset, int stringLength) {
        byte[] stringBytes = new byte[stringLength];
        System.arraycopy(storageData, (blockOffset - 1) * attributeLength,
                stringBytes, 0, stringLength);
        return new String(stringBytes);
    }

}
