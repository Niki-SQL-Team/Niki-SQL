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
	private BPlusTreeBlock root;
	private DataType ElementType;

	//构造函数
	BPlusTree(DataType Type, BPlusTreeBlock root){
		this.root = root;
		this.ElementType = Type;
	}

	//插入
	private void insertKeyForNode(byte[] element, BPlusTreePointer elementPointer, BPlusTreeBlock node){
		if(node.isLeafNode){//递归到叶节点

		}
		else{//在非叶节点递归

		}
	}
	//精确查找
	public BPlusTreePointer searchKey(byte[] target){
		return searchKeyForNode(target, this.root);
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
			BPlusTreeBlock currentNode = this.root;
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

	}
}