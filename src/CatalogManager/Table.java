package CatalogManager;

import BufferManager.BufferManager;
import Foundation.Blocks.Block;
import Foundation.Enumeration.CompareCondition;
import Foundation.Enumeration.DataType;
import Foundation.Exception.NKInterfaceException;
import Foundation.Exception.NKInternalException;
import Foundation.MemoryStorage.ConditionalAttribute;
import Foundation.MemoryStorage.MetadataAttribute;
import Foundation.MemoryStorage.Tuple;
import Top.NKSql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

public class Table implements Serializable {

    public String tableName;
    public Vector<MetadataAttribute> metadataAttributes;
    public Integer numberOfBlocks;
    public Vector<Integer> availableBlocks;
    public Vector<Integer> emptyBlocks;
    private Vector<Tuple> intermediateResults;

    public Table(String tableName, Vector<MetadataAttribute> metadataAttributes)
            throws NKInterfaceException {
        this.tableName = tableName;
        this.metadataAttributes = metadataAttributes;
        this.numberOfBlocks = 0;
        this.availableBlocks = new Vector<>();
        this.emptyBlocks = new Vector<>();
        this.intermediateResults = new Vector<>();
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

    public Vector<Tuple> searchFor(ArrayList<ConditionalAttribute> conditionalAttributes)
            throws NKInterfaceException {
        conditionalAttributes = makeIndexedSearchFirst(conditionalAttributes);
        ConditionalAttribute firstCondition = conditionalAttributes.get(0);
        if (getAttribute(firstCondition.attributeName).isIndexed) {
            firstSearchWithIndex(firstCondition);
        } else {
            firstSearchWithoutIndex(firstCondition);
        }
        conditionalAttributes.remove(0);
        if (!conditionalAttributes.isEmpty()) {
            subsequentSearch(conditionalAttributes);
        }
        Vector<Tuple> returnValue = this.intermediateResults;
        this.intermediateResults.clear();
        return returnValue;
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
        if (tuple.size() != this.metadataAttributes.size()) {
            throw new NKInterfaceException("Insert values don't correspond to its metadata.");
        }
        for (MetadataAttribute attribute : this.metadataAttributes) {
            writeItemToBlock(tuple.get(iterator), attribute.dataType, block, insertOffset);
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

    private void firstSearchWithIndex(ConditionalAttribute condition) throws NKInterfaceException {
        Vector<Tuple> result = new Vector<>();
        for (int i = 0; i < this.numberOfBlocks; i ++) {
            Block block = BufferManager.sharedInstance.getBlock(this.getFileIdentifier(), i);
            result.addAll(searchInBlock(condition, block));
        }
        this.intermediateResults = result;
    }

    private void firstSearchWithoutIndex(ConditionalAttribute condition) {

    }

    private void subsequentSearch(ArrayList<ConditionalAttribute> conditions)
            throws NKInterfaceException {
        for (ConditionalAttribute condition : conditions) {
            singleSubsequentSearch(condition);
        }
    }

    private void singleSubsequentSearch(ConditionalAttribute conditionalAttribute)
            throws NKInterfaceException {
        Vector<Tuple> selectedResults = new Vector<>();
        for (Tuple tuple : this.intermediateResults) {
            if (matches(conditionalAttribute, tuple)) {
                selectedResults.add(tuple);
            }
        }
        this.intermediateResults = selectedResults;
    }

    private Vector<Tuple> searchInBlock(ConditionalAttribute condition, Block block)
            throws NKInterfaceException {
        Vector<Tuple> result = new Vector<>();
        for (int index = 0; index < block.currentSize; index ++) {
            Tuple tuple = getTuple(block, index);
            if (matches(condition, tuple)) {
                result.add(tuple);
            }
        }
        return result;
    }

    private Boolean matches(ConditionalAttribute condition, Tuple tuple)
            throws NKInterfaceException {
        Integer index = getAttributeIndex(condition.attributeName);
        String dataItem = tuple.get(index);
        return condition.satisfies(dataItem);
    }

    private Boolean matchesAll(ArrayList<ConditionalAttribute> conditions, Tuple tuple)
            throws NKInterfaceException {
        for (ConditionalAttribute condition : conditions) {
            if (!matches(condition, tuple)) {
                return false;
            }
        }
        return true;
    }

    private ArrayList<ConditionalAttribute> makeIndexedSearchFirst
            (ArrayList<ConditionalAttribute> conditions) throws NKInterfaceException {
        for (int i = 0; i < conditions.size(); i ++) {
            ConditionalAttribute conditionalAttribute = conditions.get(i);
            Integer attributeIndex = getAttributeIndex(conditionalAttribute.attributeName);
            MetadataAttribute metadataAttribute = getAttribute(conditionalAttribute.attributeName);
            if (metadataAttribute.isIndexed && conditionalAttribute.compareCondition !=
                    CompareCondition.NotEqualTo) {
                conditions.remove(i);
                conditions.add(0, conditionalAttribute);
                return conditions;
            }
        }
        return conditions;
    }

    private Tuple getTuple(Block block, Integer index) {
        Vector<String> dataItems = new Vector<>();
        Integer offset = index * block.attributeLength;
        for (MetadataAttribute metadata : this.metadataAttributes) {
            dataItems.add(getAttributeFromBlock(block, metadata.dataType, offset));
            offset += getDataTypeSize(metadata.dataType);
        }
        return new Tuple(dataItems);
    }

    private String getAttributeFromBlock(Block block, DataType dataType, Integer offset) {
        switch (dataType) {
            case IntegerType: return String.valueOf(block.getInteger(offset));
            case FloatType: return String.valueOf(block.getFloat(offset));
            case StringType: return block.getString(offset);
        }
        return null;
    }

    private MetadataAttribute getAttribute(String attributeName) throws NKInterfaceException {
        for (MetadataAttribute metadataAttribute : this.metadataAttributes) {
            if (metadataAttribute.attributeName.equals(attributeName)) {
                return metadataAttribute;
            }
        }
        throw new NKInterfaceException("Attribute " + attributeName + "is not found in table.");
    }

    private Integer getAttributeIndex(String attributeName) throws NKInterfaceException {
        for (int i = 0; i < this.metadataAttributes.size(); i ++) {
            if (this.metadataAttributes.get(i).attributeName.equals(attributeName)) {
                return i;
            }
        }
        throw new NKInterfaceException("Attribute " + attributeName + "is not found in table.");
    }

    private Integer getAttributeOffset(String attributeName) throws NKInterfaceException {
        Boolean isFound = false;
        Integer offset = 0;
        for (MetadataAttribute attribute : this.metadataAttributes) {
            if (!attribute.attributeName.equals(attributeName)) {
                offset += getDataTypeSize(attribute.dataType);
            } else {
                isFound = true;
                break;
            }
        }
        if (!isFound) {
            throw new NKInterfaceException("Attribute " + attributeName + "is not found in table.");
        }
        return offset;
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
