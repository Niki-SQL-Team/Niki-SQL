package CatalogManager;

import BufferManager.BufferManager;
import Foundation.Blocks.Block;
import Foundation.Enumeration.DataType;
import Foundation.Exception.NKInterfaceException;
import Foundation.Exception.NKInternalException;
import Foundation.MemoryStorage.MetadataAttribute;
import Foundation.MemoryStorage.Tuple;
import Top.NKSql;

import java.io.Serializable;
import java.util.Vector;

public class Table implements Serializable {

    public String tableName;
    public Vector<MetadataAttribute> metadataAttributes;
    public Integer numberOfBlocks;
    public Vector<Integer> availableBlocks;

    public Table(String tableName, Vector<MetadataAttribute> metadataAttributes)
            throws NKInterfaceException {
        this.tableName = tableName;
        this.metadataAttributes = metadataAttributes;
        this.numberOfBlocks = 0;
        this.availableBlocks = new Vector<>();
        if (getAttributeLength() > Block.blockSize) {
            throw new NKInterfaceException("Content in definition has exceeded block capacity.");
        }
    }

    public void insertAttributes(Tuple attributeTuple) throws NKInterfaceException {
        if (availableBlocks.isEmpty()) {
            createBlockAndInsert(attributeTuple);
        } else {
            insertIntoAvailableBlock(attributeTuple);
        }
    }

    public void drop() {
        for (int i = 0; i < this.numberOfBlocks; i ++) {
            BufferManager.sharedInstance.removeBlock(getFileIdentifier(), i);
        }
    }

    private void createBlockAndInsert(Tuple attributeTuple) throws NKInterfaceException {
        Block block = new Block(getFileIdentifier(), getAttributeLength(), numberOfBlocks);
        numberOfBlocks ++;
        try {
            insertIntoBlock(attributeTuple, block);
        } catch (NKInternalException exception) {
            exception.describe();
            exception.printStackTrace();
        }
        if (!block.isFullyOccupied) {
            this.availableBlocks.add(block.index);
        }
        BufferManager.sharedInstance.storeBlock(block);
    }

    private void insertIntoAvailableBlock(Tuple attributeTuple) throws NKInterfaceException {
        BufferManager bufferManager = BufferManager.sharedInstance;
        Integer blockIndexToInsert = availableBlocks.firstElement();
        Block block = bufferManager.getBlock(getFileIdentifier(), blockIndexToInsert);
        try {
            insertIntoBlock(attributeTuple, block);
        } catch (NKInternalException exception) {
            exception.describe();
            exception.printStackTrace();
        }
        if (block.isFullyOccupied) {
            availableBlocks.remove(blockIndexToInsert);
        }
        BufferManager.sharedInstance.storeBlock(block);
    }

    private void insertIntoBlock(Tuple tuple, Block block)
            throws NKInterfaceException, NKInternalException {
        Integer insertOffset = block.declareOccupancy();
        Integer iterator = 0;
        if (tuple.dataItems.size() != this.metadataAttributes.size()) {
            throw new NKInterfaceException("Insert values don't correspond to its metadata.");
        }
        for (MetadataAttribute attribute : this.metadataAttributes) {
            writeItemToBlock(tuple.dataItems.get(iterator), attribute.dataType, block, insertOffset);
            insertOffset += getDataTypeSize(attribute.dataType);
        }
    }

    private void writeItemToBlock(String item, DataType dataType, Block block, Integer offset)
            throws NKInterfaceException {
        try {
            switch (dataType) {
                case IntegerType: block.writeInteger(Integer.valueOf(item), offset); break;
                case FloatType: block.writeFloat(Float.valueOf(item), offset); break;
                case StringType: block.writeString(item, offset);
            }
        } catch (Exception exception) {
            throw new NKInterfaceException("Insert values don't correspond to its metadata.");
        }
    }

    private String getFileIdentifier() {
        return "data_" + this.tableName;
    }

    private Integer getAttributeLength() {
        Integer length = 0;
        for (MetadataAttribute attribute : this.metadataAttributes) {
            length += getDataTypeSize(attribute.dataType);
        }
        return length;
    }

    private Integer getDataTypeSize(DataType dataType) {
        switch (dataType) {
            case IntegerType: return Integer.SIZE / 8;
            case FloatType: return Float.SIZE / 8;
            case StringType: return NKSql.maxLengthOfString;
        }
        return -1;
    }

}
