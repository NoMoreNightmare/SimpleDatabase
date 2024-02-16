package Operator;
import pojo.PropertyInTest;
import pojo.Tuple;
import ed.inf.adbs.lightdb.LightDB;

import java.io.*;

public abstract class Operator {
    String sqlPath;
    String dbPath;
    String outputPath;
    String sqlFilename;
    String schemaPath;
    public abstract Tuple getNextTuple();

    public abstract void reset();

    //TODO 把tuple写到适合的printStream，比如文件或者输出控制台

    public void dump() {
        String printChoice = PropertyInTest.properties.getProperty("printStream");
        if ("file".equals(printChoice)){
            PrintStream printStream = new PrintStream(System.out);

            printStream.close();
        }else if("console".equals(printChoice)){
            File file = new File(outputPath);
            if(!file.exists()){
                try {
                    boolean success = file.createNewFile();
                    if(!success){
                        throw new IOException("create file failed");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            PrintStream printStream = null;
            try {
                printStream = new PrintStream(new FileOutputStream(file, false));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }finally {
                if(printStream != null){
                    printStream.close();
                }
            }


        }else{
            //TODO 默认输出到控制台
        }
    }
}
