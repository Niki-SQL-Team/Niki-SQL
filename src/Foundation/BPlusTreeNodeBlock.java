package Foundation;

import Foundation.Enumeration.*;
import Foundation.MemoryStorage.BPlusTreePointer;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public class BPlusTreeNodeBlock<Type extends Comparable> extends Block {

    Boolean isLeafNode;     // Whether the node is the leaf node of the B+ tree
    String attributeName;     // The attribute name of the index

    Integer degreeOfNode;     // The maximum degree of the node, depending on the data type
    final Integer pointerSize = 2 * Integer.SIZE / 8;
    Integer currentElements;     // The current number of elements in the node
    DataType dataType;

    BPlusTreeNodeBlock(DataType dataType, Boolean isLeafNode) {
        super();

        this.dataType = dataType;
        this.isLeafNode = isLeafNode;
        this.degreeOfNode = this.blockSize / (elementSize() + pointerSize) - 1;
        currentElements = 0;
        this.attributeLength = elementSize() + pointerSize;
    }

    BPlusTreePointer searchFor(Type dataItem) {
        for (int i = 0; i < currentElements; i ++) {
            if (compare(getElement(i), dataItem)) {     // dataItem < i th sign
                if (this.isLeafNode) {
                    return getAttributePointer(i);
                } else {
                    return getInternalPointer(i);
                }
            }
        }
        if (this.isLeafNode) {
            return getAttributePointer(currentElements + 1);
        } else {
            return getInternalPointer(currentElements + 1);
        }
    }

    // The index starts from 1
    Type getElement(Integer index) {
        switch (this.dataType) {
            case IntegerType: return (Type)getIntegerSign(index);
            case FloatType: return (Type)getFloatSign(index);
            case StringType: return (Type)getStringSign(index);
        }
        return null;
    }

    // The index starts from 1
    // The method should be called if the node is an internal node
    BPlusTreePointer getInternalPointer(Integer index) {
        Integer integerSize = Integer.SIZE / 8;
        byte[] blockIndex = new byte[integerSize];
        System.arraycopy(storageData, (index - 1) * attributeLength,
                blockIndex, 0, integerSize);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(blockIndex);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
        Integer tempIndex = 0;
        try {
            tempIndex = dataInputStream.readInt();
        } catch (Exception exception) {
            System.out.println("Error in getInternalPointer method.");
            exception.printStackTrace();
        }
        return new BPlusTreePointer(tempIndex);
    }

    // The index starts from 1
    // The method should be called if the node is an leaf node
    BPlusTreePointer getAttributePointer(Integer index) {
        Integer integerSize = Integer.SIZE / 8;
        byte[] blockIndex = new byte[integerSize];
        byte[] blockOffset = new byte[integerSize];
        System.arraycopy(storageData, (index - 1) * attributeLength,
                blockIndex, 0, integerSize);
        System.arraycopy(storageData, (index - 1) * attributeLength + integerSize
                , blockOffset, 0, integerSize);
        Integer tempIndex = 0, tempOffset = 0;
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(blockIndex);
            DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
            tempIndex = dataInputStream.readInt();
            byteArrayInputStream = new ByteArrayInputStream(blockOffset);
            dataInputStream = new DataInputStream(byteArrayInputStream);
            tempOffset = dataInputStream.readInt();
        } catch (Exception exception) {
            System.out.println("Error in getPointer method.");
            exception.printStackTrace();
        }
        return new BPlusTreePointer(tempIndex, tempOffset);
    }

    // The pointer that points to the first child of the node's parent's next sibling
    BPlusTreePointer getTailPointer() {
        Integer lastInteger = getIntegerSign(currentElements + 1);
        return new BPlusTreePointer(lastInteger);
    }

    private Integer elementSize() {
        switch (this.dataType) {
            case IntegerType: return Integer.SIZE / 8;
            case FloatType: return Float.SIZE / 8;
            case StringType: return 255;
        }
        return 0;
    }

    private Integer getIntegerSign(Integer index) {
        Integer integerSize = Integer.SIZE / 8;
        byte[] integerBytes = new byte[integerSize];
        System.arraycopy(storageData, (index - 1) * attributeLength + pointerSize,
                integerBytes, 0, integerSize);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(integerBytes);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
        Integer returnValue = 0;
        try {
            returnValue = dataInputStream.readInt();
        } catch (Exception exception) {
            System.out.println("Error in getIntegerSign method.");
            exception.printStackTrace();
        }
        return returnValue;
    }

    private Float getFloatSign(Integer index) {
        Integer floatSize = Float.SIZE / 8;
        byte[] floatBytes = new byte[floatSize];
        System.arraycopy(storageData, (index - 1) * attributeLength + pointerSize,
                floatBytes, 0, floatSize);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(floatBytes);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
        Float returnValue = (float) 0;
        try {
            returnValue = dataInputStream.readFloat();
        } catch (Exception exception) {
            System.out.println("Error in getFloatSign method.");
            exception.printStackTrace();
        }
        return returnValue;
    }

    private String getStringSign(Integer index) {
        byte[] stringBytes = new byte[255];
        System.arraycopy(storageData, (index - 1) * attributeLength + pointerSize,
                stringBytes, 0, 255);
        return new String(stringBytes).replaceFirst("\\s++$", "");
    }

    // The method returns true if a > b
    private <Type extends Comparable<? super Type>> Boolean compare(Type a, Type b) {
        return a.compareTo(b) > 0;
    }

}
