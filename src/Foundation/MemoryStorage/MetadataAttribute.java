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
    public String indexName;

    public MetadataAttribute(String attributeName, DataType dataType, Integer length,
                             Boolean isPrimaryKey, Boolean isUnique) {
        this.attributeName = attributeName;
        this.dataType = dataType;
        this.length = length;
        this.isPrimaryKey = isPrimaryKey;
        this.isUnique = isUnique;
        if (this.isPrimaryKey) {
            this.isUnique = true;
        }
        this.isIndexed = this.isUnique;
        this.indexName = this.attributeName;
    }

    public MetadataAttribute(String attributeName, DataType dataType, Boolean isPrimaryKey,
                             Boolean isUnique) throws NKInternalException {
        if (dataType == DataType.StringType) {
            throw new NKInternalException("StringType requires length.");
        }
        this.attributeName = attributeName;
        this.dataType = dataType;
        this.length = 0;
        this.isPrimaryKey = isPrimaryKey;
        this.isUnique = isUnique;
        if (this.isPrimaryKey) {
            this.isUnique = true;
        }
        this.isIndexed = this.isUnique;
        this.isIndexed = isUnique;
        this.indexName = this.attributeName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public void setAsPrimaryKey() {
        this.isPrimaryKey = true;
        this.isUnique = true;
        this.isIndexed = true;
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
