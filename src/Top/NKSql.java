package Top;

import BufferManager.BufferManager;
import CatalogManager.CatalogManager;
import Foundation.Exception.*;
import Foundation.MemoryStorage.*;
import RecordManager.RecordManager;

import java.util.ArrayList;

public class NKSql {

    public static final String homeDirectory = "/Users/licanchen/Desktop/Niki SQL/DB Files/";
    public static final String dataHomeDirectory = homeDirectory + "Data/";
    public static final String indexHomeDirectory = homeDirectory + "Index/";
    public static final String metadataHomeDirectory = homeDirectory + "Metadata/";

    public static final String dataFilePostfix = ".nkdata";
    public static final String indexFilePostfix = ".nkidx";
    public static final String metadataFilePostfix = ".nkmd";

    public static final Integer maxLengthOfString = 255;
    public static final Integer maxAttributesPerTable = 32;
    public static final Integer bufferSize = 8;

    private BufferManager bufferManager;
    private CatalogManager catalogManager;
    private RecordManager recordManager;

    public NKSql() throws NKInternalException {
        bufferManager = new BufferManager();
        catalogManager = new CatalogManager();
        recordManager = new RecordManager();
        System.out.println("NiKi SQL Server initialized.");
    }

    public void close() throws NKInternalException {
        bufferManager.close();
        catalogManager.close();
        System.out.println("Niki SQL Server closed.");
    }

    public void createTable(String tableName, ArrayList<MetadataAttribute> attributes)
            throws NKInterfaceException {
        Metadata metadata = new Metadata(attributes);
        CatalogManager.sharedInstance.createTable(tableName, metadata);
        System.out.println("Table named " + tableName + " created.");
    }

    public void dropTable(String tableName) throws NKInterfaceException {
        CatalogManager.sharedInstance.dropTable(tableName);
        System.out.println("Table named " + tableName + " dropped.");
    }

    public void createIndex(String indexName, String tableName, String attributeName)
            throws NKInterfaceException {
        System.out.println("Index named " + indexName + " created.");
    }

    public void dropIndex(String indexName) throws NKInterfaceException {
        System.out.println("Index named " + indexName + " dropped.");
    }

    public void insertTuple(Tuple newItem, String tableName) throws NKInterfaceException {
        RecordManager.sharedInstance.insertIntoTable(tableName, newItem);
        System.out.println("Item inserted.");
    }

    public void dropTuple(String tableName,
                          ArrayList<ConditionalAttribute> conditionalAttributes)
            throws NKInterfaceException {
        System.out.println("Tuple dropped.");
    }

    public ArrayList<Tuple> select(String tableName, ArrayList<String> attributeNames,
                                   ArrayList<ConditionalAttribute> conditionalAttributes)
            throws NKInterfaceException {
        ArrayList<Tuple> result = RecordManager.sharedInstance.selectInTable(tableName,
                attributeNames, conditionalAttributes);
        System.out.println("Select complete.");
        return result;
    }

}
