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
    }

    public Boolean satisfies(String dataItem) throws NKInterfaceException {
        if (this.dataType == DataType.IntegerType) {
            try {
                Integer data = Integer.parseInt(dataItem);
                Integer constant = Integer.parseInt(comparedConstant);
                return compare(data, constant);
            } catch (Exception exception) {
                throw new NKInterfaceException(dataItem + " isn't it's defined type");
            }
        } else if (this.dataType == DataType.FloatType) {
            try {
                Float data = Float.parseFloat(dataItem);
                Float constant = Float.parseFloat(comparedConstant);
                return compare(data, constant);
            } catch (Exception exception) {
                throw new NKInterfaceException(dataItem + " isn't it's defined type");
            }
        } else {
            return compare(dataItem, comparedConstant);
        }
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    private <Type extends Comparable<? super Type>> Boolean compare(Type a, Type b) {
        switch (this.compareCondition) {
            case EqualTo: return a.compareTo(b) == 0;
            case LessThan: return a.compareTo(b) < 0;
            case GreaterThan: return a.compareTo(b) > 0;
            case NoLessThan: return a.compareTo(b) >= 0;
            case NoGreaterThan: return a.compareTo(b) <= 0;
            case NotEqualTo: return a.compareTo(b) != 0;
            case All: return true;
        }
        return false;
    }

}
