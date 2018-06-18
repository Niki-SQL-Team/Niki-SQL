package IndexManager;

import java.util.Vector;
import java.util.concurrent.ConcurrentMap;

import BufferManager.*;
import Foundation.Blocks.*;
import Foundation.Enumeration.*;
import Foundation.MemoryStorage.*;

//还没有做异常处理
//B+树
public class BPlusTree{
	private BPlusTreePointer root;
	private DataType ElementType;
	private BufferManager bufferManager;
	private String identifier;
	private Integer blockIndexCount;
	//构造函数
	BPlusTree(DataType Type, BPlusTreePointer root, String table, String attribute, Integer nodeCount){
		this.root = root;
		this.ElementType = Type;
		this.bufferManager = BufferManager.sharedInstance;
		this.identifier = "index" + "_" + table + "_" + attribute;
		this.blockIndexCount = nodeCount;
	}

	//插入
    //一个触发函数触发
    public void insertKey(byte[] element, BPlusTreePointer elementPointer){
	    //注意这里要考虑root节点此时满了的情况
		BPlusTreePointer res = insertKeyForNode(element, elementPointer, getTreeNode(this.root));
		if(res == null)
			return;
		else{
			BPlusTreeBlock newRoot = new BPlusTreeBlock(identifier, this.ElementType, this.blockIndexCount++, false);
			newRoot.insert(this.root, getTreeNode(res).getElement(0), res);
			this.root = new BPlusTreePointer(newRoot.index);
			bufferManager.storeBlock(newRoot);
			return;
		}
    }

	private BPlusTreePointer insertKeyForNode(byte[] element, BPlusTreePointer elementPointer, BPlusTreeBlock node){
		//主要想法：
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
		//非叶节点，向下递归
		if(!node.isLeafNode){
			BPlusTreeBlock next = (BPlusTreeBlock)bufferManager.getBlock(this.identifier ,node.searchFor(element).blockIndex);
			res = insertKeyForNode(element, elementPointer, next);
			if(res == null)
				return null;
		}
		//以下情况是搜做到叶节点，或者以非NULL递归返回到非叶节点
		if(node.currentSize >= node.markerCapacity/*满了*/){
			//叶节点注意连接兄弟指针
			//目前块一分为二，再选择一块插入新的节点
			BPlusTreeBlock newNode = new BPlusTreeBlock(identifier, this.ElementType, this.blockIndexCount++, node.isLeafNode);
			int mid = node.markerCapacity / 2;
			if(node.isLeafNode){//叶节点信息转移
				for(int i = mid; i <node.markerCapacity; i++){
					newNode.insert(node.getElement(i), node.getPointer(i));
					node.remove(node.getElement(i), true);
				}
				CompareCondition comp = newNode.compareAttributeAt(element, 0);
				if(comp == CompareCondition.LessThan || comp == CompareCondition.EqualTo)
					newNode.insert(element, elementPointer);
				else
					node.insert(element, elementPointer);
				//尾指针连接
				newNode.setTailPointer(node.getTailPointer().blockIndex);
				node.setTailPointer(newNode.index);
				bufferManager.storeBlock(newNode);
				bufferManager.storeBlock(node);
			}
			else{//非叶节点的信息转移
				//返回值不方便返回两个们这里约定，转移时会多转移一个路标到新节点的第一个，当在上一层进行插入是，对子节点判断是否删去，以此传递路标
				for(int i = mid; i <node.markerCapacity; i++){
					newNode.insert(node.getPointer(i), node.getElement(i), node.getPointer(i+1));
					node.remove(node.getElement(i), true);
				}
				BPlusTreeBlock returnNode = getTreeNode(res);
				CompareCondition comp = compare(newNode.getElement(0), getTreeNode(res).getElement(0));
				if(comp == CompareCondition.LessThan || comp == CompareCondition.EqualTo)
					newNode.insert(returnNode.getElement(0), res);
				else
					node.insert(returnNode.getElement(0), res);
				returnNode.remove(returnNode.getElement(0), false);//删去儿子那个多余的路标
				bufferManager.storeBlock(newNode);
				bufferManager.storeBlock(node);
			}

		}
		else{/*没有满，插入*/
			if(node.isLeafNode){//在没有满的叶节点插入
				node.insert(element, elementPointer);
				bufferManager.storeBlock(node);
				return null;
			}
			else{//在没有满的非叶节点插入
				//新插入的左指针不动，右指针为res
				rightInsert(getTreeNode(res).getElement(0), res, node);
				bufferManager.storeBlock(node);
				return null;
			}
		}
		return null;//no use
	}
	//精确查找
	public BPlusTreePointer searchKey(byte[] target){
		return searchKeyForNode(target, getTreeNode(this.root));
	}

	private BPlusTreePointer searchKeyForNode(byte[] target, BPlusTreeBlock node ){
		if(node.isLeafNode){//如果搜索到了叶节点
			return node.searchFor(target);
		}
		else{//没有搜索到叶节点
			return searchKeyForNode(target, getTreeNode(node.searchFor(target)));
		}
	}

	//范围查找
	//默认返回结果为闭区间查找
	public Vector<BPlusTreePointer> searchKeyInRange(byte[] startKey, byte[] endKey){//闭区间查找
		if(startKey == null){//左侧无限制
			BPlusTreeBlock currentNode = getTreeNode((this.root));
			while(!currentNode.isLeafNode){//找到最左侧的block
				currentNode = getTreeNode(currentNode.getPointer(0));
			}

			Vector<BPlusTreePointer> result = new Vector<BPlusTreePointer>();
			while(currentNode != null) {//从最左侧的block向右侧遍历
				for (int i = 0; i < currentNode.currentSize; i++) {
					CompareCondition res = currentNode.compareAttributeAt(endKey, i);
					if (res == CompareCondition.LessThan || res == CompareCondition.EqualTo)//如果一个元素小于等于右边界，加入结果
						result.addElement(currentNode.getAtrributePointer(i));
					else//否则表明遇到边界，返回结果
						return result;
				}
				currentNode = getTreeNode(currentNode.getTailPointer());//指向下一个block
			}
			return result;//用来解决有边界比现有元素都大的情况
		}else{//左侧有限制
			BPlusTreeBlock currentNode = getTreeNode(this.root);
			while(currentNode.isLeafNode != true){//根据startkey确定位置
				currentNode = getTreeNode(currentNode.searchFor(startKey));
			}
			
			Vector<BPlusTreePointer> result = new Vector<BPlusTreePointer>();
			while(currentNode != null){
				for(int i = 0; i < currentNode.currentSize; i++){//从这个block向右侧查找
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


    //私有封装函数
    private BPlusTreeBlock getTreeNode(BPlusTreePointer pointer){
		if(pointer == null || pointer.blockIndex < 0)  return null;
		return (BPlusTreeBlock)bufferManager.getBlock(identifier, pointer.blockIndex);
	}

	private BPlusTreeBlock getTreeNode(Integer index){
		if(index < 0) return null;
		return (BPlusTreeBlock)bufferManager.getBlock(identifier, index);
	}

	//插入只需要对右侧更新指针即可，这里对提供的双侧方法包装为单侧
	private void rightInsert(byte[] marker, BPlusTreePointer right, BPlusTreeBlock node){
		BPlusTreePointer left = node.searchFor(marker);
		node.insert(left, marker, right);
	}
}