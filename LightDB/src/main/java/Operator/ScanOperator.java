package Operator;

import pojo.Catalog;
import pojo.Tuple;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScanOperator extends Operator{

    //TODO 创建一个能够读取csv文件的对象引用

    List<List<Integer>> data = new ArrayList<>();
    public int index = 0;




    //TODO 应该在构造的时候知道要解析的sql文件路径，数据文件的路径和最终输出的路径
    public ScanOperator(String tableName){
        //TODO 获取Catalog来获取各个目录
        Catalog catalog = Catalog.getInstance();
        String dbFile = catalog.getDbPath() + tableName +".csv";


        //TODO 将dbPath和表名拼接，获取数据文件的路径
        //TODO 读取数据文件，创建读取文件的对象
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(dbFile);
            br = new BufferedReader(fr);

            String line = br.readLine();
            while(line != null){
                String[] list = line.split(",");
                List<Integer> values = new ArrayList<>();
                for(String field : list){
                    field = field.trim();
                    values.add(Integer.valueOf(field));
                }
                data.add(values);
                line = br.readLine();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {         //br还需要读，fr可能还不能关闭
            if(br != null){
                try {
                    br.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        //TODO 初始化Tuple的列
        String schema = catalog.getSchemaPath();
        FileReader frSchema = null;
        BufferedReader brSchema = null;
        try {
            frSchema = new FileReader(schema);
            brSchema = new BufferedReader(frSchema);
            String line = brSchema.readLine();
            while(line != null){
                List<String> tableAndColumn = new ArrayList<>(Arrays.asList(line.split(" ")));
                if(tableName.equals(tableAndColumn.get(0))){
                    tableAndColumn.remove(0);
                    Tuple.columns = tableAndColumn;
                    break;
                }
                line = brSchema.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if(brSchema != null){
                try {
                    brSchema.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }



    }

    //TODO 获取这个operator会输出的下一个tuple
    @Override
    public Tuple getNextTuple() {
        if(index >= data.size()){
            return null;
        }
        List<Integer> values = data.get(index);
        index++;
        Tuple tuple = new Tuple();
        tuple.setValues(values);
        return tuple;
    }

    //TODO 从头开始，从这个operator会返回的第一个tuple重新开始
    @Override
    public void reset() {
        System.out.println("hello");
        index = 0;
    }


}
