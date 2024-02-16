package Operator;
import pojo.Catalog;
import pojo.PropertyInTest;
import pojo.Tuple;
import ed.inf.adbs.lightdb.LightDB;

import java.io.*;

public abstract class Operator {
    public abstract Tuple getNextTuple();

    public abstract void reset();

    //TODO 把tuple写到适合的printStream，比如文件或者输出控制台

    public void dump() {
        String printChoice = PropertyInTest.properties.getProperty("printStream");
        if ("file".equals(printChoice)){
            String outputPath = Catalog.getInstance().getOutputPath() + "test.csv";
            File file = new File(outputPath);
            if(!file.exists()){
                try {
//                    file.mkdirs();

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
                while(true){
                    Tuple tuple = this.getNextTuple();
                    if(tuple == null){
                        break;
                    }
                    StringBuffer stringBuffer = new StringBuffer();
                    for(Integer value : tuple.getValues()) {
                        stringBuffer.append(value);
                        stringBuffer.append(",");
                    }
                    int index = stringBuffer.lastIndexOf(",");
                    stringBuffer.delete(index, index + 1);
                    printStream.print(stringBuffer);
                    printStream.print("\n");
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }finally {
                if(printStream != null){
                    printStream.close();
                }
            }
        }else if("console".equals(printChoice)){
            PrintStream printStream = null;

            try {
                printStream = new PrintStream(System.out);
                while(true){
                    Tuple tuple = this.getNextTuple();
                    if(tuple == null){
                        break;
                    }
                    for(Integer value : tuple.getValues()) {
                        printStream.print(value);
                        printStream.print(" ");
                    }
                    printStream.print("\n");
                }
            } catch(Exception e){
                e.printStackTrace();
            }
//            finally {     //printStream一旦关闭就在这个程序中再也无法启用，因此不关闭
//                if(printStream != null){
//                    printStream.close();
//                }
//            }

        }else{
            //TODO 默认输出到控制台
        }
    }
}
