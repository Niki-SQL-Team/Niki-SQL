package IndexManager;

import java.io.*;
import java.util.Vector;
import BufferManger.*;
import CatalogManger.*;
import RecordManger.*;
import Fundation.*;

//B+树
public class BPlusTree{
	//创建新的B+树到内存中
	
	//以已有的block还原B+树
	
	//插入
	
	//精确查找
	public BPlusTreePointer searchKey(BPlusTreeBlock root, Type target){
		if(root.isLeafNode == true){//如果搜索到了叶节点
			return root.searchFor(target);
		}
		else{//没有搜索到叶节点
			int i = 0;
			for(i = 0; i < root.currentElements; i++){
				if(target.compareTo(root.getElement(i)) == -1){
					sonNode = root.getInternalPointer(i);
					return searchKey(sonNode, target);
				}
			}
			sonNode = root.getInternalPointer(i+1);
			return searchKey(sonNode, target);
		}
	}
	
	//范围查找
	public Vector<BPlusTreePointer> searchKeyInRange(BPlusTreeBlock root, Type startKey, Type endKey){//闭区间查找
		if(startKey == NULL){//左侧无限制
			BPlusTreeBlock currentNode = root;
			while(currentNode.isLeafNode != true){
				currentNode = currentNode.getInternalPointer(0);
			}
			
			Vector<BPlusTreePointer> result = new Vector<BPlusTreePointer>();
			for(int i = 0; i < currentNode.currentElements; i++){//第一个block
				if(currentNode.getElement(i) <= endKey)
					result.addElement(currentNode.getAtrributePointer(i));
				else
					return result;
			}
			
			currentNode = currentNode.getTailPointer();//更加后面的block
			while(currentNode != NULL){
				for(int i = 0; i < currentNode.currentElements; i++){
					if(currentNode.getElement(i) <= endKey)
						result.addElement(currentNode.getAtrributePointer(i));
					else
						return result;
				}
				currentNode = currentNode.getTailPointer();
			}	
			
			return result;
			
		}else{//左侧有限制
			BPlusTreeNode currentNode = root;
			while(currentNode.isLeafNode != true){//根据startkey确定位置
				int i = 0;
				for(i = 0; i < currentNode.currentElements; i++){
					if(startKey.compareTo(currentNode.getElement(i)) == -1){
						currentNode = currentNode.getInternalPointer(i);
						break;
					}
				}
				currentNode = currentNode.getInternalPointer(i+1);
			}
			
			Vector<BPlusTreePointer> result = new Vector<BPlusTreePointer>();//startkey所在block判断
			for(int i = 0; i < currentNode.currentElements; i++){
				if(endKey == NULL){
					if(currentNode.getElement(i) >= startKey)
						result.addElement(currentNode.getAtrributePointer(i));
				}
				else{
					if(currentNode.getElement(i) >= startKey && currentNode.getElement(i) <= endKey)
						result.addElement(currentNode.getAtrributePointer(i));
					else if(currentNode.getElement(i) > endKey) return result;
				}
			}
			
			currentNode = currentNode.getTailPointer();//更加后面的block
			while(currentNode != NULL){
				for(int i = 0; i < currentNode.currentElements; i++){
					if(endKey == NULL){
						result.addElement(currentNode.getAtrributePointer(i));
						continue;
					}
					if(currentNode.getElement(i) <= endKey)
						result.addElement(currentNode.getAtrributePointer(i));
					else
						return result;
				}
				currentNode = currentNode.getTailPointer();
			}	
			
			return result;
			
		}
	}
	//删除
}