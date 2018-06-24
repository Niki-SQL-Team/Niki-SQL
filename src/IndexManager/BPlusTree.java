package IndexManager;

import java.util.Vector;

import java.io.*;
import BufferManager.*;
import Foundation.Blocks.*;
import Foundation.Enumeration.*;
import Foundation.Exception.NKInternalException;
import Foundation.MemoryStorage.*;

//还没有做异常处理
//B+树
public class BPlusTree{
	public BPlusTreePointer root;
	private DataType ElementType;
	private BufferManager bufferManager;
	private String identifier;
	public Integer blockIndexCount;
	private byte[] markerBuffer;
	private Converter converterOfTree;
	//构造函数
	public BPlusTree(DataType Type, BPlusTreePointer root, String table, String attribute, Integer nodeCount){
		this.root = root;
		this.ElementType = Type;
		this.bufferManager = BufferManager.sharedInstance;
		this.identifier = "index" + "_" + table + "_" + attribute;
		this.blockIndexCount = nodeCount;
		this.markerBuffer = null;
		this.converterOfTree = new Converter();
	}

	//插入
    //一个触发函数触发
    public void insertKey(byte[] element, BPlusTreePointer elementPointer){
	    //注意这里要考虑root节点此时满了的情况
		BPlusTreePointer res = insertKeyForNode(element, elementPointer, getTreeNode(this.root));
		if(res == null)
			return;
		else{
			BPlusTreeBlock newRoot = new BPlusTreeBlock(identifier, this.ElementType, ++this.blockIndexCount, false);
			newRoot.insert(this.root, getTreeNode(res).getAttribute(0), res);
			//System.out.println(converterOfTree.convertToInteger(getTreeNode(res).getAttribute(0)));
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
			BPlusTreeBlock newNode = new BPlusTreeBlock(identifier, this.ElementType, ++this.blockIndexCount, node.isLeafNode);
			int mid = node.markerCapacity/2;
			if(node.isLeafNode){//叶节点信息转移
				for(int i = mid; i < node.markerCapacity ; i++){//这里！检测用代码添加
					newNode.insert(node.getAttribute(mid), node.getPointer(mid));
					try{
						node.remove(node.getAttribute(mid), true);
					}
					catch(NKInternalException exception){
						System.out.print("[BPlusTree: 84] try to delete ");
						System.out.println(converterOfTree.convertToInteger(node.getAttribute(mid)));
						exception.describe();
					}
				}
				CompareCondition comp = myCompare(element, newNode.getAttribute(0));
				//System.out.println(converterOfTree.convertToInteger(newNode.getAttribute(0)));
				if(comp == CompareCondition.LessThan || comp == CompareCondition.EqualTo)
					node.insert(element, elementPointer);
				else
					newNode.insert(element, elementPointer);
				//尾指针连接
				newNode.setTailPointer(node.getTailPointer().blockIndex);
				node.setTailPointer(newNode.index);
				bufferManager.storeBlock(newNode);
				bufferManager.storeBlock(node);

				BPlusTreePointer result = new BPlusTreePointer(newNode.index);
				return result;
			}
			else{//非叶节点的信息转移
				//返回值不方便返回两个们这里约定，转移时会多转移一个路标到新节点的第一个，当在上一层进行插入是，对子节点判断是否删去，以此传递路标
				for(int i = mid; i <node.markerCapacity; i++){
					newNode.insert(node.getPointer(mid), node.getAttribute(mid), node.getPointer(mid+1));
					try{
						node.remove(node.getAttribute(mid), true);
					}
					catch(NKInternalException exception){
						exception.describe();
					}

				}
				BPlusTreeBlock returnNode = getTreeNode(res);
				CompareCondition comp = myCompare(newNode.getAttribute(0), getTreeNode(res).getAttribute(0));
				if(comp == CompareCondition.LessThan || comp == CompareCondition.EqualTo)
					newNode.insert(returnNode.getAttribute(0), res);
				else
					node.insert(returnNode.getAttribute(0), res);
				try{
					returnNode.remove(returnNode.getAttribute(0), false);//删去儿子那个多余的路标
				}
				catch(NKInternalException exception){
					exception.describe();
				}

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
				rightInsert(getTreeNode(res).getAttribute(0), res, node);
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

	//属性的pointer修改，基本和search一样
	public BPlusTreePointer rePoint(byte[] target, BPlusTreePointer newPointer){
		return rePointForNode(target, newPointer, getTreeNode(this.root));
	}

	private BPlusTreePointer rePointForNode(byte[] target,  BPlusTreePointer newPointer, BPlusTreeBlock node ){
		if(node.isLeafNode){//如果搜索到了叶节点
			Integer index = node.searchIndexFor(target, true);
			byte[] toBeRemoved = node.getAttribute(index);
			try{
				node.remove(toBeRemoved, true);
			}catch(NKInternalException e){
				e.describe();
			}
			node.insert(toBeRemoved, newPointer);

			 return  newPointer;
		}
		else{//没有搜索到叶节点
			return rePointForNode(target, newPointer, getTreeNode(node.searchFor(target)));
		}
	}

	//范围查找
	//默认返回结果为闭区间查找
	public Vector<BPlusTreePointer> searchKeyInRange(byte[] startKey, byte[] endKey, boolean leftEqual, boolean rightEqual){//闭区间查找
		if(startKey == null){//左侧无限制
			BPlusTreeBlock currentNode = getTreeNode((this.root));
			while(!currentNode.isLeafNode){//找到最左侧的block
				currentNode = getTreeNode(currentNode.getPointer(0));
			}

			Vector<BPlusTreePointer> result = new Vector<BPlusTreePointer>();
			while(currentNode != null) {//从最左侧的block向右侧遍历
				for (int i = 0; i < currentNode.currentSize; i++) {
					CompareCondition res = myCompare(endKey, currentNode.getAttribute(i));
					if (res == CompareCondition.LessThan || (res == CompareCondition.EqualTo) && rightEqual)//如果一个元素小于等于右边界，加入结果
						result.addElement(currentNode.getPointer(i));
					else//否则表明遇到边界，返回结果
						return result;
				}
				currentNode = getTreeNode(currentNode.getTailPointer());//指向下一个block
			}
			return result;//用来解决有边界比现有元素都大的情况
		}else{//左侧有限制
			BPlusTreeBlock currentNode = getTreeNode(this.root);
			while(!currentNode.isLeafNode){//根据startkey确定位置
				currentNode = getTreeNode(currentNode.searchFor(startKey));
			}
			
			Vector<BPlusTreePointer> result = new Vector<BPlusTreePointer>();
			while(currentNode != null){
				for(int i = 0; i < currentNode.currentSize; i++){//从这个block向右侧查找
					if(endKey == null){
						CompareCondition res =  myCompare(startKey, currentNode.getAttribute(i));
						if(res == CompareCondition.GreaterThan || (res == CompareCondition.EqualTo) && leftEqual)
							result.addElement(currentNode.getPointer(i));
					}
					else{
						CompareCondition withStartKey = myCompare(startKey, currentNode.getAttribute(i));
						CompareCondition withEndKey = myCompare(endKey, currentNode.getAttribute(i));
						boolean noLessThanLeft = (withStartKey == CompareCondition.GreaterThan || (withStartKey == CompareCondition.EqualTo) && leftEqual);
						boolean noBiggerThanRight = (withEndKey == CompareCondition.LessThan || (withEndKey == CompareCondition.EqualTo) && rightEqual);
						if(noLessThanLeft && noBiggerThanRight)
							result.addElement(currentNode.getPointer(i));
						else if(!noBiggerThanRight) return result;
					}
				}
				currentNode = getTreeNode(currentNode.getTailPointer());
			}
			return result;
		}
	}

	//删除
	public void deleteKey(byte[] element){
        //触发函数
        //注意root要被merge的情况
		BPlusTreeBlock treeRoot = (BPlusTreeBlock)bufferManager.getBlock(identifier, this.root.blockIndex);
		Integer res = deleteKeyForNode(element, treeRoot);
		this.markerBuffer = null;
		if(treeRoot.currentSize == 0){
			BPlusTreePointer newRoot = treeRoot.getPointer(0);
			this.root = newRoot;
			bufferManager.removeBlock(identifier,treeRoot.index);
		}

	}
	private int deleteKeyForNode(byte[] element, BPlusTreeBlock node){
		//叶节点就是根节点
		if(node.isLeafNode && root.blockIndex == node.index){
			try{
				node.remove(element, true);
				return 0;
			}
			catch(NKInternalException exception){
				exception.describe();
			}
		}

		//正常的叶节点
		if(node.isLeafNode){
			//删一下没事
			if(node.currentSize >= node.markerCapacity / 2){
				Integer indexToBeDeleted = node.searchIndexFor(element, true);

				//删除的是第一个元素需要更新父亲们的路标
				if(indexToBeDeleted == 0){
					try{
						node.remove(element, true);
					}
					catch(NKInternalException exception){
						exception.describe();
					}
					bufferManager.storeBlock(node);
					//递归处理
					return 2;//2 -> FirstDeleteHandler
				}
				else{
					try{
						node.remove(element, true);
						return 0;
					}
					catch(NKInternalException exception){
						exception.describe();
					}
					bufferManager.storeBlock(node);
					return 0;
				}
			}
			//少于一半，要考虑一些调整
			else{
				//需要访问兄弟，递归到上一层处理。
				try{
					node.remove(element,true);
				}
				catch(NKInternalException e){
					e.describe();;
				}
				return 1;//1 -> sonNodeElementTooFew
				//借左节点
				//借右节点
				//没得借，递归合并
			}
		}

		//递归调用
		BPlusTreeBlock next = (BPlusTreeBlock)bufferManager.getBlock(identifier, node.searchFor(element).blockIndex);
		int res = deleteKeyForNode(element, next);

		//递归过程，非叶节点处理
		if(res == 0) return 0;
		if(res == 1){//叶子节点提示：该叶子节点元素数目过少

			Integer sonNodePointerIndex = node.searchIndexFor(element, true);
			BPlusTreeBlock sonNode = (BPlusTreeBlock) bufferManager.getBlock(identifier, node.getPointer(sonNodePointerIndex).blockIndex);
			BPlusTreeBlock leftSon = null, rightSon = null;
			boolean isFromLeaf = sonNode.isLeafNode;
			if(sonNodePointerIndex != 0) leftSon = (BPlusTreeBlock)bufferManager.getBlock(identifier, node.getPointer(sonNodePointerIndex - 1).blockIndex);
			if(sonNodePointerIndex != node.markerCapacity) rightSon = (BPlusTreeBlock)bufferManager.getBlock(identifier, node.getPointer(sonNodePointerIndex + 1).blockIndex);

			if(leftSon != null && leftSon.currentSize >= leftSon.markerCapacity / 2){//左儿子送元素
				BPlusTreePointer presentPointer = leftSon.getPointer(leftSon.currentSize);
				try{
					leftSon.remove(leftSon.getAttribute(leftSon.currentSize-1), true);
				}catch(NKInternalException e){
					e.describe();
				}
				if(isFromLeaf)
					sonNode.insert(leftSon.getAttribute(leftSon.currentSize - 1), presentPointer);
				else{
					BPlusTreePointer right = sonNode.getPointer(0);
					BPlusTreeBlock sonSon = (BPlusTreeBlock)bufferManager.getBlock(identifier, right.blockIndex);
					byte [] newMarker = sonSon.getAttribute(0);
					sonNode.insert(presentPointer, newMarker, right);
				}

				bufferManager.storeBlock(leftSon);
				bufferManager.storeBlock(sonNode);



				//下面修改node的路标啊,copy 了 res == 2 的情况
				Integer index = node.searchIndexFor(element, true);
				BPlusTreePointer sonNodePointer = node.searchFor(element);
				sonNode = (BPlusTreeBlock)bufferManager.getBlock(identifier,sonNodePointer.blockIndex);
				BPlusTreePointer left = node.getPointer(index - 1);
				byte[] newMarker = sonNode.getAttribute(0);
				byte[] toBeDeleted = node.getAttribute(index - 1);
				try{
					node.remove(toBeDeleted, false);
				}catch(NKInternalException e){
					e.describe();
				}
				node.insert(left, newMarker, sonNodePointer);
				bufferManager.storeBlock(node);
				return 0;
			}
			else if(rightSon != null && rightSon.currentSize >= rightSon.markerCapacity / 2){//右儿子送元素
				BPlusTreePointer presentPointer = rightSon.getPointer(0);
				byte[] newMarker = rightSon.getAttribute(0);
				try{
					rightSon.remove(newMarker, false);
				}catch(NKInternalException e){
					e.describe();
				}
				if(isFromLeaf)
					sonNode.insert(newMarker,presentPointer);
				else{
					BPlusTreePointer left = sonNode.getPointer(sonNode.currentSize);
					sonNode.insert(left, newMarker, presentPointer);
				}
				bufferManager.storeBlock(sonNode);
				bufferManager.storeBlock(rightSon);

				//对于右面的节点，要递归处理路标
				BPlusTreePointer sonNodePointer = node.searchFor(element);
				Integer index = node.searchIndexFor(element,true);
				byte[] toBeDeleted = node.getAttribute(index);
				BPlusTreePointer right = node.getPointer(index + 1);
				try{
					node.remove(toBeDeleted, true);
				}
				catch(NKInternalException e){
					e.describe();
				}
				node.insert(sonNodePointer, rightSon.getAttribute(0), right);
				bufferManager.storeBlock(node);
				return 0;
			}
			else{//需要合并，对叶子节点
				BPlusTreeBlock destinationNode, sourceNode;
				if(leftSon != null){//和左儿子合并
					destinationNode = leftSon;
					sourceNode = sonNode;
				}
				else if(rightSon != null){//和右儿子合并
					destinationNode = sonNode;
					sourceNode = rightSon;
				}
				else{
					return 404;
				}

				if(isFromLeaf){
					for(int i = 0; i < sourceNode.currentSize; i++){
						destinationNode.insert(sourceNode.getAttribute(i), sourceNode.getPointer(i));
					}
				}
				else{
					BPlusTreePointer leftLast = destinationNode.getPointer(destinationNode.currentSize);
					BPlusTreePointer rightFirst = sourceNode.getPointer(0);
					BPlusTreeBlock rightFirstBlock =  (BPlusTreeBlock)(bufferManager.getBlock(identifier,rightFirst.blockIndex));
					byte[] theMarker = rightFirstBlock.getAttribute(0);
					destinationNode.insert(leftLast, theMarker, rightFirst);
					for(int i = 0; i < sourceNode.currentSize; i++){
						destinationNode.insert(sourceNode.getPointer(i), sourceNode.getAttribute(i), sourceNode.getPointer(i+1));
					}
				}


				byte[] toBeDeleted = node.getAttribute(node.searchIndexFor(sourceNode.getAttribute(0), true) - 1);
				try{
					node.remove(toBeDeleted, true);
				}catch(NKInternalException e){
					e.describe();
				}

				if(isFromLeaf)
					destinationNode.setTailPointer(sourceNode.getTailPointer().blockIndex);

				bufferManager.removeBlock(identifier, sourceNode.index);
				bufferManager.storeBlock(destinationNode);
				if(node.currentSize < node.markerCapacity / 2)
					return 1;
				else
					return 0;
			}
		}
		else if(res == 2){//删除了，或者修改了对应叶子节点的第一个元素
			Integer index = node.searchIndexFor(element, true);
			BPlusTreePointer sonNodePointer = node.searchFor(element);
			BPlusTreeBlock sonNode = (BPlusTreeBlock)bufferManager.getBlock(identifier, sonNodePointer.blockIndex);
			if(index == 0){
				this.markerBuffer = sonNode.getAttribute(0);
				return 3;//非叶节点提示，最下面有一个叶节点的第一个改了，他是我的最左边指针，传给上面看，直到找到不是最左指针
			}
			//否则，可以改这个节点信息，然后over
			BPlusTreePointer left = node.getPointer(index - 1);
			byte[] newMarker = sonNode.getAttribute(0);
			byte[] toBeDeleted = node.getAttribute(index - 1);
			try{
				node.remove(toBeDeleted, false);
			}catch(NKInternalException e){
				e.describe();
			}
			node.insert(left, newMarker, sonNodePointer);
			bufferManager.storeBlock(node);
			return 0;
		}
		else if(res == 3){//递归，知道找到对应儿子节点不是node的第一个指针
			BPlusTreePointer sonNodePointer = node.searchFor(element);
			Integer index = node.searchIndexFor(element,true);
			if(index == 0) return 3;
			byte[] toBeDeleted = node.getAttribute(index - 1);
			BPlusTreePointer left = node.getPointer(index - 1);
			try{
				node.remove(toBeDeleted, false);
			}
			catch(NKInternalException e){
				e.describe();
			}
			node.insert(left, this.markerBuffer, sonNodePointer);
			this.markerBuffer = null;
			bufferManager.storeBlock(node);
			return 0;
		}
		else{
			return 404;
		}

		//曾经的旧代码
    }

    public Vector<BPlusTreePointer> findAll(){
		Vector<BPlusTreePointer> res = new Vector<BPlusTreePointer>();

		BPlusTreeBlock currentNode = getTreeNode((this.root));
		while(!currentNode.isLeafNode){//找到最左侧的block
			currentNode = getTreeNode(currentNode.getPointer(0));
		}

		do{
			for(int i = 0;i <currentNode.currentSize; i++){
				res.add(currentNode.getPointer(i));
				//System.out.println(currentNode.getPointer(i).blockIndex);
			}
			currentNode = getTreeNode(currentNode.getTailPointer());
		}while(currentNode != null);

		return res;
	}

    //私有封装函数
	//key left, node right
	private CompareCondition myCompare(byte[] key2, byte[] key1){
		Converter convertUtility = new Converter();
		if(this.ElementType == DataType.IntegerType){
			Integer left = convertUtility.convertToInteger(key1);
			Integer right = convertUtility.convertToInteger(key2);
			return generateCompareResult(left.equals(right), left > right, left < right);
		}
		else if(this.ElementType == DataType.FloatType){
			Float left = convertUtility.convertToFloat(key1);
			Float right = convertUtility.convertToFloat(key2);
			return generateCompareResult(left.equals(right) , left > right, left < right);
		}
		else if(this.ElementType == DataType.StringType) {
			String left = convertUtility.convertToString(key1);
			String right = convertUtility.convertToString(key2);
			Integer res = left.compareTo(right);
			if(res == 0) return CompareCondition.EqualTo;
			else if(res > 0) return CompareCondition.GreaterThan;
			else return CompareCondition.LessThan;
		}

		return null;
	}

	private CompareCondition generateCompareResult(boolean equal, boolean greater, boolean less){
		if(equal) return CompareCondition.EqualTo;
		else if(greater) return CompareCondition.GreaterThan;
		else if(less) return CompareCondition.LessThan;
		return null;
	}

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