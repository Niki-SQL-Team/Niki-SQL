package IndexManager;

import java.util.Vector;
import java.util.concurrent.ConcurrentMap;

import BufferManager.*;
import CatalogManager.*;
import Foundation.Block.BPlusTreeBlock;
import Foundation.Enumeration.CompareCondition;
import Foundation.Enumeration.DataType;
import Foundation.MemoryStorage.BPlusTreePointer;
import RecordManager.*;
import Foundation.*;

//B+树
public class BPlusTree{
	private BPlusTreePointer root;
	private DataType ElementType;

	//构造函数
	BPlusTree(DataType Type, BPlusTreePointer root){
		this.root = root;
		this.ElementType = Type;
	}

	//插入
    //一个触发函数触发
    public void insertKey(byte[] element, BPlusTreePointer elementPointer){
	    //注意这里要考虑root节点此时满了的情况
    }
	private BPlusTreePointer insertKeyForNode(byte[] element, BPlusTreePointer elementPointer, BPlusTreeBlock node){
		//在子节点递归
            //如果是叶节点，进入否则部分
            //如果是非叶节点，找到它应该在的子节点，对子节点递归调用函数
            //检查返回值，如果返回值为null，则继续返回null
            //否则
                //将返回的指针，和其所指向的块的第一个元素作为路标插入该节点
                    //如果节点储存信息已经饱和
                    //生成新的右侧节点，将原节点内一般的信息转移至新的节点
                    //插入element
                    //返回新生成节点的指针
                    //如果还能插入
                    //插入，返回null

	}
	//精确查找
	public BPlusTreePointer searchKey(byte[] target){
		return searchKeyForNode(target, this.root.getTreeNode();
	}

	private BPlusTreePointer searchKeyForNode(byte[] target, BPlusTreeBlock node ){
		if(node.isLeafNode){//如果搜索到了叶节点
			return node.searchFor(target);
		}
		else{//没有搜索到叶节点
			searchKeyForNode(target, searchFor(target));
		}
	}

	//范围查找
	public Vector<BPlusTreePointer> searchKeyInRange(byte[] startKey, byte[] endKey){//闭区间查找
		if(startKey == null){//左侧无限制
			BPlusTreeBlock currentNode = this.root.getTreeNode();
			while(!currentNode.isLeafNode){//找到最左侧的block
				currentNode = currentNode.getPointer(0).getTreeNode();
			}

			Vector<BPlusTreePointer> result = new Vector<BPlusTreePointer>();
			while(currentNode != null) {//从最左侧的block向右侧遍历
				for (int i = 0; i < currentNode.currentElements; i++) {
					CompareCondition res = currentNode.compareAttributeAt(endKey, i)
					if (res == CompareCondition.LessThan || res == CompareCondition.EqualTo)
						result.addElement(currentNode.getAtrributePointer(i));
					else
						return result;
				}
				currentNode = currentNode.getTailPointer().getTreeNode();
			}
			return result;
		}else{//左侧有限制
			BPlusTreeNode currentNode = root;
			while(currentNode.isLeafNode != true){//根据startkey确定位置
				currentNode = currentNode.searchFor(startKey).getTreeNode();
			}
			
			Vector<BPlusTreePointer> result = new Vector<BPlusTreePointer>();
			while(currentNode != null){
				for(int i = 0; i < currentNode.currentElements; i++){//从这个block向右侧查找
					if(endKey == null){
						CompareCondition res =  currentNode.compareAttributeAt(startKey, i);
						if(res == CompareCondition.GreaterThan || res == CompareCondition.EqualTo)
							result.addElement(currentNode.getAtrributePointer(i));
					}
					else{
						CompareCondition withStartKey = currentNode.compareAttributeAt(startKey, i);
						CompareCondition withEndKey = currentNode.compareAttributeAt(endKey, i);
						boolean noLessThanLeft = (withStartKey == CompareCondition.GreaterThan || withStartKey == CompareCondition.EqualTo);
						boolean noBiggerThanRight = (withEndKey == CompareCondition.LessThan || withStartKey == CompareCondition.EqualTo);
						if(noLessThanLeft && noBiggerThanRight)
							result.addElement(currentNode.getAtrributePointer(i));
						else if(withEndKey == CompareCondition.GreaterThan) return result;
					}
				}
			}
			return result;
		}
	}

	//删除
	public void deleteKey(byte[] element){
        //触发函数
        //注意root要被merge的情况
	}
	private BPlusTreePointer deleteKeyForNode(byte[] element, BPlusTreeBlock node){
	    //对子节点递归
            //如果是非叶节点，递归调用
    }
}