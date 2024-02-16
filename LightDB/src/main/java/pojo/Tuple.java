package pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tuple {
    public static List<String> columns;

    static{
        columns = new ArrayList<>();
    }
    List<Integer> values = new ArrayList<>();

    public void setValues(List<Integer> values){
        this.values = values;
    }

    public List<Integer> getValues(){
        return values;
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



}
