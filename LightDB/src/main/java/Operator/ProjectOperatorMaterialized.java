package Operator;

import net.sf.jsqlparser.statement.select.SelectItem;
import tools.PropertyLoading;
import tools.Tuple;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * the project operator with materialization that filter out the wanted column
 */
public class ProjectOperatorMaterialized extends ProjectOperator{
    String tableName = null;
    List<String> columnNames = null;

    String tempFilepath = null;

    BufferedReader br = null;

    boolean empty = false;

    /**
     * construct a projection operator and apply materialization
     * @param selectItem the projected columns
     * @param operator the child operator
     */
    public ProjectOperatorMaterialized(List<SelectItem<?>> selectItem, Operator operator) {
        super(selectItem, operator);
        try {
            materialize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * get the next tuple if one of the columns of that table will be used, otherwise return null
     * @return the tuple
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
     * get all the tuples and store them in specified file to materialization
     * store the table name and columns of that tuple
     * @throws IOException IOException
     */
    public void materialize() throws IOException {
        String dir = PropertyLoading.properties.getProperty("temp-path");
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
