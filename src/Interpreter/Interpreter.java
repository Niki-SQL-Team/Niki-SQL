package Interpreter;

import java.io.*;
import java.util.Vector;

import Top.NKSql;
import Foundation.MemoryStorage.ConditionalAttribute;
import Interpreter.Comparison;
import Interpreter.Lexer;
import Interpreter.Tag;
import Interpreter.Token;

public class Interpreter {
    private static Token token;/*Tag*/
    private static boolean isSynCorrect=true;
    private static boolean isSemaCorrect=true;
    private static String synErrMsg;
    private static String semaErrMsg;

    public static void startInterpreter(String[] args) {

        System.out.println("Welcome to use MiniSql.");
        System.out.println("Please enter the command");
        NKSql nkSql;

        try{
            nkSql = new NKSql();
            BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
            Translating(reader);
        }
        catch(Exception e){
            System.out.println("Interpreter error:"+e.getMessage());
            e.printStackTrace();
        }
    }

    public static void Translating(BufferedReader reader)throws IOException{
        Lexer lexer= new Lexer(reader);/* 初始化读取文件变量*/
           while(lexer.getReaderState() == false) {/* 是否读取到文件的结尾 */
               if (!isSynCorrect) {
                   if (token.toString().equals(";")) {
                       System.out.println(synErrMsg);/*报错*/
                       isSemaCorrect = true;
                       isSynCorrect = true;
                       continue;
                   }
               }
               token = lexer.scan();/*分类，得到tag标签*/
               if(token.tag==Tag.QUIT){
                   token=lexer.scan();
                   if(token.toString().equals(";")){
                       System.out.println("THANK YOU FOR USING!");
                       try{
                           NKSql.close();
                       }
                       catch(Exception e){
                           System.out.println("Interpreter error:"+e.getMessage());
                           e.printStackTrace();
                       }
                       reader.close();
                       System.exit(0);
                   }
                   else{
                       if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                       continue;
                   }

               }
               else if(token.tag==Tag.EXECFILE){
                   token=lexer.scan();
                   File file=new File(token.toString()+".txt");
                   token=lexer.scan();
                   if(token.toString().equals(";")){

                       if(file.exists()){
                           BufferedReader reader2=new BufferedReader(new FileReader(file));
                           Translating(reader2);
                           isSynCorrect=true;
                           continue;
                       }
                       else{
                           synErrMsg="The file "+file.getName()+" doesn't exist";
                           isSynCorrect=false;
                           continue;
                       }

                   }
                   else{
                       if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                       continue;
                   }
               }
               else if(token.tag==Tag.CREATE){
                   token=lexer.scan();
                   /*
                    * create table 错误种类
                    * 1 table name已存在
                    * 2 primary key不存在
                    * 3 重复attribute属性
                    * 4 attribute属性类型定义错误
                    * 5 char(n) 的n越界
                    */
                   if(token.tag==Tag.TABLE){
                       token=lexer.scan();
                       if(token.tag==Tag.ID){	//create table ����
                           String tmpTableName=token.toString();
                           Vector<attribute>tmpAttributes=new Vector<attribute>();/*attribute*/
                           String tmpPrimaryKey=null;
                           if(CatalogManager.isTableExist(tmpTableName)){
                               /*
                            * create table 错误种类
                            * 1 table name已存在
                            */
                               semaErrMsg="The table "+tmpTableName+" already exists";
                               isSemaCorrect=false;
                           }
                           token=lexer.scan();
                           if(token.toString().equals("(")){//create table XX(
                               token=lexer.scan();
                               while(!token.toString().equals(")")&&!token.toString().equals(";")){
                                   if(token.tag==Tag.ID){
                                       String tmpAttriName=token.toString();
                                       String tmpType;
                                       int tmpLength;
                                       boolean tmpIsU=false;
                                       if(CatalogManager.isAttributeExist(tmpAttributes, tmpAttriName)){
                                           /*
                                            * create table 错误种类
                                            * 3 重复attribute属性
                                            */
                                           semaErrMsg="Duplicated attribute names "+tmpAttriName;
                                           isSemaCorrect=false;
                                       }
                                       token=lexer.scan();
                                       if(token.tag==Tag.TYPE){
                                           tmpType=token.toString();
                                           if(tmpType.equals("char")){
                                               token=lexer.scan();
                                               if(token.toString().equals("(")){
                                                   token=lexer.scan();
                                                   if(token.tag==Tag.INTNUM){
                                                       tmpLength = Integer.parseInt(token.toString());
                                                       if(tmpLength<1||tmpLength>255){
                                                           /*
                                                            * create table 错误种类
                                                            * 4 attribute属性类型定义错误
                                                            */
                                                           semaErrMsg="The length of char should be 1<=n<=255";
                                                           isSemaCorrect=false;
                                                       }
                                                       token=lexer.scan();
                                                       if(token.toString().equals(")"));
                                                       else{
                                                           if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                                           break;
                                                       }
                                                   }
                                                   else{
                                                       if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                                       break;
                                                   }
                                               }
                                               else{
                                                   if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                                   break;
                                               }

                                           }
                                           else{
                                               tmpLength=4;

                                           }
                                           token=lexer.scan();
                                           if(token.tag==Tag.UNIQUE){
                                               tmpIsU=true;
                                               token=lexer.scan();
                                           }
                                           else ;
                                           if(token.toString().equals(",")){
                                               tmpAttributes.addElement(new attribute(tmpAttriName,tmpType,tmpLength,tmpIsU));
                                           }
                                           else if(token.toString().equals(")")){
                                               tmpAttributes.addElement(new attribute(tmpAttriName,tmpType,tmpLength,tmpIsU));
                                               break;
                                           }
                                           else{
                                               if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                               break;
                                           }
                                       }
                                       else{
                                           if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                           break;
                                       }
                                   }

                                   else if(token.tag==Tag.PRIMARY){
                                       token=lexer.scan();
                                       if(token.tag==Tag.KEY){
                                           token=lexer.scan();
                                           if(token.toString().equals("(")){
                                               token=lexer.scan();
                                               if(token.tag==Tag.ID){
                                                   tmpPrimaryKey=token.toString();
                                                   token=lexer.scan();
                                                   if(token.toString().equals(")")){
                                                       if(!CatalogManager.isAttributeExist(tmpAttributes, tmpPrimaryKey)){
                                                           semaErrMsg="The attribute "+tmpPrimaryKey+" doesn't exist";isSemaCorrect=false;
                                                       }
                                                   }
                                                   else{

                                                       if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                                       break;
                                                   }
                                               }
                                               else{
                                                   if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                                   break;
                                               }
                                           }
                                           else{
                                               if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                               break;
                                           }
                                       }
                                       else{

                                           if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                           break;
                                       }
                                   }

                                   else{
                                       if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                       break;
                                   }
                                   token=lexer.scan();
                               }
                               token=lexer.scan();

                               if(isSynCorrect&&token.toString().equals(";")){
                                   /*
                                    * create table
                                    * */
                                   if(tmpPrimaryKey==null){
                                       synErrMsg="Synthetic error: no primary key defined";isSynCorrect=false;
                                       continue;
                                   }
                                   if(isSemaCorrect){
                                       try{
                                          NKSql.createTable(tmpTableName,new table(tmpTableName,tmpAttributes,tmpPrimaryKey));
                                       }
                                       catch(Exception e){
                                           System.out.println("Interpreter error:"+e.getMessage());
                                           e.printStackTrace();
                                       }

                                   }
                                   else{
                                       System.out.print(semaErrMsg);
                                       System.out.println(", create table "+tmpTableName+" failed");
                                       isSemaCorrect=true;
                                   }
                                   continue;
                               }
                               else{
                                   //System.out.println("stop here"+isSynCorrect);
                                   if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                   continue;
                               }
                           }
                       }
                       else{
                           if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                           continue;
                       }
                   }
                   /*
                    * create index 语义错误种类
                    * 1 index name已存在
                    * 2 table name 不存在
                    * 3 attribute不存在
                    * 4 attribute已经是索引
                    * 5 attribute 不是unique
                    */
                   else if(token.tag==Tag.INDEX){
                       String tmpIndexName,tmpTableName,tmpAttriName;
                       token=lexer.scan();
                       if(token.tag==Tag.ID){//create index a
                           tmpIndexName=token.toString();
                           if(CatalogManager.isIndexExist(tmpIndexName)){
                               semaErrMsg="The index "+tmpIndexName+" already exist";
                               isSemaCorrect=false;
                           }
                           token=lexer.scan();
                           if(token.tag==Tag.ON){//create index a on
                               token=lexer.scan();
                               if(token.tag==Tag.ID){//create index a on b
                                   tmpTableName=token.toString();
                                   if(!CatalogManager.isTableExist(tmpTableName)){
                                       semaErrMsg="The table "+tmpTableName+" doesn't exist";
                                       isSemaCorrect=false;
                                   }
                                   token=lexer.scan();
                                   if(token.toString().equals("(")){
                                       token=lexer.scan();
                                       if(token.tag==Tag.ID){
                                           tmpAttriName=token.toString();
                                           if(isSemaCorrect&&!CatalogManager.isAttributeExist(tmpTableName, tmpAttriName)){
                                               semaErrMsg="The attribute "+tmpAttriName+" doesn't exist on "+tmpTableName;
                                               isSemaCorrect=false;
                                           }
                                           else if(isSemaCorrect&&!CatalogManager.inUniqueKey(tmpTableName, tmpAttriName)){
                                               semaErrMsg="The attribute "+tmpAttriName+" on "+tmpTableName+" is not unique";
                                               isSemaCorrect=false;
                                           }
                                           else if(isSemaCorrect&&CatalogManager.isIndexKey(tmpTableName, tmpAttriName)){
                                               semaErrMsg="The attribute "+tmpAttriName+" on "+tmpTableName+" is already an index";
                                               isSemaCorrect=false;
                                           }
                                          token=lexer.scan();
                                           if(token.toString().equals(")")&&lexer.scan().toString().equals(";")){//create index a on b;
                                               /*
                                                * ִ��create index����
                                                * */
                                               if(isSemaCorrect){
                                                   try{
                                                       NKSql.createIndex(new index(tmpIndexName,tmpTableName,tmpAttriName));
                                                   }
                                                   catch(Exception e){
                                                       System.out.println("Interpreter error:"+e.getMessage());
                                                       e.printStackTrace();
                                                   }

                                               }
                                               else{
                                                   System.out.print(semaErrMsg);
                                                   System.out.println(", create index failed");
                                                   isSemaCorrect=true;
                                               }
                                           }
                                           else{
                                               if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                               continue;
                                           }
                                       }
                                       else{
                                           if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                           continue;
                                       }

                                   }
                                   else{
                                       if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                       continue;
                                   }
                               }
                               else{
                                   if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                   continue;
                               }
                           }
                           else{
                               if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                               continue;
                           }
                       }
                       else{
                           if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                           continue;
                       }
                   }
                   else{
                       if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                       continue;
                   }          //end of index
               }           //end of create
               else if(token.tag==Tag.DROP){
                   token=lexer.scan();
                   /*
                    * drop index 错误种类
                    * 1该table不存在
                    */
                   if(token.tag==Tag.TABLE){
                       String tmpTableName;
                       token=lexer.scan();
                       if(token.tag==Tag.ID){//drop table a
                           tmpTableName=token.toString();
                           if(!CatalogManager.isTableExist(tmpTableName)){
                               semaErrMsg="The table "+tmpTableName+" doesn't exist, ";
                               isSemaCorrect=false;
                           }
                           token=lexer.scan();
                           if(token.toString().equals(";")){//drop table a ;
                               /*
                                * ִ��drop table
                                * ����*/
                               if(isSemaCorrect){
                                   try{
                                       NKSql.dropTable(tmpTableName);
                                   }
                                   catch(Exception e){
                                       System.out.println("Interpreter error:"+e.getMessage());
                                       e.printStackTrace();
                                   }
                               }
                               else{
                                   System.out.print(semaErrMsg);
                                   System.out.println("drop table "+tmpTableName+" failed");
                                   isSemaCorrect=true;
                               }
                               continue;
                           }
                           else{
                               if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                               continue;
                           }

                       }
                       else{
                           if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                           continue;
                       }
                   }//end of drop table
                   /*
                    * drop index 错误种类
                    * 1该index不存在
                    * 2 该index是主键
                    */
                   else if(token.tag==Tag.INDEX){//drop index
                       token=lexer.scan();
                       if(token.tag==Tag.ID){//drop index a
                           String tmpIndexName=token.toString();
                           if(!CatalogManager.isIndexExist(tmpIndexName)){
                               semaErrMsg="The index "+tmpIndexName+" doesn't exist, ";
                               isSemaCorrect=false;
                           }
                           if(tmpIndexName.endsWith("_prikey")){
                               semaErrMsg="The index "+tmpIndexName+" is a primary key, ";
                               isSemaCorrect=false;
                           }
                           token=lexer.scan();
                           if(token.toString().equals(";")){//drop index a ;
                               /*
                                * ִ��drop index ����
                                * */
                               if(isSemaCorrect){
                                   try{
                                       NKSql.dropIndex(tmpIndexName);
                                   }
                                   catch(Exception e){
                                       System.out.println("Interpreter error:"+e.getMessage());
                                       e.printStackTrace();
                                   }
                               }
                               else{
                                   System.out.print(semaErrMsg);
                                   System.out.println("drop index "+tmpIndexName+" failed");
                                   isSemaCorrect=true;
                               }
                               continue;
                           }
                           else{
                               if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                               continue;
                           }
                       }
                       else{
                           if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                           continue;
                       }
                   }
                   else{
                       if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                       continue;
                   }//end of drop index
               }//end of drop
               /*
                * insert into 错误种类
                * 1 table 不存在
                * 2 插入的tuple数量不对
                * 3 插入的tuple类型或长度不对
                */
               else if(token.tag==Tag.INSERT){
                   token=lexer.scan();
                   if(token.tag==Tag.INTO){
                       token=lexer.scan();
                       if(token.tag==Tag.ID){
                           String tmpTableName=token.toString();
                           Vector<String>units=new Vector<String>();
                           if(!CatalogManager.isTableExist(tmpTableName)){
                               /*
                                * insert into 错误种类
                                * 1 table 不存在
                                */
                               semaErrMsg="The table "+tmpTableName+" doesn't exist";
                               isSemaCorrect=false;
                           }

                           token=lexer.scan();
                           if(token.tag==Tag.VALUES){
                               token=lexer.scan();
                               if(token.toString().equals("(")){
                                   token=lexer.scan();
                                   String tmpValue ;
                                   int i=0;//记录unit的index
                                   while(!token.toString().equals(")")){
                                       if(isSemaCorrect&&i>=CatalogManager.getTableAttriNum(tmpTableName)){
                                           isSemaCorrect=false;
                                           semaErrMsg="The number of values is larger than that of attributes";
                                       }
                                       else if(isSemaCorrect){

                                           tmpValue=token.toString();
                                           int tmpLength=CatalogManager.getLength(tmpTableName, i);
                                           String tmpType=CatalogManager.getType(tmpTableName, i);
                                           String tmpAttriName=CatalogManager.getAttriName(tmpTableName, i);

                                           if(CatalogManager.inUniqueKey(tmpTableName, tmpAttriName)){//对于unique key的判别/*change here*/
                                               conditionNode tmpCondition=new conditionNode(tmpAttriName,"=",token.toString());

                                               if(isSemaCorrect&&NKSql.selectTuples(tmpTableName,null,tmpCondition).size()!=0){
                                                   isSemaCorrect=false;
                                                   semaErrMsg="The value "+token.toString()+" already exists in the unique attrubute "+tmpAttriName;
                                               }
                                           }
                                           if(token.tag==Tag.STR){//字符类型

                                               //if(tmpType.equals("char"))tmpLength/=2;
                                               if(!tmpType.equals("char")
                                                       ||tmpLength<tmpValue.getBytes().length){
                                                   isSemaCorrect=false;
                                                   semaErrMsg="The type of value +"+tmpValue+" should be "+tmpType+"("+tmpLength+"), not char("+tmpValue.getBytes().length+")";
                                               }
                                               i++;
                                               units.add(tmpValue);
                                           }
                                           else if(token.tag==Tag.INTNUM){//整型

                                               if(!tmpType.toString().equals("int")
                                                       &&!tmpType.equals("float")){
                                                   isSemaCorrect=false;
                                                   semaErrMsg="The type of value +"+tmpValue+" should be "+tmpType+"("+tmpLength+"), not be int";
                                               }
                                               i++;
                                               units.add(tmpValue);
                                           }
                                           else if(token.tag==Tag.FLOATNUM){//浮点型

                                               if(!CatalogManager.getType(tmpTableName, i++).equals("float")){
                                                   isSemaCorrect=false;
                                                   semaErrMsg="The type of value +"+tmpValue+" should be "+tmpType+"("+tmpLength+"), not float";
                                               }
                                               i++;/*change here*/
                                               units.add(tmpValue);
                                           }
                                           else{
                                               if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                               break;
                                           }
                                       }
                                       token=lexer.scan();
                                       if(token.toString().equals(","))token=lexer.scan();
                                       else if(token.toString().equals(")"));
                                       else{
                                           if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                           break;
                                       }
                                   }
                                   if(isSemaCorrect&&i<CatalogManager.getTableAttriNum(tmpTableName)){
                                       isSemaCorrect=false;
                                       semaErrMsg="The number of values is smaller than that of attributes";
                                   }
                                   token=lexer.scan();
                                   if(isSynCorrect&&token.toString().equals(";")){
                                       /*
                                        * 执行insert 操作
                                        */


                                       if(isSemaCorrect){
                                           if(NKSql.insertTuple(tmpTableName,new tuple(units)))
                                               System.out.println("insert into "+tmpTableName+" succeeded.");
                                           else
                                               System.out.println("Error:insert into "+tmpTableName+" failed.");
                                       }
                                       else{
                                           System.out.print(semaErrMsg);
                                           System.out.println(", insert failed");
                                           isSemaCorrect=true;
                                       }
									

                                   }
                                   else{
                                       if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                       continue;
                                   }
                               }
                               else{
                                   if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                   continue;
                               }
                           }
                           else{
                               if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                               continue;
                           }
                       }
                       else{
                           if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                           continue;
                       }
                   }
                   else{
                       if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                       continue;
                   }
               }//end of insert
               /*
                * delete 错误种类
                * 1 table 不存在
                * 2 where 条件有误 
                */
               else if(token.tag==Tag.DELETE){
                   token=lexer.scan();
                   if(token.tag==Tag.FROM){//delete from
                       token=lexer.scan();
                       if(token.tag==Tag.ID){
                           String tmpTableName=token.toString();
                           if(!CatalogManager.isTableExist(tmpTableName)){
                               semaErrMsg="The table "+tmpTableName+" doesn't exist";
                               isSemaCorrect=false;
                           }
                           token=lexer.scan();
                           if(token.tag==Tag.WHERE){//delete from 表名 where 条件；
                               // 添加搜索条件
                               conditionNode tmpConditionNodes=ParsingCondition(lexer,tmpTableName,";");
                               if(token.toString().equals(";")){//delete from 表名；

                                   if(isSemaCorrect&&isSynCorrect){
                                       /*
                                        * 执行delete where 操作
                                        */
                                       int deleteNum=NKSql.dropTuple(tmpTableName, tmpConditionNodes);
                                       System.out.println("delete "+deleteNum+ " tuples from table "+tmpTableName);
                                       //System.out.println("delete succeeded");
                                   }
                                   else if(!isSynCorrect){
                                       continue;
                                   }
                                   else{
                                       System.out.println(semaErrMsg+", delete tuples failed");
                                       isSemaCorrect=true;
                                   }
                               }
                               else{
                                   if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                   continue;
                               }
                           }
                           else if(token.toString().equals(";")){//delete from 表名；

                               if(isSemaCorrect){
                                   /*
                                    * 执行delete操作
                                    */
                                   int deleteNum=NKSql.dropTuple(tmpTableName, null);

                                   System.out.println("delete "+deleteNum+ " tuples from table "+tmpTableName);

                               }
                               else{
                                   System.out.println(semaErrMsg+", delete tuples failed");
                                   isSemaCorrect=true;
                               }
                           }
                           else{

                               if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                               continue;
                           }
                       }
                       else{
                           if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                           continue;
                       }
                   }
                   else{
                       if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                       continue;
                   }
               }//end of insert
               /*
                * select 错误种类
                * 1 table 不存在
                * 2 attribute 不存在
                * 3 where 条件有误
                */
               else if(token.tag==Tag.SELECT){
                   Vector<String>tmpAttriNames=ParsingProjection(lexer);
                   if(isSynCorrect&&token.tag==Tag.FROM){//select * from
                       token=lexer.scan();
                       if(token.tag==Tag.ID){
                           String tmpTableName=token.toString();
                           String tmpTableName2="";
                           boolean joinflag=false;
                           if(isSemaCorrect&&!CatalogManager.isTableExist(tmpTableName)){
                               semaErrMsg="The table "+tmpTableName+" doesn't exist";
                               isSemaCorrect=false;
                           }
                           if(tmpAttriNames!=null)//对于投影的属性进行判断
                               for(int i=0;i<tmpAttriNames.size();i++){
                                   if(isSemaCorrect&&!CatalogManager.isAttributeExist(tmpTableName, tmpAttriNames.get(i))){
                                       semaErrMsg="The attribute "+tmpAttriNames.get(i)+" doesn't exist";
                                       isSemaCorrect=false;
                                   }
                               }
                           token=lexer.scan();
                           //如果有join
                           if(token.tag==Tag.JOIN||token.toString().equals(",")){
                               joinflag=true;
                               token=lexer.scan();
                               if(token.tag==Tag.ID){

                                   tmpTableName2=token.toString();
                                   if(isSemaCorrect&&!CatalogManager.isTableExist(tmpTableName2)){
                                       semaErrMsg="The table "+tmpTableName2+" doesn't exist";
                                       isSemaCorrect=false;
                                   }
                                   token=lexer.scan();
                               }
                               else{
                                   if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                   continue;
                               }
                           }
                           if(isSynCorrect&&token.tag==Tag.WHERE){//select * from 表名 where 条件；
                               /* 添加搜索条件*/

                               if(joinflag){
                                   token=lexer.scan();
                                   String[]tmpName1=new String[2],tmpName2=new String[2];

                                   if(token.tag==Tag.ID){
                                       tmpName1=token.toString().split("\\.");
                                       if(isSemaCorrect&&!CatalogManager.isTableExist(tmpName1[0])){
                                           semaErrMsg="The table "+tmpName1[0]+" doesn't exist";
                                           isSemaCorrect=false;
                                       }
                                       if(isSemaCorrect&&!CatalogManager.isAttributeExist(tmpTableName, tmpName1[1])){
                                           semaErrMsg="The attribute "+tmpName1[1]+" doesn't exist";
                                           isSemaCorrect=false;
                                       }
                                       token=lexer.scan();
                                       if(token.tag==Tag.OP){
                                           token=lexer.scan();
                                           if(token.tag==Tag.ID){
                                               tmpName2=token.toString().split("\\.");
                                               if(isSemaCorrect&&!CatalogManager.isTableExist(tmpName2[0])){
                                                   semaErrMsg="The table "+tmpName2[0]+" doesn't exist";
                                                   isSemaCorrect=false;
                                               }
                                               if(isSemaCorrect&&!CatalogManager.isAttributeExist(tmpTableName, tmpName2[1])){
                                                   semaErrMsg="The attribute "+tmpName2[1]+" doesn't exist";
                                                   isSemaCorrect=false;
                                               }
                                               token=lexer.scan();
                                               if(token.toString().equals(";")){
                                                   if(isSemaCorrect&&isSynCorrect){
                                                       /*
                                                        * 执行select join 操作*/
                                                       for(int i=0;i<CatalogManager.getTableAttriNum(tmpTableName);i++){ //输出属性名
                                                           System.out.print("\t"+CatalogManager.getAttriName(tmpTableName, i));
                                                       }
                                                       for(int i=0;i<CatalogManager.getTableAttriNum(tmpTableName2);i++){ //输出属性名
                                                           System.out.print("\t"+CatalogManager.getAttriName(tmpTableName2, i));
                                                       }
                                                       System.out.println();
                                                       Vector<tuple> seleteTuples=NKSql.join(tmpName1[0],tmpName1[1],tmpName2[0],tmpName2[1]);/*change here*/
                                                       for(int i=0;i<seleteTuples.size();i++){
                                                           System.out.println(seleteTuples.get(i).getString());
                                                       }

                                                   }
                                                   else if(!isSynCorrect) continue;
                                                   else{
                                                       System.out.println(semaErrMsg+", select tuples failed");
                                                       isSemaCorrect=true;
                                                   }
                                               }
                                               else{
                                                   if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                                   continue;
                                               }

                                           }
                                           else{
                                               if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                               continue;
                                           }
                                       }
                                       else{
                                           if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                           continue;
                                       }
                                   }
                                   else{
                                       if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                       continue;
                                   }

                                   continue;
                               }
                               conditionNode tmpConditionNode=ParsingCondition(lexer,tmpTableName,";");
                               if(token.toString().equals(";")){//select from 表名；
                                   if(isSemaCorrect&&isSynCorrect){
                                       /*
                                        * 执行select where 操作*/

                                       showSelectRes(tmpTableName,tmpAttriNames, tmpConditionNode,null,false);

                                   }
                                   else if(!isSynCorrect) continue;
                                   else{
                                       System.out.println(semaErrMsg+", select tuples failed");
                                       isSemaCorrect=true;
                                   }

                               }
                               else if(isSynCorrect&&token.tag==Tag.ORDER){
                                   token=lexer.scan();
                                   if(token.tag==Tag.BY){
                                       token=lexer.scan();
                                       if(token.tag==Tag.ID){
                                           String tmpOrderAttriName=token.toString();
                                           if(isSemaCorrect&&!CatalogManager.isAttributeExist(tmpTableName, tmpOrderAttriName)){
                                               semaErrMsg="The attribute "+tmpOrderAttriName+" doesn't exist";
                                               isSemaCorrect=false;
                                           }
                                           token=lexer.scan();
                                           if(token.toString().equals(";")||token.tag==Tag.ASC||token.tag==Tag.DESC){
                                               boolean order;
                                               if(token.toString().equals(";")) order=true;
                                               else {
                                                   order=token.tag==Tag.ASC?true:false;
                                                   token=lexer.scan();
                                                   if(isSynCorrect&&!token.toString().equals(";")){
                                                       synErrMsg="Synthetic error near: "+token.toString();
                                                       isSynCorrect=false;
                                                       continue;
                                                   }
                                               }
                                               if(isSemaCorrect){
                                                   /*执行select where order操作*/
                                                   showSelectRes(tmpTableName,tmpAttriNames, tmpConditionNode,tmpOrderAttriName,order);

                                               }
                                               else{
                                                   System.out.println(semaErrMsg+", select tuples failed");
                                                   isSemaCorrect=true;
                                               }
                                           }
                                           else{
                                               if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                               continue;
                                           }

                                       } else{
                                           if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                           continue;
                                       }
                                   }
                                   else{
                                       if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                       continue;
                                   }
                               }
                               else{
                                   if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                   continue;
                               }
                           }
                           else if(token.toString().equals(";")){//select * from 表名；
                               if(isSemaCorrect){
                                   /*执行select 操作*/
                                   showSelectRes(tmpTableName,tmpAttriNames, null,null,false);
                               }
                               else{
                                   System.out.println(semaErrMsg+", select tuples failed");
                                   isSemaCorrect=true;
                               }
                           }
                           else if(token.tag==Tag.ORDER){
                               token=lexer.scan();
                               if(token.tag==Tag.BY){
                                   token=lexer.scan();
                                   if(token.tag==Tag.ID){
                                       String tmpOrderAttriName=token.toString();
                                       if(isSemaCorrect&&!CatalogManager.isAttributeExist(tmpTableName, tmpOrderAttriName)){
                                           semaErrMsg="The attribute "+tmpOrderAttriName+" doesn't exist";
                                           isSemaCorrect=false;
                                       }
                                       token=lexer.scan();
                                       if(token.toString().equals(";")||token.tag==Tag.ASC||token.tag==Tag.DESC){
                                           boolean order;
                                           if(token.toString().equals(";")) order=true;
                                           else {
                                               order=token.tag==Tag.ASC?true:false;
                                               token=lexer.scan();
                                               if(isSynCorrect&&!token.toString().equals(";")){
                                                   synErrMsg="Synthetic error near: "+token.toString();
                                                   isSynCorrect=false;
                                                   continue;
                                               }
                                           }
                                           if(isSemaCorrect){
                                               /*
                                                * 执行select order操作
                                                */
                                               showSelectRes(tmpTableName,tmpAttriNames, null,tmpOrderAttriName,order);

                                           }
                                           else{
                                               System.out.println(semaErrMsg+", select tuples failed");
                                               isSemaCorrect=true;
                                           }
                                       }
                                       else{
                                           if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                           continue;
                                       }

                                   } else{
                                       if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                       continue;
                                   }
                               }
                               else{
                                   if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                   continue;
                               }
                           }
                           else{

                               if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                               continue;
                           }
                       }
                       else{
                           if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                           continue;
                       }
                   }
                   else{
                       if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                       continue;
                   }


               }//end of select
           }//end of while
    }//end of Translating

    private static void showSelectRes(String tmpTableName,Vector<String> tmpAttriNames,conditionNode tmpConditionNode,String tmpOrderAttriName,boolean order){
        if(tmpAttriNames==null)
            for(int i=0;i<CatalogManager.getTableAttriNum(tmpTableName);i++){ //全选
                System.out.print("\t"+CatalogManager.getAttriName(tmpTableName, i));
            }
        else
            for(int i=0;i<tmpAttriNames.size();i++)
                System.out.print("\t"+tmpAttriNames.get(i));
        System.out.println();
        Vector<tuple> seleteTuples;
        if(tmpOrderAttriName==null)
            seleteTuples=NKSql.selectTuple(tmpTableName,tmpAttriNames, tmpConditionNode);
        else{
            seleteTuples=NKSql.selectTuple(tmpTableName,tmpAttriNames, tmpConditionNode,tmpOrderAttriName,order);
        }
        for(int i=0;i<seleteTuples.size();i++){
            System.out.println(seleteTuples.get(i).getString());
        }
        System.out.println("There are "+seleteTuples.size()+" tuples returned");
    }

    private static Vector<String> ParsingProjection(Lexer lexer) throws IOException{
        Vector<String>tmpAttriNames=new Vector<String>();
        token=lexer.scan();
        if(token.toString().equals("*")){
            token=lexer.scan();
            return null;
        }
        else{
            while(token.tag!=Tag.FROM){
                if(token.tag==Tag.ID){
                    tmpAttriNames.add(token.toString());
                    token=lexer.scan();
                    if(token.toString().equals(",")){
                        token=lexer.scan();
                    }
                    else if(token.tag==Tag.FROM);
                    else{
                        if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                        break;
                    }
                }
                else{
                    if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                    break;
                }

            }
            return tmpAttriNames;
        }
    }

    private static conditionNode ParsingExpression(Lexer lexer,String tmpTableName) throws IOException{
        String tmpAttriName;Comparison op;String tmpValue;
        boolean constantFlag = false;
        if(token.tag==Tag.ID){
            tmpAttriName=token.toString();
            if(isSemaCorrect&&!CatalogManager.isAttributeExist(tmpTableName, tmpAttriName)){
                isSemaCorrect=false;
                semaErrMsg="The attribute "+tmpAttriName+" doesn't exist";
            }
            token=lexer.scan();
            if(token.tag==Tag.OP){
                op=Comparison.parseCompar(token);
                token=lexer.scan();
                tmpValue=token.toString();
                if(isSemaCorrect){
                    if(token.tag==Tag.STR){
                        constantFlag=true;
                        String tmpType=CatalogManager.getType(tmpTableName, tmpAttriName);
                        int tmpLength=CatalogManager.getLength(tmpTableName, tmpAttriName);

                        if(!tmpType.equals("char")
                                ||tmpLength<tmpValue.getBytes().length){
                            isSemaCorrect=false;
                            semaErrMsg="The type of value +"+tmpValue+" should be "+tmpType+"("+tmpLength+"), not char("+tmpValue.getBytes().length+")";
                        }
                    }
                    else if(token.tag==Tag.INTNUM){
                        constantFlag=true;
                        String tmpType=CatalogManager.getType(tmpTableName, tmpAttriName);
                        int tmpLength=CatalogManager.getLength(tmpTableName, tmpAttriName);

                        if(!tmpType.toString().equals("int")
                                &&!tmpType.equals("float")){
                            isSemaCorrect=false;
                            semaErrMsg="The type of value +"+tmpValue+" should be "+tmpType+"("+tmpLength+"), not int";
                        }
                    }
                    else if(token.tag==Tag.FLOATNUM){
                        constantFlag=true;
                        String tmpType=CatalogManager.getType(tmpTableName, tmpAttriName);
                        int tmpLength=CatalogManager.getLength(tmpTableName, tmpAttriName);

                        if(!tmpType.equals("float")){
                            isSemaCorrect=false;
                            semaErrMsg="The type of value +"+tmpValue+" should be "+tmpType+"("+tmpLength+"), not float";
                        }
                    }
                    else if(token.tag==Tag.ID){
                        constantFlag=false;
                        String tmpType1=CatalogManager.getType(tmpTableName, tmpAttriName);
                        String tmpType2=CatalogManager.getType(tmpTableName, tmpValue);
                        //֧��float��int��char�����Լ�Ƚ�
                        if(!tmpType1.equals(tmpType2)){
                            isSemaCorrect=false;
                            semaErrMsg="The two attributes are in different types and cannot be compared";
                        }
                    }
                    else{
                        if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();
                        isSynCorrect=false;

                    }
                    //return new conditionNode(tmpAttriName,op,tmpValue,constantFlag);

                }
                return new conditionNode(tmpAttriName,op,tmpValue,constantFlag);

            }
            else{
                if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();
                isSynCorrect=false;
            }
        }
        else{
            if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();
            isSynCorrect=false;
        }
        return null;
    }

    private static conditionNode ParsingCondition(Lexer lexer,String tmpTableName,String endtoken)throws IOException {
        /*
         * �����������
         * 1 ������������
         * 2 value ��ʽ����
         * 3 ���������� charֻ֧��= <>
         */
        conditionNode tmpConditionRoot = null;
        conditionNode tmpExpresstion = null,tmpConjunction;
        token=lexer.scan();
        boolean flag=false;//�����һ��ʽ���Ǵ����ŵ� flag==true �Ա�֤��������
        if(token.toString().equals("(")){
            tmpConditionRoot=ParsingCondition(lexer,tmpTableName,")");
            flag=true;
        }
        else if(token.tag==Tag.ID){
            tmpConditionRoot=ParsingExpression(lexer,tmpTableName);
        }
        else{
            if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();
            isSynCorrect=false;
        }
        if(tmpConditionRoot==null||!isSynCorrect){
            return null;
        }

        token=lexer.scan();
        while(!token.toString().equals(endtoken)&&token.tag!=Tag.ORDER){
            if(token.tag==Tag.AND){
                tmpConjunction=new conditionNode("and");
                token=lexer.scan();
                if(token.toString().equals("(")){
                    tmpExpresstion=ParsingCondition(lexer,tmpTableName,")");
                }
                else if(token.tag==Tag.ID){
                    tmpExpresstion=ParsingExpression(lexer,tmpTableName);
                }
                else{
                    if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();
                    isSynCorrect=false;
                }
                if(tmpExpresstion==null){
                    return null;
                }
                //����
                if(tmpConditionRoot.conjunction=="or"&&flag==false){

                    tmpConditionRoot=tmpConditionRoot.linkChildNode(tmpConditionRoot.left, tmpConjunction.linkChildNode(tmpConditionRoot.right, tmpExpresstion));

                }

                else{
                    tmpConditionRoot=tmpConjunction.linkChildNode(tmpConditionRoot, tmpExpresstion);
                    if(flag) flag=false;
                }



            }
            else if(token.tag==Tag.OR){
                tmpConjunction=new conditionNode("or");
                token=lexer.scan();
                if(token.toString().equals("(")){
                    tmpExpresstion=ParsingCondition(lexer,tmpTableName,")");
                }
                else if(token.tag==Tag.ID){
                    tmpExpresstion=ParsingExpression(lexer,tmpTableName);
                }
                else{
                    if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();
                    isSynCorrect=false;
                }

                if(tmpExpresstion==null){
                    return null;
                }
                //����
                tmpConditionRoot=tmpConjunction.linkChildNode(tmpConditionRoot, tmpExpresstion);
            }

            else if(token.toString().equals(endtoken)||token.tag==Tag.ORDER);
            else{
                if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                break;
            }
            token=lexer.scan();

        }

        return tmpConditionRoot;

    }




}