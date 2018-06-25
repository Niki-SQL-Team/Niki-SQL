package IndexManager;

import java.io.*;
import java.util.Vector;
import BufferManager.*;
import CatalogManager.*;
import Foundation.Blocks.BPlusTreeBlock;
import Foundation.Blocks.Converter;
import Foundation.Enumeration.DataType;
import Foundation.MemoryStorage.BPlusTreePointer;
import RecordManager.*;

//index管理
public class IndexManager{
    public static IndexManager sharedInstance;
    private BPlusTree workOn;//工作对象
    private String identifier;
    private Integer blockCount;
    //构造函数
    public  IndexManager(){
        sharedInstance = this;
    }

    //Index Manager的一些列的操作针对一个给定的索引，使用Index Manager其他功能时（除非要创建一个新索引），务必要先调用这个函数设定一个给定的索引
    private void managerInitialize(Index targetObject){
        BPlusTreePointer root = new BPlusTreePointer(targetObject.blockOfTheIndexTree);
        BPlusTree bTree = new BPlusTree(targetObject.dataType, root, targetObject.table, targetObject.attribute, targetObject.currentNodeCount, targetObject.stringLength);
        this.workOn = bTree;
        this.identifier = "index_" + targetObject.table + "_" +targetObject.attribute;
        this.blockCount = targetObject.currentNodeCount;
    }

    //创建新的索引，返回Index类型， 通知调整workOn
    public Index createBlankIndex(String table, String attribute, DataType dataType){// 非String
        return createBlankIndex(table,attribute,dataType, -1);
    }

    public Index createBlankIndex(String table, String attribute, DataType dataType, Integer stringlength){//建成空的B+树
        BufferManager bufferManager = BufferManager.sharedInstance;
        String identifier = "index" + "_" +  table + "_" + attribute;
        BPlusTreeBlock myRoot;
        if(dataType == DataType.StringType)
            myRoot = new BPlusTreeBlock(identifier, dataType, stringlength,0, true);
        else
            myRoot = new BPlusTreeBlock(identifier, dataType,0, true);
        myRoot.setTailPointer(-1);
        bufferManager.storeBlock(myRoot);
        BPlusTreePointer rootPointer = new BPlusTreePointer(0);
        BPlusTree bTree = new BPlusTree(dataType, rootPointer,table,attribute,0, stringlength);
        workOn = bTree;

        Index res = new Index();
        res.attribute = attribute;
        res.blockOfTheIndexTree = 0;
        res.currentNodeCount = 0;
        res.dataType = dataType;
        res.table = table;
        return res;
    }



    //删除workOn整个索引
    public boolean	dropWholeIndex(Index index){
        managerInitialize(index);

        BufferManager bufferManager = BufferManager.sharedInstance;
        for(int i = 0; i < this.blockCount; i++){
            bufferManager.removeBlock(identifier, i);
        }

        this.blockCount = -1;
        this.identifier = null;
        this.workOn = null;
        return true;
    }

    //等值查找,找不到返回的是null
    public BPlusTreePointer searchEqually(Index index, byte[] key){
        managerInitialize(index);

        return this.workOn.searchKey(key);
    }

    //范围查找,单侧查找的话，端点传入null；后两个bool判断是否包含端点值
    public Vector<BPlusTreePointer> searchRangely(Index index, byte[] leftKey, byte[] rightKey, boolean leftEqual, boolean rightEual){
        managerInitialize(index);

        Vector<BPlusTreePointer> res;
        res = this.workOn.searchKeyInRange(leftKey, rightKey, leftEqual, rightEual);

        return res;
    }

    //插入新值
    public void insertNewKey(Index index, byte[] key, BPlusTreePointer pointer){
        managerInitialize(index);
        workOn.insertKey(key, pointer);

        //更新index
        index.currentNodeCount = workOn.blockIndexCount;
        index.blockOfTheIndexTree = workOn.root.blockIndex;
    }

    //删除一个值
    public void removeKey(Index index, byte[] key){
        managerInitialize(index);
        workOn.deleteKey(key);

        //更新index
        index.currentNodeCount = workOn.blockIndexCount;
        index.blockOfTheIndexTree = workOn.root.blockIndex;
    }

    //更新某个属性的指针
    public void setNewPointerFor(Index index, byte[] key, BPlusTreePointer newPointer){
        managerInitialize(index);

        workOn.rePoint(key, newPointer);
    }

    //获取所有属性值的指针，返回结果会是有序的
    public Vector<BPlusTreePointer> getAllPointer(Index index){
        managerInitialize(index);

        return workOn.findAll();
    }

}