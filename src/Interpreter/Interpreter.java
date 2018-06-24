package Interpreter;

import java.io.*;
import java.util.ArrayList;
import java.util.Vector;
import Foundation.MemoryStorage.*;
import Top.NKSql;
import Foundation.Enumeration.*;
import Foundation.Exception.*;
import Foundation.MemoryStorage.ConditionalAttribute;
import Interpreter.Comparison;
import Interpreter.Lexer;
import Interpreter.Tag;
import Interpreter.Token;

public class Interpreter {
    private static Token token;/*Tag*/
    private static boolean isSynCorrect = true;
    private static boolean isSemaCorrect = true;
    private static String synErrMsg;
    private static String semaErrMsg;

    public static void startInterpreter(String[] args) {

        System.out.println("Welcome to use MiniSql.");
        System.out.println("Please enter the command");
        NKSql nkSql;

        try {
            nkSql = new NKSql();
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            Translating(reader);
        } catch (Exception e) {
            System.out.println("Interpreter error:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void Translating(BufferedReader reader) throws IOException  {
        Lexer lexer = new Lexer(reader);/* 初始化读取文件变量*/
        while (!lexer.getReaderState()) {/* 是否读取到文件的结尾 */
            if (!isSynCorrect) {
                if (token.toString().equals(";")) {
                    System.out.println(synErrMsg);/*报错*/
                    isSemaCorrect = true;
                    isSynCorrect = true;
                    continue;
                }
            }
            token = lexer.scan();/*分类，得到tag标签*/
            if (token.tag == Tag.QUIT) {
                token = lexer.scan();
                if (token.toString().equals(";")) {
                    System.out.println("THANK YOU FOR USING!");
                    try {
                        NKSql.close();
                    } catch (Exception e) {
                        System.out.println("Interpreter error:" + e.getMessage());
                        e.printStackTrace();
                    }
                    reader.close();
                    System.exit(0);
                } else {
                    if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                    isSynCorrect = false;

                }

            } else if (token.tag == Tag.EXECFILE) {
                token = lexer.scan();
                File file = new File(token.toString() + ".txt");
                token = lexer.scan();
                if (token.toString().equals(";")) {

                    if (file.exists()) {
                        BufferedReader reader2 = new BufferedReader(new FileReader(file));
                        Translating(reader2);
                        isSynCorrect = true;

                    } else {
                        synErrMsg = "The file " + file.getName() + " doesn't exist";
                        isSynCorrect = false;

                    }

                } else {
                    if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                    isSynCorrect = false;

                }
            } else if (token.tag == Tag.CREATE) {
                token = lexer.scan();
                /*
                 * create table 错误种类
                 * 1 table name已存在
                 * 2 primary key不存在
                 * 3 重复attribute属性
                 * 4 attribute属性类型定义错误
                 * 5 char(n) 的n越界
                 */
                if (token.tag == Tag.TABLE) {
                    token = lexer.scan();
                    if (token.tag == Tag.ID) {    //create table ����
                        String tmpTableName = token.toString();
                        ArrayList<MetadataAttribute> tmpAttributes = new ArrayList<>();/*attribute*/

                        String tmpPrimaryKey = null;
                          /* if(CatalogManager.isTableExist(tmpTableName)){
                               /*
                            * create table 错误种类
                            * 1 table name已存在

                               semaErrMsg="The table "+tmpTableName+" already exists";
                               isSemaCorrect=false;
                           }
                           */
                        token = lexer.scan();
                        if (token.toString().equals("(")) {//create table XX(
                            token = lexer.scan();
                            while (!token.toString().equals(")") && !token.toString().equals(";")) {
                                if (token.tag == Tag.ID) {
                                    String tmpAttriName = token.toString();
                                    String tmpType;
                                    int tmpLength;
                                    DataType dataType = DataType.IntegerType;
                                    boolean tmpIsU = false;
                                    boolean tmpIsP = false;

                                       /*if(CatalogManager.isAttributeExist(tmpAttributes, tmpAttriName)){
                                           /*
                                            * create table 错误种类
                                            * 3 重复attribute属性
                                            *
                                           semaErrMsg="Duplicated attribute names "+tmpAttriName;
                                           isSemaCorrect=false;
                                       }*/
                                    token = lexer.scan();
                                    if (token.tag == Tag.TYPE) {
                                        tmpType = token.toString();
                                        if (tmpType.equals("int")) dataType.equals(DataType.IntegerType);
                                        if (tmpType.equals("float")) dataType = DataType.FloatType;
                                        if (tmpType.equals("char")) {
                                            dataType = DataType.StringType;
                                            token = lexer.scan();
                                            if (token.toString().equals("(")) {
                                                token = lexer.scan();
                                                if (token.tag == Tag.INTNUM) {
                                                    tmpLength = Integer.parseInt(token.toString());
                                                    if (tmpLength < 1 || tmpLength > 255) {
                                                        /*
                                                         * create table 错误种类
                                                         * 4 attribute属性类型定义错误
                                                         */
                                                        semaErrMsg = "The length of char should be 1<=n<=255";
                                                        isSemaCorrect = false;
                                                    }
                                                    token = lexer.scan();
                                                    if (!token.toString().equals(")")) {
                                                        if (isSynCorrect)
                                                            synErrMsg = "Synthetic error near: " + token.toString();
                                                        isSynCorrect = false;
                                                        break;
                                                    }
                                                } else {
                                                    if (isSynCorrect)
                                                        synErrMsg = "Synthetic error near: " + token.toString();
                                                    isSynCorrect = false;
                                                    break;
                                                }
                                            } else {
                                                if (isSynCorrect)
                                                    synErrMsg = "Synthetic error near: " + token.toString();
                                                isSynCorrect = false;
                                                break;
                                            }

                                        } else {
                                            tmpLength = 4;

                                        }
                                        token = lexer.scan();
                                        if (token.tag == Tag.UNIQUE) {
                                            tmpIsU = true;
                                            token = lexer.scan();
                                        }


                                        if (token.toString().equals(",")) {
                                            try {
                                                tmpAttributes.add(new MetadataAttribute(tmpAttriName, dataType, tmpLength, tmpIsP, tmpIsU));
                                            } catch (Exception e) {
                                                System.out.println("Interpreter error:" + e.getMessage());
                                                e.printStackTrace();
                                            }
                                        } else if (token.toString().equals(")")) {
                                            try {
                                                tmpAttributes.add(new MetadataAttribute(tmpAttriName, dataType, tmpLength, tmpIsP, tmpIsU));
                                            } catch (Exception e) {
                                                System.out.println("Interpreter error:" + e.getMessage());
                                                e.printStackTrace();
                                            }
                                            break;
                                        } else {
                                            if (isSynCorrect)
                                                synErrMsg = "Synthetic error near: " + token.toString();
                                            isSynCorrect = false;
                                            break;
                                        }
                                    } else {
                                        if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                                        isSynCorrect = false;
                                        break;
                                    }
                                } else if (token.tag == Tag.PRIMARY) {
                                    token = lexer.scan();
                                    if (token.tag == Tag.KEY) {
                                        token = lexer.scan();
                                        if (token.toString().equals("(")) {
                                            token = lexer.scan();
                                            if (token.tag == Tag.ID) {
                                                tmpPrimaryKey = token.toString();
                                                token = lexer.scan();
                                                if (token.toString().equals(")")) {
                                                      /* if(!CatalogManager.isAttributeExist(tmpAttributes, tmpPrimaryKey)){
                                                           semaErrMsg="The attribute "+tmpPrimaryKey+" doesn't exist";isSemaCorrect=false;
                                                       }*/
                                                } else {

                                                    if (isSynCorrect)
                                                        synErrMsg = "Synthetic error near: " + token.toString();
                                                    isSynCorrect = false;
                                                    break;
                                                }
                                            } else {
                                                if (isSynCorrect)
                                                    synErrMsg = "Synthetic error near: " + token.toString();
                                                isSynCorrect = false;
                                                break;
                                            }
                                        } else {
                                            if (isSynCorrect)
                                                synErrMsg = "Synthetic error near: " + token.toString();
                                            isSynCorrect = false;
                                            break;
                                        }
                                    } else {

                                        if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                                        isSynCorrect = false;
                                        break;
                                    }
                                } else {
                                    if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                                    isSynCorrect = false;
                                    break;
                                }
                                token = lexer.scan();
                            }
                            token = lexer.scan();

                            if (isSynCorrect && token.toString().equals(";")) {
                                /*
                                 * create table
                                 * */
                                if (tmpPrimaryKey == null) {
                                    synErrMsg = "Synthetic error: no primary key defined";
                                    isSynCorrect = false;
                                    continue;
                                } else {
                                    for (MetadataAttribute attribute : tmpAttributes) {
                                        if (attribute.attributeName.equals(tmpPrimaryKey)) {
                                            attribute.isPrimaryKey = true;
                                        }
                                    }
                                }
                                if (isSemaCorrect) {
                                    try {
                                        NKSql.createTable(tmpTableName, tmpAttributes);
                                    } catch (Exception e) {
                                        System.out.println("Interpreter error:" + e.getMessage());
                                        e.printStackTrace();
                                    }

                                } else {
                                    System.out.print(semaErrMsg);
                                    System.out.println(", create table " + tmpTableName + " failed");
                                    isSemaCorrect = true;
                                }

                            } else {
                                //System.out.println("stop here"+isSynCorrect);
                                if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                                isSynCorrect = false;

                            }
                        }
                    } else {
                        if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                        isSynCorrect = false;

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
                else if (token.tag == Tag.INDEX) {
                    String tmpIndexName, tmpTableName, tmpAttriName;
                    token = lexer.scan();
                    if (token.tag == Tag.ID) {//create index a
                        tmpIndexName = token.toString();
                           /*if(CatalogManager.isIndexExist(tmpIndexName)){
                               semaErrMsg="The index "+tmpIndexName+" already exist";
                               isSemaCorrect=false;
                           }*/
                        token = lexer.scan();
                        if (token.tag == Tag.ON) {//create index a on
                            token = lexer.scan();
                            if (token.tag == Tag.ID) {//create index a on b
                                tmpTableName = token.toString();
                                   /*if(!CatalogManager.isTableExist(tmpTableName)){
                                       semaErrMsg="The table "+tmpTableName+" doesn't exist";
                                       isSemaCorrect=false;
                                   }*/
                                token = lexer.scan();
                                if (token.toString().equals("(")) {
                                    token = lexer.scan();
                                    if (token.tag == Tag.ID) {
                                        tmpAttriName = token.toString();
                                           /*if(isSemaCorrect&&!CatalogManager.isAttributeExist(tmpTableName, tmpAttriName)){
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
                                           }*/
                                        token = lexer.scan();
                                        if (token.toString().equals(")") && lexer.scan().toString().equals(";")) {//create index a on b;
                                            /*
                                             * ִ��create index����
                                             * */
                                            if (isSemaCorrect) {
                                                try {
                                                    NKSql.createIndex(tmpIndexName, tmpTableName, tmpAttriName);
                                                } catch (Exception e) {
                                                    System.out.println("Interpreter error:" + e.getMessage());
                                                    e.printStackTrace();
                                                }

                                            } else {
                                                System.out.print(semaErrMsg);
                                                System.out.println(", create index failed");
                                                isSemaCorrect = true;
                                            }
                                        } else {
                                            if (isSynCorrect)
                                                synErrMsg = "Synthetic error near: " + token.toString();
                                            isSynCorrect = false;

                                        }
                                    } else {
                                        if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                                        isSynCorrect = false;

                                    }

                                } else {
                                    if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                                    isSynCorrect = false;

                                }
                            } else {
                                if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                                isSynCorrect = false;

                            }
                        } else {
                            if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                            isSynCorrect = false;

                        }
                    } else {
                        if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                        isSynCorrect = false;

                    }
                } else {
                    if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                    isSynCorrect = false;

                }          //end of index
            }           //end of create
            else if (token.tag == Tag.DROP) {
                token = lexer.scan();
                /*
                 * drop index 错误种类
                 * 1该table不存在
                 */
                if (token.tag == Tag.TABLE) {
                    String tmpTableName;
                    token = lexer.scan();
                    if (token.tag == Tag.ID) {//drop table a
                        tmpTableName = token.toString();
                           /*if(!CatalogManager.isTableExist(tmpTableName)){
                               semaErrMsg="The table "+tmpTableName+" doesn't exist, ";
                               isSemaCorrect=false;
                           }*/
                        token = lexer.scan();
                        if (token.toString().equals(";")) {//drop table a ;
                            /*
                             * ִ��drop table
                             * ����*/
                            if (isSemaCorrect) {
                                try {
                                    NKSql.dropTable(tmpTableName);
                                } catch (Exception e) {
                                    System.out.println("Interpreter error:" + e.getMessage());
                                    e.printStackTrace();
                                }
                            } else {
                                System.out.print(semaErrMsg);
                                System.out.println("drop table " + tmpTableName + " failed");
                                isSemaCorrect = true;
                            }

                        } else {
                            if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                            isSynCorrect = false;

                        }

                    } else {
                        if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                        isSynCorrect = false;

                    }
                }//end of drop table
                /*
                 * drop index 错误种类
                 * 1该index不存在
                 * 2 该index是主键
                 */
                else if (token.tag == Tag.INDEX) {//drop index
                    token = lexer.scan();
                    if (token.tag == Tag.ID) {//drop index a
                        String tmpIndexName = token.toString();
                           /*if(!CatalogManager.isIndexExist(tmpIndexName)){
                               semaErrMsg="The index "+tmpIndexName+" doesn't exist, ";
                               isSemaCorrect=false;
                           }*/
                        if (tmpIndexName.endsWith("_prikey")) {
                            semaErrMsg = "The index " + tmpIndexName + " is a primary key, ";
                            isSemaCorrect = false;
                        }
                        token = lexer.scan();
                        if (token.toString().equals(";")) {//drop index a ;
                            /*
                             * ִ��drop index ����
                             * */
                            if (isSemaCorrect) {
                                try {
                                    NKSql.dropIndex(tmpIndexName);
                                } catch (Exception e) {
                                    System.out.println("Interpreter error:" + e.getMessage());
                                    e.printStackTrace();
                                }
                            } else {
                                System.out.print(semaErrMsg);
                                System.out.println("drop index " + tmpIndexName + " failed");
                                isSemaCorrect = true;
                            }

                        } else {
                            if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                            isSynCorrect = false;

                        }
                    } else {
                        if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                        isSynCorrect = false;

                    }
                } else {
                    if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                    isSynCorrect = false;

                }//end of drop index
            }//end of drop
            /*
             * insert into 错误种类
             * 1 table 不存在
             * 2 插入的tuple数量不对
             * 3 插入的tuple类型或长度不对
             */
            else if (token.tag == Tag.INSERT) {
                token = lexer.scan();
                if (token.tag == Tag.INTO) {
                    token = lexer.scan();
                    if (token.tag == Tag.ID) {
                        String tmpTableName = token.toString();
                        Vector<String> dataItems = new Vector<>();


                           /*if(!CatalogManager.isTableExist(tmpTableName)){
                               /*
                                * insert into 错误种类
                                * 1 table 不存在
                                *
                               semaErrMsg="The table "+tmpTableName+" doesn't exist";
                               isSemaCorrect=false;
                           }*/

                        token = lexer.scan();
                        if (token.tag == Tag.VALUES) {
                            token = lexer.scan();
                            if (token.toString().equals("(")) {
                                token = lexer.scan();
                                String tmpValue;
                                int i = 0;//记录unit的index
                                while (!token.toString().equals(")")) {
                                    if (token.toString().equals("'")) token = lexer.scan();
                                    else if (token.toString().equals(",")) token = lexer.scan();
                                    else{
                                        tmpValue = token.toString();

                                    i++;
                                    dataItems.add(tmpValue);
                                      /* else if(isSemaCorrect){

                                           tmpValue=token.toString();
                                           int tmpLength=CatalogManager.getLength(tmpTableName, i);
                                           String tmpType=CatalogManager.getType(tmpTableName, i);
                                           String tmpAttriName=CatalogManager.getAttriName(tmpTableName, i);

                                           if(CatalogManager.inUniqueKey(tmpTableName, tmpAttriName)){
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
                                               i++;/*change here*
                                               units.add(tmpValue);
                                           }
                                           else{
                                               if(isSynCorrect)  synErrMsg="Synthetic error near: "+token.toString();isSynCorrect=false;
                                               break;
                                           }
                                       }*/
                                    token = lexer.scan();}
                                    if (token.toString().equals(",")) token = lexer.scan();
                                    else if (token.toString().equals(")")) ;
                                    else {
                                        if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                                        isSynCorrect = false;
                                        break;
                                    }
                                }
                                if (isSemaCorrect/*&&i<CatalogManager.getTableAttriNum(tmpTableName)*/) {
                                    isSemaCorrect = false;
                                    semaErrMsg = "The number of values is smaller than that of attributes";
                                }
                                token = lexer.scan();
                                if (isSynCorrect && token.toString().equals(";")) {
                                    /*
                                     * 执行insert 操作
                                     */



                                        Tuple units = new Tuple(dataItems);
                                        try {
                                            NKSql.insertTuple(units, tmpTableName);
                                        } catch (Exception e) {
                                            System.out.println("Interpreter error:" + e.getMessage());
                                            e.printStackTrace();
                                        }




                                } else {
                                    if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                                    isSynCorrect = false;

                                }
                            } else {
                                if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                                isSynCorrect = false;

                            }
                        } else {
                            if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                            isSynCorrect = false;

                        }
                    } else {
                        if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                        isSynCorrect = false;

                    }
                } else {
                    if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                    isSynCorrect = false;

                }
            }//end of insert
            /*
             * delete 错误种类
             * 1 table 不存在
             * 2 where 条件有误
             */
            else if (token.tag == Tag.DELETE) {
                String tmpAttriName;
                String op;
                String tmpValue;
                boolean constantFlag = false;
                CompareCondition compareCondition = CompareCondition.EqualTo;
                ArrayList<ConditionalAttribute> conditionalAttributes = new ArrayList<>();
                token = lexer.scan();

                if (token.tag == Tag.FROM) {//delete from
                    token = lexer.scan();
                    if (token.tag == Tag.ID) {
                        String tmpTableName = token.toString();

                        token = lexer.scan();

                        if (token.tag == Tag.WHERE) {
                            // 添加搜索条件
                            token = lexer.scan();
                                if (token.tag == Tag.ID) {
                                    tmpAttriName = token.toString();
                                    token = lexer.scan();
                                    if (token.tag == Tag.OP) {
                                        op = token.toString();
                                        token = lexer.scan();
                                        tmpValue = token.toString();
                                        if (token.tag == Tag.STR) {
                                            constantFlag = true;
                                        } else if (token.tag == Tag.INTNUM) {
                                            constantFlag = true;
                                        } else if (token.tag == Tag.FLOATNUM) {
                                            constantFlag = true;
                                        }


                                            if (isSemaCorrect && isSynCorrect && constantFlag) {
                                                /*
                                                 * 执行delete where 操作
                                                 */
                                                if (op.equals("<")) compareCondition = CompareCondition.LessThan;
                                                else if (op.equals(">"))
                                                    compareCondition = CompareCondition.GreaterThan;
                                                else if (op.equals("<="))
                                                    compareCondition = CompareCondition.NoGreaterThan;
                                                else if (op.equals(">="))
                                                    compareCondition = CompareCondition.NoLessThan;
                                                else if (op.equals("==")) compareCondition = CompareCondition.EqualTo;
                                                else if (op.equals("=")) compareCondition = CompareCondition.EqualTo;
                                                else if (op.equals("<>"))
                                                    compareCondition = CompareCondition.NotEqualTo;
                                                try {
                                                    ConditionalAttribute CA = new ConditionalAttribute(tmpTableName, tmpAttriName, tmpValue, compareCondition);

                                                    conditionalAttributes.add(CA);        /*int deleteNum = */
                                                    token = lexer.scan();

                                                    /*System.out.println("delete " + deleteNum + " tuples from table " + tmpTableName);*/
                                                } catch (Exception e) {
                                                    System.out.println("Interpreter error:" + e.getMessage());
                                                    e.printStackTrace();
                                                }
                                            }
                                            else {
                                                System.out.println(semaErrMsg + ", delete tuples failed");
                                                isSemaCorrect = true;
                                            }
                                            if(token.toString().equals(";")){
                                                /*int deleteNum = */

                                                /*System.out.println("delete " + deleteNum + " tuples from table " + tmpTableName);*/
                                                try{
                                                    NKSql.dropTuple(tmpTableName, conditionalAttributes);
                                                }
                                                catch(Exception e){
                                                    System.out.println("Interpreter error:"+e.getMessage());
                                                    e.printStackTrace();
                                                }
                                            }

                                    } else {
                                        if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                                        isSynCorrect = false;
                                    }
                                } else {
                                    if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                                    isSynCorrect = false;
                                }


                        }else if (token.toString().equals(";")) {

                            if (isSemaCorrect) {
                                /*
                                 * 执行delete操作
                                 */
                                try {
                                   /*change int deleteNum =*/ NKSql.dropTable(tmpTableName);

                                   /* System.out.println("delete " + deleteNum + " tuples from table " + tmpTableName);*/
                                } catch (Exception e) {
                                    System.out.println("Interpreter error:" + e.getMessage());
                                    e.printStackTrace();
                                }
                            } else {
                                System.out.println(semaErrMsg + ", delete tuples failed");
                                isSemaCorrect = true;
                            }
                        }

                    } else {

                        if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                        isSynCorrect = false;

                    }
                } else {
                    if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                    isSynCorrect = false;

                }
            }
            //end of delete
            /*
             * select 错误种类
             * 1 table 不存在
             * 2 attribute 不存在
             * 3 where 条件有误
             */
            else if (token.tag == Tag.SELECT) {
                token = lexer.scan();
                ArrayList<String> AttriName = new ArrayList<>();
                String tmpAttriName = null;
                String op;
                String tmpValue = null;
                boolean constantFlag=false;
                CompareCondition compareCondition = CompareCondition.All;
                ArrayList<ConditionalAttribute> conditionalAttributes = new ArrayList<>();
                AttriName.add(token.toString());
                token = lexer.scan();
                while (token.tag != Tag.FROM && token.tag == Tag.ID) {
                    AttriName.add(token.toString());
                    token = lexer.scan();

                }

                if (token.tag == Tag.FROM) {
                    token = lexer.scan();
                    if (token.tag == Tag.ID) {
                        String tmpTableName = token.toString();
                        if (token.tag == Tag.WHERE) {

                            // 添加 搜索条件
                            token = lexer.scan();
                            while (token.toString() != ";") {
                                if (token.toString().equals("and")) {
                                    token = lexer.scan();
                                }
                                if (token.tag == Tag.ID) {
                                    tmpAttriName = token.toString();
                                    if (token.tag == Tag.OP) {
                                        op = token.toString();
                                        token = lexer.scan();
                                        tmpValue = token.toString();
                                        if (token.tag == Tag.STR) {
                                            constantFlag = true;
                                        } else if (token.tag == Tag.INTNUM) {
                                            constantFlag = true;
                                        } else if (token.tag == Tag.FLOATNUM) {
                                            constantFlag = true;
                                        }


                                            if (isSemaCorrect && isSynCorrect && constantFlag) {
                                                /*
                                                 * 执行select where 操作
                                                 */
                                                if (op.equals("<")) compareCondition = CompareCondition.LessThan;
                                                else if (op.equals(">"))
                                                    compareCondition = CompareCondition.GreaterThan;
                                                else if (op.equals("<="))
                                                    compareCondition = CompareCondition.NoGreaterThan;
                                                else if (op.equals(">="))
                                                    compareCondition = CompareCondition.NoLessThan;
                                                else if (op.equals("=="))
                                                    compareCondition = CompareCondition.EqualTo;
                                                else if (op.equals("=")) compareCondition = CompareCondition.EqualTo;
                                                else if (op.equals("<>"))
                                                    compareCondition = CompareCondition.NotEqualTo;

                                                ConditionalAttribute CA = new ConditionalAttribute(tmpTableName, tmpAttriName, tmpValue, compareCondition);

                                                conditionalAttributes.add(CA);

                                            } else {
                                                System.out.println(semaErrMsg + ", select failed");
                                                isSemaCorrect = true;
                                            }

                                    } else {
                                        if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                                        isSynCorrect = false;
                                    }
                                } else {
                                    if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                                    isSynCorrect = false;
                                }
                            }token = lexer.scan();
                        } else if (token.toString().equals(";")) {
                            ConditionalAttribute CA = new ConditionalAttribute(tmpTableName, null, null, CompareCondition.All);

                            conditionalAttributes.add(CA);

                        }
                        ArrayList<Tuple> result = new ArrayList<>();
                        try {
                            result = NKSql.select(tmpTableName, AttriName, conditionalAttributes);
                            showSelectRes(AttriName,result);
                        } catch (Exception e) {
                            System.out.println("Interpreter error:" + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println(semaErrMsg + ", select tuples failed1");
                        isSemaCorrect = true;
                    }
                } else {
                    System.out.println(semaErrMsg + ", select tuples failed2");
                    isSemaCorrect = true;
                }
            } else {
                if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                isSynCorrect = false;

            }
        }
    }


    //end of select
    //end of while
    //end of Translating

    private static void showSelectRes(ArrayList<String> AttriName,ArrayList<Tuple> result) {
        if(result==null) System.out.println("There  do not exist what you want");
        else if(result.size()==0) System.out.println("There  do not exist what you want");
        else {
            for (String item : AttriName) {
                System.out.print(item+"    ");
            }
            System.out.print("\n");
            for (Tuple item : result) {
                for (String item1 : item.dataItems) {
                    System.out.print(item1+"    ");
                }
                System.out.print("\n");
            }
            System.out.println("There are " + result.size() + " tuples returned");
        }
    }

    private static Vector<String> ParsingProjection(Lexer lexer) throws IOException {
        Vector<String> tmpAttriNames = new Vector<String>();
        token = lexer.scan();
        if (token.toString().equals("*")) {
            token = lexer.scan();
            return null;
        } else {
            while (token.tag != Tag.FROM) {
                if (token.tag == Tag.ID) {
                    tmpAttriNames.add(token.toString());
                    token = lexer.scan();
                    if (token.toString().equals(",")) {
                        token = lexer.scan();
                    } else if (token.tag == Tag.FROM) ;
                    else {
                        if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                        isSynCorrect = false;
                        break;
                    }
                } else {
                    if (isSynCorrect) synErrMsg = "Synthetic error near: " + token.toString();
                    isSynCorrect = false;
                    break;
                }

            }
            return tmpAttriNames;
        }
    }
}