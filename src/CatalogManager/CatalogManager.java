package CatalogManager;

import BufferManager.FileManager;
import Foundation.Exception.NKInterfaceException;
import Foundation.MemoryStorage.MetadataAttribute;
import Top.NKSql;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class CatalogManager {

    Map<String, Table> tableMetadataBuffer;
    Vector<String> tableNameInBuffer;
    FileManager<Table> fileManager;

    public void CatalogManager() {
        this.tableMetadataBuffer = new HashMap<String, Table>();
        this.fileManager = new FileManager<Table>();
        this.tableNameInBuffer = new Vector<>(NKSql.bufferSize);
    }

    public void createTable(String tableName, Vector<MetadataAttribute> metadataAttributes)
            throws NKInterfaceException {
        if (isTableExists(tableName)) {
            throw new NKInterfaceException("Table named " + tableName + " has already existed.");
        }
        Table newTable = new Table(tableName, metadataAttributes);
        flushInBuffer(newTable);
        storeTable(newTable);
    }

    public void dropTable(String tableName) throws NKInterfaceException {
        if (!isTableExists(tableName)) {
            throw new NKInterfaceException("Table named " + tableName + " doesn't exist.");
        }
        Table tableToDelete = getTable(tableName);
        tableToDelete.drop();
        tableMetadataBuffer.remove(tableName);
        fileManager.dropFile(createTablePath(tableName));
    }

    public Table getTable(String tableName) {
        if (tableMetadataBuffer.containsKey(tableName)) {
            return tableMetadataBuffer.get(tableName);
        } else {
            Table table = loadTable(tableName);
            flushInBuffer(table);
            return table;
        }
    }

    private Boolean isTableExists(String tableName) {
        if (tableMetadataBuffer.containsKey(tableName)) {
            return true;
        }
        return fileManager.isFileExist(createTablePath(tableName));
    }

    private void flushInBuffer(Table table) {
        String firstTableName = tableNameInBuffer.firstElement();
        tableNameInBuffer.remove(firstTableName);
        tableMetadataBuffer.remove(firstTableName);
        tableNameInBuffer.add(table.tableName);
        tableMetadataBuffer.put(table.tableName, table);
    }

    private Table loadTable(String path) {
        return fileManager.getObject(path);
    }

    private void storeTable(Table table) {
        fileManager.storeObject(table, createTablePath(table.tableName));
    }

    private String createTablePath(String tableName) {
        String path = NKSql.metadataHomeDirectory;
        path += "meta_";
        path += tableName;
        path += NKSql.metadataFilePostfix;
        return path;
    }

}
