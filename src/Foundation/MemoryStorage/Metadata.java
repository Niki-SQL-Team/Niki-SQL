package Foundation.MemoryStorage;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class Metadata {

    public Integer numberOfAttributes;
    public Vector<Integer> attributeOffsetArray;
    private Map<Integer, String> attributeName;
    private Map<String, MetadataAttribute> metadataAttributes;

    public Metadata(Vector<MetadataAttribute> metadataAttributes) {
        this.numberOfAttributes = metadataAttributes.size();
        setMetadataAttributes(metadataAttributes);
        setAttributeName(metadataAttributes);
        setAttributeOffsetArray();
    }

    public MetadataAttribute getMetadataAttributeNamed(String attributeName) {
        return metadataAttributes.get(attributeName);
    }

    public MetadataAttribute getMetadataAttributeAt(Integer index) {
        String attributeName = this.attributeName.get(index);
        return this.metadataAttributes.get(attributeName);
    }

    public Integer getTupleLength() {
        Integer length = 0;
        for (MetadataAttribute attribute : this.metadataAttributes.values()) {
            length += attribute.getAttributeLength();
        }
        return length;
    }

    public Integer getTupleOffsetAt(Integer index) {
        return this.attributeOffsetArray.get(index);
    }

    private void setAttributeName(Vector<MetadataAttribute> attributes) {
        this.attributeName = new LinkedHashMap<>();
        for (int i = 0; i < attributes.size(); i ++) {
            this.attributeName.put(i, attributes.get(i).attributeName);
        }
    }

    private void setMetadataAttributes(Vector<MetadataAttribute> attributes) {
        this.metadataAttributes = new LinkedHashMap<>();
        for (MetadataAttribute attribute : attributes) {
            this.metadataAttributes.put(attribute.attributeName, attribute);
        }
    }

    private void setAttributeOffsetArray() {
        this.attributeOffsetArray = new Vector<>();
        Integer currentOffset = 0;
        for (MetadataAttribute attribute : this.metadataAttributes.values()) {
            this.attributeOffsetArray.add(currentOffset);
            currentOffset += attribute.getAttributeLength();
        }
    }

}
