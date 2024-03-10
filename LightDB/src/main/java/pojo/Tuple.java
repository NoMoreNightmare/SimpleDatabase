package pojo;

import java.util.*;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Tuple tuple = (Tuple) o;
        for(int i = 0; i < tuple.getColumns().size(); i++){
            String column = columns.get(i);
            if(!this.getValue(column).equals(tuple.getValue(column))){
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }
}
