package Foundation.MemoryStorage;

import Foundation.Enumeration.DataType;

import java.util.Objects;
import java.util.Vector;

public class BPlusTreeMetadata extends Metadata {

    public BPlusTreeMetadata(DataType dataType, Integer length) {
        super(Objects.requireNonNull(getAttributes(dataType, length)));
    }

    public BPlusTreeMetadata(DataType dataType) {
        super(Objects.requireNonNull(getAttributes(dataType)));
    }

    private static Vector<MetadataAttribute> getAttributes(DataType dataType, Integer length) {
        try {
            MetadataAttribute indexPointer = new MetadataAttribute("Index Pointer",
                    DataType.IntegerType, false, false, false);
            MetadataAttribute offsetPointer = new MetadataAttribute("Offset Pointer",
                    DataType.IntegerType, false, false, false);
            MetadataAttribute marker = new MetadataAttribute("Marker", dataType, length,
                    false, false, false);
            Vector<MetadataAttribute> attributes = new Vector<>();
            attributes.add(indexPointer);
            attributes.add(offsetPointer);
            attributes.add(marker);
            return attributes;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private static Vector<MetadataAttribute> getAttributes(DataType dataType) {
        try {
            MetadataAttribute indexPointer = new MetadataAttribute("Index Pointer",
                    DataType.IntegerType, false, false, false);
            MetadataAttribute offsetPointer = new MetadataAttribute("Offset Pointer",
                    DataType.IntegerType, false, false, false);
            MetadataAttribute marker = new MetadataAttribute("Marker", dataType,
                    false, false, false);
            Vector<MetadataAttribute> attributes = new Vector<>();
            attributes.add(indexPointer);
            attributes.add(offsetPointer);
            attributes.add(marker);
            return attributes;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

}
