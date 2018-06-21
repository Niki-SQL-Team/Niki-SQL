package CatalogManager;

import BufferManager.FileManager;
import Foundation.Exception.NKInterfaceException;
import Foundation.MemoryStorage.Metadata;
import Top.NKSql;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class CatalogManager {

    public static CatalogManager sharedInstance;
    private Map<String, Table> tableMetadataBuffer;
    private Vector<String> tableNameInBuffer;
    private FileManager<Table> fileManager;

    public CatalogManager() {
        this.tableMetadataBuffer = new HashMap<String, Table>();
        this.fileManager = new FileManager<Table>();
        this.tableNameInBuffer = new Vector<>(NKSql.bufferSize);
    }

    public void createTable(String tableName, Metadata metadata)
            throws NKInterfaceException {
        if (isTableExists(tableName)) {
            throw new NKInterfaceException("Table named " + tableName + " has already existed.");
        }
        setSharedInstance();
        Table newTable = new Table(tableName, metadata);
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
            if (table != null) {
                flushInBuffer(table);
                return table;
            }
            return null;
        }
    }

    private void setSharedInstance() {
        sharedInstance = this;
    }

    private Boolean isTableExists(String tableName) {
        if (tableMetadataBuffer.containsKey(tableName)) {
            return true;
        }
        return fileManager.isFileExist(createTablePath(tableName));
    }

    private void flushInBuffer(Table table) {
        if (tableMetadataBuffer.size() < NKSql.bufferSize) {
            addTableToBuffer(table);
        } else {
            removeTableFromBuffer(tableNameInBuffer.firstElement());
        }
    }

    private void removeTableFromBuffer(String tableName) {
        tableNameInBuffer.remove(tableName);
        tableMetadataBuffer.remove(tableName);
    }

    private void addTableToBuffer(Table table) {
        tableNameInBuffer.add(table.tableName);
        tableMetadataBuffer.put(table.tableName, table);
    }

    private Table loadTable(String path) {
        if (fileManager.isFileExist(path)) {
            return fileManager.getObject(path);
        } else {
            return null;
        }
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
