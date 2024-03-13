package Operator;

import tools.Tuple;

import java.util.*;

/**
 * the operator to eliminate the duplicate records using list or hashset
 */
public class DuplicateEliminationOperator extends Operator{
    Operator operator;
    List<Tuple> lists = new ArrayList<>();
    int index = 0;

    /**
     * construct the operator according to whether the tuples has been sorted
     * @param operator the child operator
     * @param ordered whether the tuples has been sorted
     */
    public DuplicateEliminationOperator(Operator operator, boolean ordered){
        this.operator = operator;
        if(!ordered){
            getAllTuplesWithSet();
        }else{
            getAllTuplesWithList();
        }
    }

    /**
     * use list to eliminate the duplicate
     */
    private void getAllTuplesWithList() {
        Tuple tuple = operator.getNextTuple();
        int currentIndex = 0;
        if(tuple != null){
            lists.add(tuple);
        }else{
            return;
        }
        tuple = operator.getNextTuple();
        while(tuple != null){
            Tuple last = lists.get(currentIndex);
            if(last.equals(tuple)){
                tuple = operator.getNextTuple();
                continue;
            }
            lists.add(tuple);
            tuple = operator.getNextTuple();
        }
    }

    /**
     * use hashset to eliminate the duplicate
     */
    private void getAllTuplesWithSet() {
        Set<Tuple> tuples = new HashSet<>();

        Tuple tuple = operator.getNextTuple();

        while(tuple != null){
            tuples.add(tuple);
            tuple = operator.getNextTuple();
        }

        lists = new ArrayList<>(tuples);
    }

    /**
     * get the next tuple
     * @return the current tuple or null
     */
    @Override
    public Tuple getNextTuple() {
        if(index == lists.size()){
            return null;
        }else{
            Tuple tuple = lists.get(index);
            index++;
            return tuple;
        }
    }

    /**
     * reset the operator
     */
    @Override
    public void reset() {
        operator.reset();
        index = 0;
    }

}
