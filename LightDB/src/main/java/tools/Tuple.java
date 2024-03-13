package tools;

import java.util.*;

/**
 * the class stored tuple information
 */
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
     * print tuple
     */
    public void printTuple(){
        for(Integer i : values){
            System.out.print(i + " ");
        }
        System.out.println();
    }

    /**
     * get the value of that column in the tuple
     * @param column the specified column
     * @return the value
     */
    public Integer getValue(String column){
        column = column.toUpperCase();
        if(columns.contains(column)){
            int index = columns.indexOf(column);
            return values.get(index);
        }else{
            return null;
        }
    }

    /**
     * set the columns in the tuple
     * @param columns columns
     */
    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    /**
     * get the table name that tuple belongs to
     * @return the table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * set the table name that tuple belongs to
     * @param tableName  the table name
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * the rules to judge whether two tuples are equal
     * @param o the tuple to compare
     * @return whether two tuples are equal
     */
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

    /**
     * rewrited hashcode
     * @return
     */
    @Override
    public int hashCode() {
        return Objects.hash(values);
    }
}
