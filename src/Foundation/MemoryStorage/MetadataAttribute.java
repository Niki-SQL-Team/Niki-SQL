package Foundation.MemoryStorage;

import Foundation.Enumeration.*;
import Foundation.Exception.*;

import java.io.Serializable;

public class MetadataAttribute implements Serializable {

    public String attributeName;
    public DataType dataType;
    public Integer length;     // This attribute is only for StringType
    public Boolean isPrimaryKey;
    public Boolean isUnique;
    public Boolean isIndexed;

    public MetadataAttribute(String attributeName, DataType dataType, Integer length,
                             Boolean isPrimaryKey, Boolean isUnique, Boolean isIndexed) {
        this.attributeName = attributeName;
        this.dataType = dataType;
        this.length = length;
        this.isPrimaryKey = isPrimaryKey;
        this.isUnique = isUnique;
        this.isIndexed = isIndexed;
    }

    public MetadataAttribute(String attributeName, DataType dataType, Boolean isPrimaryKey,
                             Boolean isUnique, Boolean isIndexed) throws NKInternalException {
        if (dataType == DataType.StringType) {
            throw new NKInternalException("StringType requires length.");
        }
        this.attributeName = attributeName;
        this.dataType = dataType;
        this.length = 0;
        this.isPrimaryKey = isPrimaryKey;
        this.isUnique = isUnique;
        this.isIndexed = isIndexed;
    }

    public Integer getAttributeLength() {
        switch (this.dataType) {
            case IntegerType: return Integer.SIZE / 8;
            case FloatType: return Float.SIZE / 8;
            case StringType: return this.length;
        }
        return -1;
    }

}