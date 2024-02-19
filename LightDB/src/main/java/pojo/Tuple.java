package pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tuple {


    public String tableName;

    public List<String> columns = new ArrayList<>();

    List<Integer> values = new ArrayList<>();

    public void setValues(List<Integer> values){
        this.values = values;
    }

    public List<Integer> getValues(){
        return values;
    }

    public List<String> getColumns() {
        return columns;
    }

    /**
     * 用来打印tuple的东西，上传时删除
     */
    public void printTuple(){
        for(Integer i : values){
            System.out.print(i + " ");
        }
        System.out.println();
    }

    public Integer getValue(String column){
        column = column.toUpperCase();
        if(columns.contains(column)){
            int index = columns.indexOf(column);
            return values.get(index);
        }else{
            return null;
        }
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }


}
