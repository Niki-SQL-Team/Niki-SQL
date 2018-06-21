package CatalogManager;

import BufferManager.BufferManager;
import Foundation.Blocks.Block;
import Foundation.Enumeration.CompareCondition;
import Foundation.Exception.NKInterfaceException;
import Foundation.MemoryStorage.ConditionalAttribute;
import Foundation.MemoryStorage.Metadata;
import Foundation.MemoryStorage.MetadataAttribute;
import Foundation.MemoryStorage.Tuple;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

public class Table implements Serializable {

    public String tableName;
    public Metadata metadata;
    public Integer numberOfBlocks;
    public Vector<Integer> availableBlocks;
    public Vector<Integer> emptyBlocks;
    private Vector<Tuple> intermediateResults;

    public Table(String tableName, Metadata metadata)
            throws NKInterfaceException {
        this.tableName = tableName;
        this.metadata = metadata;
        this.numberOfBlocks = 0;
        this.availableBlocks = new Vector<>();
        this.emptyBlocks = new Vector<>();
        this.intermediateResults = new Vector<>();
        if (metadata.getTupleLength() > Block.blockSize) {
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
        this.intermediateResults.clear();
        conditionalAttributes = makeIndexedSearchFirst(conditionalAttributes);
        ConditionalAttribute firstCondition = conditionalAttributes.get(0);
        if (this.metadata.getMetadataAttributeNamed(firstCondition.attributeName).isIndexed) {
            firstSearchWithIndex(firstCondition);
        } else {
            firstSearchWithoutIndex(firstCondition);
        }
        conditionalAttributes.remove(0);
        if (!conditionalAttributes.isEmpty()) {
            subsequentSearch(conditionalAttributes);
        }
        return this.intermediateResults;
    }

    public void drop() {
        for (int i = 0; i < this.numberOfBlocks; i ++) {
            BufferManager.sharedInstance.removeBlock(getFileIdentifier(), i);
        }
    }

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

    private void firstSearchWithIndex(ConditionalAttribute condition) throws NKInterfaceException {
        // Lv Lei's Job
    }

    private void firstSearchWithoutIndex(ConditionalAttribute condition) throws NKInterfaceException {
        Vector<Tuple> result = new Vector<>();
        for (int i = 0; i < this.numberOfBlocks; i ++) {
            Block block = BufferManager.sharedInstance.getBlock(this.getFileIdentifier(), i);
            result.addAll(searchInBlock(condition, block));
        }
        this.intermediateResults = result;
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

    private Tuple getTuple(Block block, Integer index) {
        return block.getTupleAt(index, this.metadata);
    }

    private String getFileIdentifier() {
        return "data_" + this.tableName;
    }

}
