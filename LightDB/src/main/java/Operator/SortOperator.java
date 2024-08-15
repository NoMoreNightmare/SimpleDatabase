package Operator;

import net.sf.jsqlparser.statement.select.OrderByElement;
import tools.Tuple;

import java.io.*;
import java.util.*;

/**
 * the order operator that sort the tuples according to the specified columns
 */
public class SortOperator extends Operator{

    Operator operator;

    List<Tuple> tuples = new LinkedList<>();

    List<OrderByElement> orders;

    int index = 0;

    final int DEFAULT_SIZE = 1024;

    String tableName;
    List<String> columns;

    String tmpPrefix = "temp";

    BufferedReader finalFile;
    /**
     * construct the sort operator according to the specified column
     * @param orders the columns used for sorting
     * @param operator child operator
     */
    public SortOperator(List<OrderByElement> orders, Operator operator){
        this.orders = orders;
        this.operator = operator;
        getAllTuples();
    }

    /**
     * store all the result tuples from the child operator and prepare for the sorting
     */
    private void getAllTuples(){
        Tuple tuple = operator.getNextTuple();
        tableName = tuple.getTableName();
        columns = tuple.getColumns();

        int round = 0;
        int fileNo = 1;
        List<File> files = new ArrayList<>();

        while(tuple != null){
            tuples.add(tuple);
            tuple = operator.getNextTuple();
            if(tuples.size() == DEFAULT_SIZE){
                fileNo = saveTempFile(round, fileNo, files);
            }
        }

        saveTempFile(round, fileNo, files);
        externalMergeSort(files, round + 1);
    }

    private void externalMergeSort(List<File> files, int round) {

        while(true){
            if(files.size() == 1){
                try {
                    finalFile = new BufferedReader(new FileReader(files.get(0)));
//                    files.get(0).deleteOnExit();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                return;
            }
            int fileNo = 1;
            List<File> outputFiles = new ArrayList<>();
            Comparator<Tuple> comparator = new MyComparator();

            int i = 0;
            for (i = 0; i < files.size() - 1; i += 2) {
                File inputFile1 = files.get(i);
                File inputFile2 = files.get(i + 1);
                try {
                    File tmp = File.createTempFile(tmpPrefix + "_" + round + "_" + fileNo, "tmp");

                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tmp));

                    BufferedReader br1 = new BufferedReader(new FileReader(inputFile1));
                    BufferedReader br2 = new BufferedReader(new FileReader(inputFile2));

                    String s1 = br1.readLine();
                    String s2 = br2.readLine();


                    if(s1 == null){
                        while(s2 != null){
                            bufferedWriter.write(s2);
                            bufferedWriter.write("\n");
                            bufferedWriter.flush();
                            s2 = br2.readLine();
                        }
                        continue;
                    }

                    if(s2 == null){
                        while(s1 != null){
                            bufferedWriter.write(s1);
                            bufferedWriter.write("\n");
                            bufferedWriter.flush();
                            s1 = br1.readLine();
                        }
                        continue;
                    }

                    String[] split1 = s1.split(",");
                    String[] split2 = s2.split(",");


                    Tuple tmp1 = new Tuple();
                    Tuple tmp2 = new Tuple();

                    List<Integer> list1 = new ArrayList<>();
                    List<Integer> list2 = new ArrayList<>();


                    for (int i1 = 0; i1 < split1.length; i1++) {
                        list1.add(Integer.valueOf(split1[i1]));
                    }

                    for (int i1 = 0; i1 < split2.length; i1++) {
                        list2.add(Integer.valueOf(split2[i1]));
                    }

                    tmp1.setColumns(columns);
                    tmp1.setValues(list1);
                    tmp1.setTableName(tableName);

                    tmp2.setColumns(columns);
                    tmp2.setValues(list2);
                    tmp2.setTableName(tableName);

                    while(s1 != null || s2 != null){
                        if(s1 == null){
                            bufferedWriter.write(s2);
                            bufferedWriter.write("\n");
                            bufferedWriter.flush();
                            s2 = br2.readLine();
                            continue;
                        }else if(s2 == null){
                            bufferedWriter.write(s1);
                            bufferedWriter.write("\n");
                            bufferedWriter.flush();
                            s1 = br1.readLine();
                            continue;
                        }
                        int compare = comparator.compare(tmp1, tmp2);
                        if(compare <= 0){
                            bufferedWriter.write(s1);
                            bufferedWriter.write("\n");
                            bufferedWriter.flush();
                            s1 = br1.readLine();
                            if(s1 == null){
                                continue;
                            }
                            split1 = s1.split(",");
                            tmp1 = new Tuple();
                            tmp1.setTableName(tableName);
                            tmp1.setColumns(columns);
                            list1 = new ArrayList<>();
                            for (int i1 = 0; i1 < split1.length; i1++) {
                                list1.add(Integer.valueOf(split1[i1]));
                            }
                            tmp1.setValues(list1);
                        }else{
                            bufferedWriter.write(s2);
                            bufferedWriter.write("\n");
                            bufferedWriter.flush();
                            s2 = br2.readLine();
                            if(s2 == null){
                                continue;
                            }
                            split2 = s2.split(",");
                            tmp2 = new Tuple();
                            list2 = new ArrayList<>();
                            tmp2.setColumns(columns);
                            tmp2.setTableName(tableName);
                            for (int i1 = 0; i1 < split2.length; i1++) {
                                list2.add(Integer.valueOf(split2[i1]));
                            }
                            tmp2.setValues(list2);
                        }


                    }


                    outputFiles.add(tmp);


                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                inputFile1.delete();
                inputFile2.delete();
                fileNo++;
            }


            if(i == files.size() - 1){
                File file = files.get(i);
                outputFiles.add(file);
            }

            files = outputFiles;
            round += 1;
//            externalMergeSort(outputFiles, round + 1);

        }

    }

    private int saveTempFile(int round, int fileNo, List<File> files) {
        try {
            File tmp = File.createTempFile(tmpPrefix + "_" + round + "_" + fileNo, "tmp");
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tmp));
            tuples.sort(new MyComparator());

            for (int i = 0; i < tuples.size(); i++) {
                Tuple tempTuple = tuples.get(i);
                bufferedWriter.write(createCSVString(tempTuple));
                bufferedWriter.flush();
            }
            files.add(tmp);
            fileNo++;
            tuples.clear();
            bufferedWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileNo;
    }

    private String createCSVString(Tuple tempTuple) {
        StringBuffer sb = new StringBuffer();
        List<Integer> values = tempTuple.getValues();
        int first = values.get(0);
        sb.append(String.valueOf(first));
        for (int i = 1; i < values.size(); i++) {
            sb.append(",");
            sb.append(String.valueOf(values.get(i)));
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * get the next tuple according to the sorted tuples
     * @return
     */
    @Override
    public Tuple getNextTuple() {
        try {
            String value = finalFile.readLine();
            if(value == null){
                return null;
            }

            Tuple tuple = new Tuple();
            String[] valueArr = value.split(",");
            List<Integer> values = new ArrayList<>();

            for (int i = 0; i < valueArr.length; i++) {
                values.add(Integer.valueOf(valueArr[i]));
            }

            tuple.setColumns(columns);
            tuple.setValues(values);
            tuple.setTableName(tableName);

            return tuple;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        if(index == tuples.size()){
//            return null;
//        }

//        Tuple tuple = tuples.get(index);
//        index++;
//
//        return tuple;
    }

    /**
     * reset the operator
     */
    @Override
    public void reset() {
//        operator.reset();
//        index = 0;
        try {
            finalFile.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * the compare rules of the Tuple class for the sorting
     */
    class MyComparator implements Comparator<Tuple>{

        @Override
        public int compare(Tuple tuple1, Tuple tuple2) {
            for(OrderByElement order : orders){

                String column = order.toString().toUpperCase();
                int v1 = tuple1.getValue(column);
                int v2 = tuple2.getValue(column);
                if(v1 != v2){
                    return v1 - v2;
                }
            }

            return 0;
        }
    }
}
