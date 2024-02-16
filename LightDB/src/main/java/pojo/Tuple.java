package pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tuple {
    static{
        columns = new ArrayList<>();
    }
    static List<String> columns;
    List<Integer> values = new ArrayList<>();

    public void addField(int value){
        values.add(value);
    }

}
