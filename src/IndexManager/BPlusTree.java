package IndexManager;

import java.util.Vector;
import java.util.concurrent.ConcurrentMap;

import BufferManager.*;
import CatalogManager.*;
import Foundation.Blocks.*;
import Foundation.Enumeration.*;
import Foundation.MemoryStorage.*;
import RecordManager.*;
import Foundation.*;

//需要getElement method
//需要getTreeBlock method
//需要block info trans method
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
    //插入只需要对右侧更新指针即可，这里对提供的双侧方法包装为单侧
    private void rightInsert(byte[] marker, BPlusTreePointer right, BPlusTreeBlock node){
		BPlusTreePointer left = node.searchFor(marker);
		node.insert(left, marker, right);
	}
	private BPlusTreePointer insertKeyForNode(byte[] element, BPlusTreePointer elementPointer, BPlusTreeBlock node){
		//在子节点递归
            //如果是叶节点，进入否则部分
            //如果是非叶节点，找到它应该在的子节点，对子节点递归调用函数
            //检查返回值，如果返回值为null，则继续返回null
            //否则
                //将返回的指针，和其所指向的块的第一个元素作为路标插入该节点
                    //如果节点储存信息已经饱和
                    //生成新的右侧节点，将原节点内一半的信息转移至新的节点
                    //插入element
                    //返回新生成节点的指针
                    //如果还能插入
                    //插入，返回null
		BPlusTreePointer res = null;
		if(!node.isLeafNode){
			BPlusTreeBlock next = node.searchFor(element).getTreeNode();
			res = insertKeyForNode(element, elementPointer, next);
			if(res == null)
				return null;
		}
		//以下情况是搜做到叶节点，或者以非NULL递归返回到非叶节点
		if(node.currentSize >= node.markerCapacity/*满了*/){

		}
		else{/*没有满，插入*/
			if(node.isLeafNode){//在没有满的叶节点插入
				node.insert(element, elementPointer);
				return null;
			}
			else{//在没有满的非叶节点插入
				//新插入的左指针不动，右指针为res
				rightInsert(res.getTreeNode().getElement(), res, node);
				return null;
			}
		}
		return null;//no use
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
	//默认返回结果为闭区间查找
	public Vector<BPlusTreePointer> searchKeyInRange(byte[] startKey, byte[] endKey){//闭区间查找
		if(startKey == null){//左侧无限制
			BPlusTreeBlock currentNode = this.root.getTreeNode();
			while(!currentNode.isLeafNode){//找到最左侧的block
				currentNode = currentNode.getPointer(0).getTreeNode();
			}

			Vector<BPlusTreePointer> result = new Vector<BPlusTreePointer>();
			while(currentNode != null) {//从最左侧的block向右侧遍历
				for (int i = 0; i < currentNode.currentElements; i++) {
					CompareCondition res = currentNode.compareAttributeAt(endKey, i);
					if (res == CompareCondition.LessThan || res == CompareCondition.EqualTo)//如果一个元素小于等于右边界，加入结果
						result.addElement(currentNode.getAtrributePointer(i));
					else//否则表明遇到边界，返回结果
						return result;
				}
				currentNode = currentNode.getTailPointer().getTreeNode();//指向下一个block
			}
			return result;//用来解决有边界比现有元素都大的情况
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