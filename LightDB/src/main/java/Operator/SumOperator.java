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

public class SumOperator extends Operator{
    Operator operator;
    Map<Tuple, Integer> sum = new HashMap<>();
    GroupByElement group;

    List<Tuple> results = new ArrayList<>();

    SelectItem<?> sumItem;

    int index = 0;

    public SumOperator(GroupByElement group, List<SelectItem<?>> selectItems, Operator operator){
        this.operator = operator;
        this.group = group;
        this.sumItem = selectItems.get(selectItems.size() - 1);
        getAllGroups();
    }

    private void getAllGroups() {
        if(group == null){
            int sum = 0;
//            Function function = (Function) sumItem.getExpression();
//            Object parameters = function.getParameters().get(0);

            Tuple tuple = operator.getNextTuple();
            while(tuple != null){
                int current = calculateValue(tuple);
                sum += current;
                tuple = operator.getNextTuple();
            }
            putIntoMap(sum);
//            if(parameters instanceof Column){
//                Column column = (Column) parameters;
//                String columnName = column.getFullyQualifiedName().toUpperCase();
//                Tuple tuple = operator.getNextTuple();
//                while(tuple != null){
//                    sum += tuple.getValue(columnName);
//                    tuple = operator.getNextTuple();
//                }
//                putIntoMap(sum);
//            }else if(parameters instanceof Multiplication){
//                Multiplication multiplication = (Multiplication) parameters;
//                MultiplicationDeParser multiplicationDeParser = new MultiplicationDeParser();
//                multiplication.accept(multiplicationDeParser);
//                List<Expression> expressions = multiplicationDeParser.getExpressions();
//
//                Tuple tuple = operator.getNextTuple();
//                while(tuple != null){
//                    int currentSum = 1;
//                    for(Expression expression : expressions){
//                        Column column = (Column) expression;
//                        String columnName = column.getFullyQualifiedName().toUpperCase();
//                        currentSum *= tuple.getValue(columnName);
//                    }
//                    sum += currentSum;
//
//                    tuple = operator.getNextTuple();
//                }
//
//                putIntoMap(sum);
//
//            }else{
//                LongValue longValue = (LongValue) parameters;
//                long value = longValue.getValue();
//                Tuple tuple = operator.getNextTuple();
//                while(tuple != null){
//                    sum += value;
//                    tuple = operator.getNextTuple();
//                }
//
//                putIntoMap(sum);
//            }

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

    private void putIntoMap(int sum) {
        Tuple tuple;
        tuple = new Tuple();
        List<String> columns = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        tuple.setColumns(columns);
        tuple.setValues(values);
        this.sum.put(tuple, sum);
    }

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

    @Override
    public Tuple getNextTuple() {
        if(index == results.size()){
            return null;
        }
        Tuple tuple = results.get(index);
        index++;
        return tuple;
    }

    @Override
    public void reset() {
        operator.reset();
        index = 0;
    }
}
