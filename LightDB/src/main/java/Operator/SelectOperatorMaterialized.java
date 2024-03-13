package Operator;

import net.sf.jsqlparser.expression.Expression;
import pojo.PropertyInTest;
import pojo.Tuple;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * the selection operator with materialization
 */
public class SelectOperatorMaterialized extends SelectOperator{

    String tableName = null;
    List<String> columnNames = null;

    String tempFilepath = null;

    BufferedReader br = null;

    boolean empty = false;

    /**
     * construct the selection operator with materialization approach
     * @param expression the expression for filtering the tuples
     * @param operator the child operator
     */
    public SelectOperatorMaterialized(Expression expression, Operator operator) {
        super(expression, operator);
        try {
            materialize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * get the next tuple that fit the expression
     * @return the next tuple
     */
    @Override
    public Tuple getNextTuple() {
        if(empty){
            return null;
        }

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
                tuple.setColumns(columnNames);
                tuple.setValues(values);
                return tuple;
            }else{
                return null;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    /**
     * reset the operator
     */
    @Override
    public void reset() {
        super.reset();
        //TODO bufferreader的位置重置
        try {
            br = new BufferedReader(new FileReader(tempFilepath));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * materialize the selection result in the file
     * @throws IOException IOException
     */
    public void materialize() throws IOException {
        String dir = PropertyInTest.properties.getProperty("temp-path");
        File file = new File(dir);
        if(!file.exists()){
            file.mkdirs();
        }

        Tuple tuple = super.getNextTuple();
        if(tuple != null){
            String tableName = tuple.getTableName();
            tempFilepath = dir + tableName + ".csv";
            FileWriter fileWriter = new FileWriter(tempFilepath, StandardCharsets.UTF_8);
            BufferedWriter bw = new BufferedWriter(fileWriter);

            List<String> columns = tuple.getColumns();
            this.columnNames = columns;
            this.tableName = tableName;
            int index = 0;
//            bw.write(columns.get(index));
//            index++;
//            while(index < columns.size()){
//                bw.write(",");
//
//                bw.write(columns.get(index));
//                index++;
//            }
//            bw.write("\n");

            while(tuple != null){
                List<Integer> values = tuple.getValues();
                index = 0;
                if(values.size() == 0){
                    bw.write("1\n");
                    tuple = super.getNextTuple();
                }else{
                    bw.write(String.valueOf(values.get(index)));
                    index++;
                    while(index < values.size()){
                        bw.write(",");
                        bw.write(String.valueOf(values.get(index)));
                        index++;
                    }
                    bw.write("\n");
                    tuple = super.getNextTuple();
                }

            }

            bw.flush();
            bw.close();

            FileReader fr = new FileReader(tempFilepath, StandardCharsets.UTF_8);
            br = new BufferedReader(fr);
        }else{
            empty = true;
        }


    }
}
