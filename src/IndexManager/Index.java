package IndexManager;

import Foundation.Enumeration.DataType;

public class Index {//包含index基本信息，提供给外部作为元数据
    public String table;
    public String attribute;
    public DataType dataType;
    public Integer blockOfTheIndexTree;
    public Integer currentNodeCount;
}