package Interpreter;

import java.io.*;
import Top.NKSql;
import Foundation.MemoryStorage.ConditionalAttribute;

public class interpreter {
    public static void main(String[] args) {

        System.out.println("Welcome to use MiniSql.");
        System.out.println("Please enter the command");

        try{
            NKSql.Initialize();
            BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
            Translating(reader);
        }
        catch(Exception e){
            System.out.println("Interpreter error:"+e.getMessage());
            e.printStackTrace();
        }
    }

    private static void Translating(BufferedReader reader)throws IOException{

    }
}