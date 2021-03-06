package RecordManager;

import CatalogManager.CatalogManager;
import CatalogManager.Table;
import Foundation.Exception.NKInterfaceException;
import Foundation.MemoryStorage.ConditionalAttribute;
import Foundation.MemoryStorage.Tuple;

import java.util.ArrayList;
import java.util.Vector;

public class RecordManager {

    public static RecordManager sharedInstance;

    public RecordManager() {
        setSharedInstance();
    }

    public void insertIntoTable(String tableName, Tuple tuple) throws NKInterfaceException {
        Table table = getTableNamed(tableName);
        table.insertAttributes(tuple);
    }

    public ArrayList<Tuple> selectInTable(String tableName, ArrayList<String> attributeNames,
                              ArrayList<ConditionalAttribute> conditions) throws NKInterfaceException {
        Table table = getTableNamed(tableName);
        Vector<Tuple> allContentTuple = searchInTable(tableName, conditions);
        if (attributeNames.get(0).equals("*")) {
            return new ArrayList<>(allContentTuple);
        }
        Vector<Integer> selectedIndices = new Vector<>();
        for (String name : attributeNames) {
            selectedIndices.add(table.metadata.getAttributeIndexNamed(name));
        }
        return selectInRawResults(allContentTuple, selectedIndices);
    }

    public void deleteInTable(String tableName, ArrayList<ConditionalAttribute> conditions)
            throws NKInterfaceException {
        Table table = getTableNamed(tableName);
        table.deleteItem(conditions);
    }

    private Vector<Tuple> searchInTable(String tableName, ArrayList<ConditionalAttribute> conditions)
            throws NKInterfaceException {
        Table table = getTableNamed(tableName);
        return table.searchFor(conditions);
    }

    private void setSharedInstance() {
        sharedInstance = this;
    }

    private Table getTableNamed(String tableName) throws NKInterfaceException {
        Table table = CatalogManager.sharedInstance.getTable(tableName);
        if (table == null) {
            throw new NKInterfaceException("Table named " + tableName + " not found!");
        }
        return table;
    }

    private ArrayList<Tuple> selectInRawResults(Vector<Tuple> rawResults, Vector<Integer> indices) {
        ArrayList<Tuple> finalResults = new ArrayList<>();
        for (Tuple rawResult : rawResults) {
            Vector<String> dataItems = new Vector<>();
            for (Integer index : indices) {
                dataItems.add(rawResult.get(index));
            }
            finalResults.add(new Tuple(dataItems));
        }
        return finalResults;
    }

}
