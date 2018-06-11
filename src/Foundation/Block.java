package Foundation;

import javax.xml.crypto.Data;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Vector;

public class Block {

    /*
     * Here's the storage property of class Block
     * fileIdentifier is the corresponding file name of the block
     * attribute length is the length of the attribute in the block, the unit is byte
     * isFullyOccupied indicates whether the block have extra space for another attribute
     * firstAvailablePosition is the first empty place of the block, empty list are used
     * capacity is the maximum number of attributes that the block can store
     * storageData is used to store the data of the block
     */

    final Integer blockSize = 4096;
    String fileIdentifier;
    Integer attributeLength;
    Boolean isFullyOccupied;
    Integer firstAvailablePosition;
    private Integer capacity;
    private byte[] storageData = new byte[blockSize];

    Block(String fileIdentifier, Integer attributeLength) {
        this.fileIdentifier = fileIdentifier;
        this.attributeLength = attributeLength;
        this.isFullyOccupied = false;
        this.firstAvailablePosition = 0;
        this.capacity = blockSize / attributeLength;
    }

    /*
     * The following methods are read methods of Block
     * These methods read the corresponding data type from position with index blockOffset
     * blockOffset starts from 0
     */
    public Integer getInteger(int blockOffset) {
        Integer integerSize = Integer.SIZE / 8;
        byte[] integerBytes = readFromStorage(blockOffset, integerSize);
        DataInputStream dataInputStream = createDataInputStream(integerBytes);
        Integer returnValue = 0;
        try {
            returnValue = dataInputStream.readInt();
        } catch (Exception exception) {
            handleInternalException(exception, "getInteger");
        }
        return returnValue;
    }

    public Float getFloat(int blockOffset) {
        Integer floatSize = Float.SIZE / 8;
        byte[] floatBytes = readFromStorage(blockOffset, floatSize);
        DataInputStream dataInputStream = createDataInputStream(floatBytes);
        Float returnValue = (float) 0;
        try {
            returnValue = dataInputStream.readFloat();
        } catch (Exception exception) {
            handleInternalException(exception, "getFloat");
        }
        return returnValue;
    }

    public String getString(int blockOffset, int stringLength) {
        byte[] stringBytes = readFromStorage(blockOffset, stringLength);
        return new String(stringBytes);
    }

    public byte[] getStorageData() {
        return this.storageData;
    }

    private byte[] readFromStorage(int blockOffset, int length) {
        byte[] loadedBytes = new byte[length];
        System.arraycopy(storageData, blockOffset * attributeLength,
                loadedBytes, 0, length);
        return loadedBytes;
    }

    private DataInputStream createDataInputStream(byte[] bytes) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        return new DataInputStream(byteArrayInputStream);
    }

    private void handleInternalException(Exception exception, String methodName) {
        System.out.println("Error in " + methodName + " method, class Block.");
        exception.printStackTrace();
    }

}
