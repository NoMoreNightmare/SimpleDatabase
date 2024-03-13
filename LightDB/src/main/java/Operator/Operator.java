package Operator;
import tools.Catalog;
import tools.PropertyLoading;
import tools.Tuple;

import java.io.*;

/**
 * the abstract parent of all the operator
 */
public abstract class Operator {
    public abstract Tuple getNextTuple();

    public abstract void reset();

    //TODO 把tuple写到适合的printStream，比如文件或者输出控制台

    /**
     * print the result to the console or store the result in the file
     */
    public void dump() {
        String printChoice = PropertyLoading.properties.getProperty("printStream");
        if ("file".equals(printChoice)){
            String outputPath = Catalog.getInstance().getOutputPath();
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
            printToConsole();

        }else{
            //print to console
            printToConsole();
        }
    }

    /**
     * print to console
     */
    private void printToConsole() {
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
    }
}
