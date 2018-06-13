package Top;

import Foundation.Exception.*;
import Foundation.MemoryStorage.*;

import java.util.ArrayList;

public class NKSql {

    public static final String homeDirectory = "/Users/licanchen/Desktop/Niki SQL/DB Files/";
    public static final String dataHomeDirectory = homeDirectory + "Data/";
    public static final String indexHomeDirectory = homeDirectory + "Index/";
    public static final String metadataHomeDirectory = homeDirectory + "Metadata/";

    public static final String dataFilePostfix = ".nkdata";
    public static final String indexFilePostfix = ".nkindex";
    public static final String metadataFilePostfix = ".nkmetadata";

    public static final Integer maxLengthOfString = 255;

    public NKSql() throws NKInternalException {
        System.out.println("Top.NKSql initialized.");
    }

    public void close() throws NKInternalException {
        System.out.println("Top.NKSql closed.");
    }

    public void createTable(String tableName, ArrayList<MetadataAttribute> attributes)
            throws NKInterfaceException {
        System.out.println("Table named " + tableName + " created.");
    }

    public void dropTable(String tableName) throws NKInterfaceException {
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
        System.out.println("Select complete.");
        return null;
    }

}