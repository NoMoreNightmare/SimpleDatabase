package Operator;

import net.sf.jsqlparser.statement.select.FromItem;
import pojo.Catalog;
import pojo.Tuple;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScanOperator extends Operator{

    //TODO 创建一个能够读取csv文件的对象引用

    List<String> columns;

    String tableName;

    BufferedReader br;

    String dbFile = null;


    //TODO 应该在构造的时候知道要解析的sql文件路径，数据文件的路径和最终输出的路径
    public ScanOperator(FromItem fromItem){
        //TODO 获取Catalog来获取各个目录
        String tableName;
        Catalog catalog = Catalog.getInstance();

        if(fromItem.getAlias() == null){
            tableName = fromItem.toString();
        }else{
            tableName = fromItem.toString().split(" ")[0];
        }
        dbFile = catalog.getDbPath() + "/data/" + tableName +".csv";;


        //TODO 将dbPath和表名拼接，获取数据文件的路径
        //TODO 读取数据文件，创建读取文件的对象
        FileReader fr = null;
        try {
            fr = new FileReader(dbFile);
            br = new BufferedReader(fr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //TODO 初始化Tuple的表名和列
        String schema = catalog.getDbPath() + "/" + catalog.getSchemaFile();
        FileReader frSchema = null;
        BufferedReader brSchema = null;
        try {
            frSchema = new FileReader(schema);
            brSchema = new BufferedReader(frSchema);
            String line = brSchema.readLine();

            this.tableName = tableName.toUpperCase();

            while(line != null){
                String[] columnStrings = line.split(" ");
                columnStrings[0] = columnStrings[0].toUpperCase();

                for(int i = 1; i < columnStrings.length; i++){
                    if(fromItem.getAlias() == null){
                        columnStrings[i] = columnStrings[0] + "." + columnStrings[i].toUpperCase();
                    }else{
                        columnStrings[i] = fromItem.getAlias().toString().trim().toUpperCase() + "." + columnStrings[i].toUpperCase();
                    }

                }
                List<String> tableAndColumn = new ArrayList<>(Arrays.asList(columnStrings));
                if(tableName.toUpperCase().equals(tableAndColumn.get(0))){
                    tableAndColumn.remove(0);
                    columns = tableAndColumn;
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
        try {
            String line = br.readLine();
            if(line != null){
                String[] list = line.split(",");
                List<Integer> values = new ArrayList<>();
                for(String field : list){
                    field = field.trim();
                    values.add(Integer.valueOf(field));
                }
                Tuple tuple = new Tuple();
                tuple.setTableName(tableName);
                tuple.setColumns(columns);
                tuple.setValues(values);
                return tuple;
            }else{
                return null;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //TODO 从头开始，从这个operator会返回的第一个tuple重新开始
    @Override
    public void reset() {
        try {
            br = new BufferedReader(new FileReader(dbFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


}
