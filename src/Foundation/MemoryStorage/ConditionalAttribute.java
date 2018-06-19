package Foundation.MemoryStorage;

import Foundation.Enumeration.CompareCondition;
import Foundation.Enumeration.DataType;
import Foundation.Exception.NKInterfaceException;
import Foundation.Exception.NKInternalException;

public class ConditionalAttribute {

    public String tableName;
    public String attributeName;
    public String comparedConstant;
    public CompareCondition compareCondition;

    private DataType dataType;

    public ConditionalAttribute(String tableName, String attributeName,
                                String comparedConstant, CompareCondition compareCondition) {
        this.tableName = tableName;
        this.attributeName = attributeName;
        this.comparedConstant = comparedConstant;
        this.compareCondition = compareCondition;

        // Actually, the data type should be looked for in the metadata, this is incomplete
        this.dataType = DataType.IntegerType;
    }

    public Boolean satisfies(String dataItem) throws NKInterfaceException {
        if (this.dataType == DataType.IntegerType) {
            Integer data;
            Integer constant;
            try {
                data = Integer.parseInt(dataItem);
                constant = Integer.parseInt(comparedConstant);
            } catch (Exception exception) {
                throw new NKInterfaceException(dataItem + " isn't it's defined type");
            }
            return compare(data, constant);
        } else if (this.dataType == DataType.FloatType) {
            Float data;
            Float constant;
            try {
                data = Float.parseFloat(dataItem);
                constant = Float.parseFloat(comparedConstant);
            } catch (Exception exception) {
                throw new NKInterfaceException(dataItem + " isn't it's defined type");
            }
            return compare(data, constant);
        } else {
            return compare(dataItem, comparedConstant);
        }
    }

    private <Type extends Comparable<? super Type>> Boolean compare(Type a, Type b) {
        switch (this.compareCondition) {
            case EqualTo: return a.compareTo(b) == 0;
            case LessThan: return a.compareTo(b) < 0;
            case GreaterThan: return a.compareTo(b) > 0;
            case NoLessThan: return a.compareTo(b) >= 0;
            case NoGreaterThan: return a.compareTo(b) <= 0;
            case NotEqualTo: return a.compareTo(b) != 0;
        }
        return false;
    }

}
