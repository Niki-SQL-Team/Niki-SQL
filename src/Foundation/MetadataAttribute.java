package Foundation;

public class MetadataAttribute {

    public String attributeName;
    public DataType dataType;
    public Integer length;     // This attribute is only for StringType
    public Boolean isPrimaryKey;
    public Boolean isUnique;

    public MetadataAttribute(String attributeName, DataType dataType, Integer length,
                             Boolean isPrimaryKey, Boolean isUnique) {
        this.attributeName = attributeName;
        this.dataType = dataType;
        this.length = length;
        this.isPrimaryKey = isPrimaryKey;
        this.isUnique = isUnique;
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
    }

}
