package IndexManager;

import Foundation.Enumeration.DataType;

import java.io.Serializable;

public class Index implements Serializable {//包含index基本信息，提供给外部作为元数据
    public String table;
    public String attribute;
    public DataType dataType;
    public Integer blockOfTheIndexTree;
    public Integer currentNodeCount;
    public Integer stringLength;
}
