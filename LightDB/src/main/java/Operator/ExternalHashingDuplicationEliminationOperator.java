package Operator;

import cn.hutool.core.util.HashUtil;
import tools.Tuple;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * the operator to eliminate the duplicate records using list or hashset
 */
public class ExternalHashingDuplicationEliminationOperator extends Operator{
    Operator operator;

    File finalFile;

    BufferedReader br;

    BufferedWriter bw;

    String tableName;
    List<String> columns;
    int DEFAULT_SIZE = 1024 * 8; //1024B

    int DEFAULT_PAGES = 4;

    List<File> files = new ArrayList<>(DEFAULT_PAGES);
    {
        try {
            finalFile = File.createTempFile("final_tmp", "tmp");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < DEFAULT_PAGES; i++) {
            try {
                files.add(File.createTempFile("temp_" + i, "tmp"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * construct the operator according to whether the tuples has been sorted
     * @param operator the child operator
     */
    public ExternalHashingDuplicationEliminationOperator(Operator operator){
        this.operator = operator;

        try {
            br = new BufferedReader(new FileReader(finalFile));
            bw = new BufferedWriter(new FileWriter(finalFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        eliminateDuplication();
    }

    private void eliminateDuplication() {
        List<ByteArrayOutputStream> write = new ArrayList<>(DEFAULT_PAGES);
        for (int i = 0; i < DEFAULT_PAGES; i++) {
            write.add(new ByteArrayOutputStream(DEFAULT_SIZE));
        }

        Tuple tuple = operator.getNextTuple();
        if(tuple == null){
            return;
        }

        tableName = tuple.getTableName();
        columns = tuple.getColumns();

        while(tuple != null){
            String tupleStr = tuple.toString();
            int hash = Math.abs(HashUtil.apHash(tupleStr)) % DEFAULT_PAGES;
            byte[] toWrite = tupleStr.getBytes();
            ByteArrayOutputStream byteArrayOutputStream = write.get(hash);
            if(byteArrayOutputStream.size() + toWrite.length < DEFAULT_SIZE){
                byteArrayOutputStream.write(toWrite, 0, toWrite.length);
                try {
                    byteArrayOutputStream.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }else{
                try {
                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(files.get(hash)));
                    bufferedWriter.write(byteArrayOutputStream.toString());
                    byteArrayOutputStream.reset();
                    byteArrayOutputStream.write(toWrite);
                    byteArrayOutputStream.flush();
                    bufferedWriter.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            tuple = operator.getNextTuple();
        }

        for (int i = 0; i < write.size(); i++) {
            ByteArrayOutputStream byteArrayOutputStream = write.get(i);
            File file = files.get(i);
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter(file));
                bw.write(byteArrayOutputStream.toString());
                bw.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    if(bw != null){
                        bw.close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        for (int i = 0; i < DEFAULT_PAGES; i++) {
            try {
                write.get(i).close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        try {
            for (int i = 0; i < DEFAULT_PAGES; i++) {
                Set<Tuple> set = new HashSet<>();
                BufferedReader br = new BufferedReader(new FileReader(files.get(i)));
                String line = br.readLine();
                while(line != null){
                    Tuple newTuple = createTuple(line);
                    line = br.readLine();
                    set.add(newTuple);
                }
                saveTupleToFinalFile(set);
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private void saveTupleToFinalFile(Set<Tuple> set) {
        try {
            for (Tuple tuple : set) {
                bw.write(tuple.toString());
                bw.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private Tuple createTuple(String line) {
        String[] split = line.split(",");
        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < split.length; i++) {
            values.add(Integer.valueOf(split[i]));
        }

        Tuple tuple = new Tuple();
        tuple.setValues(values);
        tuple.setColumns(columns);
        tuple.setTableName(tableName);

        return tuple;
    }

    @Override
    public Tuple getNextTuple() {
        String tupleStr = null;
        try {
            tupleStr = br.readLine();
            if(tupleStr != null){
                return createTuple(tupleStr);
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
        operator.reset();
        try {
            br.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
