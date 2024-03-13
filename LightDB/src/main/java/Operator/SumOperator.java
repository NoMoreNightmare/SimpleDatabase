package Operator;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import pojo.Parser.MultiplicationDeParser;
import pojo.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * the group operator for sum
 */
public class SumOperator extends Operator{
    Operator operator;
    Map<Tuple, Integer> sum = new HashMap<>();
    GroupByElement group;

    List<Tuple> results = new ArrayList<>();

    SelectItem<?> sumItem;

    int index = 0;

    /**
     * construct a group operator for sum
     * @param group the columns used for group
     * @param selectItems the projected columns and use it to check whether the sum column exists
     * @param operator the child operator
     */
    public SumOperator(GroupByElement group, List<SelectItem<?>> selectItems, Operator operator){
        this.operator = operator;
        this.group = group;
        this.sumItem = selectItems.get(selectItems.size() - 1);
        getAllGroups();
    }

    /**
     * get all the tuples and group them using sum and hashmap
     */
    private void getAllGroups() {
        if(group == null){
            int sum = 0;

            Tuple tuple = operator.getNextTuple();
            while(tuple != null){
                int current = calculateValue(tuple);
                sum += current;
                tuple = operator.getNextTuple();
            }
            putIntoMap(sum);
            createFinalTuple();

        }else{
            ExpressionList<?> expressionList = group.getGroupByExpressionList();
            Tuple tuple = operator.getNextTuple();
            while(tuple != null){
                Tuple currentTuple = new Tuple();
                List<String> columns = new ArrayList<>();
                List<Integer> values = new ArrayList<>();
                for(int i = 0; i < expressionList.size(); i++){
                    String columnName = expressionList.get(i).toString().toUpperCase();
                    columns.add(columnName);
                    int value = tuple.getValue(columnName);
                    values.add(value);
                }
                currentTuple.setColumns(columns);
                currentTuple.setValues(values);


                if(!(sumItem.getExpression() instanceof Function)){
                    this.sum.put(currentTuple, 0);
                    tuple = operator.getNextTuple();
                    continue;
                }

                int currentV = calculateValue(tuple);
                if(this.sum.containsKey(currentTuple)){
                    int currentSum = this.sum.get(currentTuple);
                    currentSum += currentV;
                    this.sum.replace(currentTuple, currentSum);
                }else{
                    this.sum.put(currentTuple, currentV);
                }
                tuple = operator.getNextTuple();
            }

            createFinalTuple();
        }

    }

    /**
     * create the result tuples list from the grouped tuples
     */
    private void createFinalTuple() {
        if(!(sumItem.getExpression() instanceof Function)){
            results.addAll(this.sum.keySet());
            return;
        }
        for(Tuple tuple : this.sum.keySet()){
            int currentSum = this.sum.get(tuple);
            List<String> columns = tuple.getColumns();
            columns.add(sumItem.toString().toUpperCase());
            tuple.setColumns(columns);
            List<Integer> values = tuple.getValues();
            values.add(currentSum);
            tuple.setValues(values);
            this.results.add(tuple);
        }
    }

    /**
     * create new tuple and put it in the hashmap
     * @param sum
     */
    private void putIntoMap(int sum) {
        Tuple tuple;
        tuple = new Tuple();
        List<String> columns = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        tuple.setColumns(columns);
        tuple.setValues(values);
        this.sum.put(tuple, sum);
    }

    /**
     * calculate the result inside the sum column for specified tuple
     * @param tuple the tuple to calculate the parameters in the sum operator
     * @return the result in the sum
     */
    private int calculateValue(Tuple tuple) {
        int mul = 1;
        Function function = (Function) sumItem.getExpression();
        Object parameters = function.getParameters().get(0);
        if(parameters instanceof Column){
            Column column = (Column) parameters;
            String columnName = column.getFullyQualifiedName().toUpperCase();

            return tuple.getValue(columnName);
        }else if(parameters instanceof Multiplication){
            Multiplication multiplication = (Multiplication) parameters;
            MultiplicationDeParser multiplicationDeParser = new MultiplicationDeParser();
            multiplication.accept(multiplicationDeParser);
            List<Expression> expressions = multiplicationDeParser.getExpressions();

            for(Expression expression : expressions){
                Column column = (Column) expression;
                String columnName = column.getFullyQualifiedName().toUpperCase();
                mul *= tuple.getValue(columnName);
            }
            return mul;
        }else{
            LongValue longValue = (LongValue) parameters;
            return (int)longValue.getValue();
        }
    }

    /**
     * get the next grouped result
     * @return the grouped tuple
     */
    @Override
    public Tuple getNextTuple() {
        if(index == results.size()){
            return null;
        }
        Tuple tuple = results.get(index);
        index++;
        return tuple;
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
