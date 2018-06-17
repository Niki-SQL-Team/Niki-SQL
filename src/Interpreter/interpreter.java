package Interpreter;

import java.io.*;
import Top.NKSql;
import Foundation.MemoryStorage.ConditionalAttribute;
import Interpreter.Comparison;
import Interpreter.Lexer;
import Interpreter.Tag;
import Interpreter.Token;

public class interpreter {
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
        Lexer lexer= new Lexer(reader);

    }

    public static void printSelectResult(){

    }
    
}