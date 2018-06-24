package CatalogManager;

import BufferManager.BufferManager;
import Foundation.Blocks.Block;
import Foundation.Blocks.Converter;
import Foundation.Enumeration.CompareCondition;
import Foundation.Exception.NKInterfaceException;
import Foundation.MemoryStorage.*;
import IndexManager.Index;
import IndexManager.IndexManager;

import java.io.Serializable;
import java.util.*;

public class Table implements Serializable {

    public String tableName;
    public Metadata metadata;
    public Integer numberOfBlocks;
    public Vector<Integer> availableBlocks;
    public Vector<Integer> emptyBlocks;
    private Vector<Tuple> insertIntermediateResults;
    private Map<Tuple, BPlusTreePointer> deleteIntermediateResults;
    private Map<String, Index> indices;

    public Table(String tableName, Metadata metadata)
            throws NKInterfaceException {
        this.tableName = tableName;
        this.metadata = metadata;
        this.numberOfBlocks = 0;
        this.availableBlocks = new Vector<>();
        this.emptyBlocks = new Vector<>();
        this.insertIntermediateResults = new Vector<>();
        this.deleteIntermediateResults = new LinkedHashMap<>();
        if (metadata.getTupleLength() > Block.blockSize) {
            throw new NKInterfaceException("Content in definition has exceeded block capacity.");
        }
        setUpAllIndices();
    }

    public void insertAttributes(Tuple attributeTuple) throws NKInterfaceException {
        if (!isUnique(attributeTuple)) {
            throw new NKInterfaceException("Duplicate data found on an unique attribute.");
        }
        if (availableBlocks.isEmpty()) {
            createBlockAndInsert(attributeTuple);
        } else {
            insertIntoAvailableBlock(attributeTuple);
        }
    }

    public Vector<Tuple> searchFor(ArrayList<ConditionalAttribute> conditions)
            throws NKInterfaceException {
        conditions = makeIndexedSearchFirst(conditions);
        ConditionalAttribute firstCondition = conditions.get(0);
        if (this.metadata.getMetadataAttributeNamed(firstCondition.attributeName).isIndexed) {
            firstSearchWithIndex(firstCondition);
        } else {
            firstSearchWithoutIndex(firstCondition);
        }
        conditions.remove(0);
        if (!conditions.isEmpty()) {
            subsequentSearch(conditions);
        }
        Vector<Tuple> returnValue = new Vector<>(this.insertIntermediateResults);
        insertIntermediateResults.clear();
        return returnValue;
    }

    public void deleteItem(ArrayList<ConditionalAttribute> conditions) throws NKInterfaceException {
        conditions = makeIndexedSearchFirst(conditions);
        ConditionalAttribute firstCondition = conditions.get(0);
        if (this.metadata.getMetadataAttributeNamed(firstCondition.attributeName).isIndexed) {
            firstDeleteWithIndex(firstCondition);
        } else {
            firstDeleteWithoutIndex(firstCondition);
        }
        conditions.remove(0);
        if (!conditions.isEmpty()) {
            subsequentDelete(conditions);
        }
        // Drop indices first
        Vector<Integer> emptyBlocks = executeDeletion();
        Vector<Integer> blockTrimRule = createBlockTrimRule(emptyBlocks);
        // adjust the block index
    }

    public void createIndex(String attributeName, String indexName) throws NKInterfaceException {
        setSingleIndex(attributeName, indexName);
        buildIndex(indexName);
    }

    public void dropIndex(String indexName) throws NKInterfaceException {
        Index indexToDrop = this.indices.get(indexName);
        if (indexToDrop == null) {
            throw new NKInterfaceException("There's no index named " + indexName);
        }
        IndexManager.sharedInstance.dropWholeIndex(this.indices.get(indexName));
    }

    public void drop() {
        for (int i = 0; i < this.numberOfBlocks; i ++) {
            BufferManager.sharedInstance.removeBlock(getFileIdentifier(), i);
        }
        for (Index index : this.indices.values()) {
            IndexManager.sharedInstance.dropWholeIndex(index);
        }
    }

    /*
     * The following 5 methods are supportive methods in indexing
     */
    private void setUpAllIndices() throws NKInterfaceException {
        this.indices = new LinkedHashMap<>();
        for (MetadataAttribute attribute : this.metadata.metadataAttributes.values()) {
            if (attribute.isIndexed) {
                setSingleIndex(attribute.attributeName, attribute.indexName);
                buildIndex(attribute.indexName);
            }
        }
    }

    private void setSingleIndex(String attributeName, String indexName) {
        this.metadata.getMetadataAttributeNamed(attributeName).isIndexed = true;
        this.metadata.getMetadataAttributeNamed(attributeName).setIndexName(indexName);
        MetadataAttribute attribute = this.metadata.getMetadataAttributeNamed(attributeName);
        this.indices.put(attribute.indexName, IndexManager.sharedInstance.createBlankIndex(
                this.tableName, attributeName, attribute.dataType));
    }

    private void buildIndex(String indexName) throws NKInterfaceException {
        for (int i = 0; i < this.numberOfBlocks; i ++) {
            Block block = BufferManager.sharedInstance.getBlock(this.getFileIdentifier(), i);
            Vector<Integer> dataIndices = block.getAllTupleIndices();
            for (int j = 0; j < dataIndices.size(); j ++) {
                BPlusTreePointer pointer = new BPlusTreePointer(i, j);
                byte[] key = getAttributeBytes(block.getTupleAt(dataIndices.get(i), this.metadata),
                        this.indices.get(indexName).attribute);
                IndexManager.sharedInstance.insertNewKey(this.indices.get(indexName), key, pointer);
            }
        }
    }

    private void dropIndexOnTuple(Tuple tuple) throws NKInterfaceException {
        for (MetadataAttribute attribute : this.metadata.metadataAttributes.values()) {
            if (attribute.isIndexed) {
                byte[] keyValue = getAttributeBytes(tuple, attribute.attributeName);
                IndexManager.sharedInstance.removeKey(this.indices.get(attribute.indexName), keyValue);
            }
        }
    }

    private byte[] getAttributeBytes(Tuple tuple, String attributeName) throws NKInterfaceException {
        Converter converter = new Converter();
        Integer index = this.metadata.getAttributeIndexNamed(attributeName);
        switch (this.metadata.getMetadataAttributeNamed(attributeName).dataType) {
            case IntegerType: converter.convertToBytes(Integer.valueOf(tuple.get(index)));
            case FloatType: converter.convertToBytes(Float.valueOf(tuple.get(index)));
            case StringType: converter.convertToBytes(tuple.get(index),
                    this.metadata.getMetadataAttributeNamed(attributeName).length);
        }
        return null;
    }

    /*
     * The following 2 methods are used in uniqueness decision
     */
    private Boolean isUnique(Tuple tuple) throws NKInterfaceException {
        for (MetadataAttribute attribute : this.metadata.metadataAttributes.values()) {
            if (attribute.isUnique) {
                Integer attributeIndex = this.metadata.getAttributeIndexNamed(attribute.attributeName);
                ConditionalAttribute condition = getSingleUniqueTestAttribute(attribute,
                        tuple.get(attributeIndex));
                ArrayList<ConditionalAttribute> conditions = new ArrayList<>();
                conditions.add(condition);
                if (searchFor(conditions).size() != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private ConditionalAttribute getSingleUniqueTestAttribute(MetadataAttribute attribute,
                                                              String dataItem) {
        return new ConditionalAttribute(this.tableName, attribute.attributeName,
                dataItem, CompareCondition.EqualTo);
    }

    /*
     * The following 3 methods are supportive methods in insertions
     */
    private void createBlockAndInsert(Tuple attributeTuple) throws NKInterfaceException {
        Block block = new Block(getFileIdentifier(), numberOfBlocks, this.metadata);
        numberOfBlocks ++;
        insertIntoBlock(attributeTuple, block);
        if (!block.isFullyOccupied) {
            this.availableBlocks.add(block.index);
        }
        BufferManager.sharedInstance.storeBlock(block);
    }

    private void insertIntoAvailableBlock(Tuple attributeTuple) throws NKInterfaceException {
        BufferManager bufferManager = BufferManager.sharedInstance;
        Integer blockIndexToInsert = availableBlocks.firstElement();
        Block block = bufferManager.getBlock(getFileIdentifier(), blockIndexToInsert);
        insertIntoBlock(attributeTuple, block);
        if (block.isFullyOccupied) {
            availableBlocks.remove(blockIndexToInsert);
        }
        BufferManager.sharedInstance.storeBlock(block);
    }

    private void insertIntoBlock(Tuple tuple, Block block)
            throws NKInterfaceException {
        if (!tuple.size().equals(this.metadata.numberOfAttributes)) {
            throw new NKInterfaceException("Insert values don't correspond to its metadata.");
        }
        block.writeTuple(tuple, this.metadata);
    }

    /*
     * The following 8 methods are supportive methods in deletions
     */
    private void firstDeleteWithIndex(ConditionalAttribute condition) throws NKInterfaceException {

    }

    private void firstDeleteWithoutIndex(ConditionalAttribute condition) throws NKInterfaceException {
        for (int i = 0; i < this.numberOfBlocks; i ++) {
            Block block = BufferManager.sharedInstance.getBlock(this.getFileIdentifier(), i);
            Vector<Integer> indices = block.getAllTupleIndices();
            for (Integer index : indices) {
                Tuple tuple = block.getTupleAt(index, this.metadata);
                BPlusTreePointer pointer = new BPlusTreePointer(i, index);
                this.deleteIntermediateResults.put(tuple, pointer);
            }
        }
    }

    private void subsequentSearch(ArrayList<ConditionalAttribute> conditions)
            throws NKInterfaceException {
        for (ConditionalAttribute condition : conditions) {
            singleSubsequentSearch(condition);
        }
    }

    private void subsequentDelete(ArrayList<ConditionalAttribute> conditions)
            throws NKInterfaceException {
        for (ConditionalAttribute condition : conditions) {
            singleSubsequentDelete(condition);
        }
    }

    private void singleSubsequentSearch(ConditionalAttribute condition)
            throws NKInterfaceException {
        Vector<Tuple> selectedResults = new Vector<>();
        for (Tuple tuple : this.insertIntermediateResults) {
            if (matches(condition, tuple)) {
                selectedResults.add(tuple);
            }
        }
        this.insertIntermediateResults = selectedResults;
    }

    private void singleSubsequentDelete(ConditionalAttribute condition)
            throws NKInterfaceException {
        for (Tuple tuple : this.deleteIntermediateResults.keySet()) {
            if (!matches(condition, tuple)) {
                deleteIntermediateResults.remove(tuple);
            }
        }
    }

    private Vector<Integer> executeDeletion() throws NKInterfaceException {
        Vector<Integer> emptyBlocks = new Vector<>();
        for (BPlusTreePointer pointer : this.deleteIntermediateResults.values()) {
            Block block = BufferManager.sharedInstance.getBlock(getFileIdentifier(), pointer.blockIndex);
            dropIndexOnTuple(block.getTupleAt(pointer.blockOffset, this.metadata));
            block.removeTupleAt(pointer.blockOffset);
            if (block.isDiscardable && !emptyBlocks.contains(block.index)) {
                emptyBlocks.add(block.index);
            }
        }
        this.deleteIntermediateResults.clear();
        return emptyBlocks;
    }

    private Vector<Integer> createBlockTrimRule(Vector<Integer> emptyBlocks) {
        Collections.sort(emptyBlocks);
        Vector<Integer> blockTrimRule = new Vector<>();
        Integer lastBlock = this.numberOfBlocks - 1;
        for (int i = 0; i < emptyBlocks.size(); i ++) {
            if (!emptyBlocks.contains(lastBlock)) {
                blockTrimRule.add(lastBlock);
                blockTrimRule.add(emptyBlocks.get(i));
            }
            lastBlock --;
            if (lastBlock < 0) {
                break;
            }
        }
        return blockTrimRule;
    }

    /*
     * The following 5 methods are supportive methods in select
     */
    private void firstSearchWithIndex(ConditionalAttribute condition) throws NKInterfaceException {

    }

    private void firstSearchWithoutIndex(ConditionalAttribute condition) throws NKInterfaceException {
        Vector<Tuple> result = new Vector<>();
        for (int i = 0; i < this.numberOfBlocks; i ++) {
            Block block = BufferManager.sharedInstance.getBlock(this.getFileIdentifier(), i);
            result.addAll(searchInBlock(condition, block));
        }
        this.insertIntermediateResults = result;
    }

    private Vector<Tuple> searchInBlock(ConditionalAttribute condition, Block block)
            throws NKInterfaceException {
        Vector<Tuple> blockContent = block.getAllTuples(this.metadata);
        Vector<Tuple> searchResult = new Vector<>();
        for (Tuple tuple : blockContent) {
            if (matches(condition, tuple)) {
                searchResult.add(tuple);
            }
        }
        return searchResult;
    }

    private Boolean matches(ConditionalAttribute condition, Tuple tuple)
            throws NKInterfaceException {
        Integer index = this.metadata.getAttributeIndexNamed(condition.attributeName);
        String dataItem = tuple.get(index);
        return condition.satisfies(dataItem);
    }

    private ArrayList<ConditionalAttribute> makeIndexedSearchFirst
            (ArrayList<ConditionalAttribute> conditions) {
        for (int i = 0; i < conditions.size(); i ++) {
            ConditionalAttribute conditionalAttribute = conditions.get(i);
            MetadataAttribute metadataAttribute =
                    this.metadata.getMetadataAttributeNamed(conditionalAttribute.attributeName);
            if (metadataAttribute.isIndexed && conditionalAttribute.compareCondition !=
                    CompareCondition.NotEqualTo) {
                conditions.remove(i);
                conditions.add(0, conditionalAttribute);
                return conditions;
            }
        }
        return conditions;
    }

    /*
     * The following are two general supportive methods
     */
    private Tuple getTuple(Block block, Integer index) {
        return block.getTupleAt(index, this.metadata);
    }

    private String getFileIdentifier() {
        return "data_" + this.tableName;
    }

}
