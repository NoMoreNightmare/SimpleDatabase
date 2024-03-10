package Operator;

import pojo.Tuple;

import java.util.*;

public class DuplicateEliminationOperator extends Operator{
    Operator operator;
    List<Tuple> lists = new ArrayList<>();
    int index = 0;

    public DuplicateEliminationOperator(Operator operator, boolean ordered){
        this.operator = operator;
        if(!ordered){
            getAllTuplesWithSet();
        }else{
            getAllTuplesWithList();
        }
    }

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

    private void getAllTuplesWithSet() {
        Set<Tuple> tuples = new HashSet<>();

        Tuple tuple = operator.getNextTuple();

        while(tuple != null){
            tuples.add(tuple);
            tuple = operator.getNextTuple();
        }

        lists = new ArrayList<>(tuples);
    }

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

    @Override
    public void reset() {
        operator.reset();
        index = 0;
    }

}
