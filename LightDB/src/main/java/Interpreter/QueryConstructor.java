package Interpreter;
import Operator.*;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.SimpleNode;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import pojo.Parser.JoinExpressionDeParser;
import pojo.Parser.MultiplicationDeParser;

import javax.print.DocFlavor;
import java.io.IOException;
import java.util.*;

/**
 * responsible for creating optimized query plan from a Statement and return the root operator
 */
public class QueryConstructor {
    /**
     * construct a query plan from a statement
     * @param statement the sql statement
     * @return the root operator of the entire query plan
     */
    public Operator constructor(Statement statement){
        if (statement != null) {
            Select select = (Select) statement;

            PlainSelect plainSelect = select.getPlainSelect();

            //try to get all the components from the statement
            FromItem fromItem = plainSelect.getFromItem();

            Expression expression = plainSelect.getWhere();

            List<SelectItem<?>> selectItems = plainSelect.getSelectItems();

            List<Join> joins = plainSelect.getJoins();

			GroupByElement groupBy = plainSelect.getGroupBy();

//			Expression having = plainSelect.getHaving();

            Distinct distinct = plainSelect.getDistinct();


            List<OrderByElement> orderByElements = plainSelect.getOrderByElements();

            boolean materialized = true;

            Operator first;

            if(joins == null){
                first = constructScanSelect(fromItem, expression, false);
            }else{
//                SelectItem<Expression> selectItem = new SelectItem<>();
//                selectItem.setExpression(expression);

                if(selectItems.size() == 1 && selectItems.get(0).getExpression() instanceof AllColumns){
                    first = recursiveConstructJoin(fromItem, expression, joins, false);
                }else{
                    Map<String, Column> projectionPush = new HashMap<>();
                    if(groupBy != null){
                        ExpressionList expressionsList = groupBy.getGroupByExpressionList();
                        for(Object o : expressionsList){
                            Column newExpression = (Column) o;

                            projectionPush.put(newExpression.getFullyQualifiedName().toUpperCase(), newExpression);
                        }
                    }

                    if(orderByElements != null){
                        for(OrderByElement order : orderByElements){
                            Column column = (Column) order.getExpression();
                            projectionPush.put(column.getFullyQualifiedName().toUpperCase(), column);
                        }
                    }

                    for(SelectItem<?> item : selectItems){
                        if(item.getExpression() instanceof Column column){
                            projectionPush.put(column.getFullyQualifiedName().toUpperCase(), column);
                        }else if(item.getExpression() instanceof Function function){
                            Object o = function.getParameters().get(0);
                            if(o instanceof Multiplication multiplication){
                                MultiplicationDeParser deParser = new MultiplicationDeParser();
                                multiplication.accept(deParser);
                                List<Expression> expressions = deParser.getExpressions();
                                for(Expression newExpression : expressions){
                                    if(newExpression instanceof Column column){
                                        projectionPush.put(column.getFullyQualifiedName().toUpperCase(), column);
                                    }
                                }
                            }else if(o instanceof Column column){
                                projectionPush.put(column.getFullyQualifiedName().toUpperCase(), column);
                            }
                        }
                    }


                    first = recursiveConstructJoin(fromItem, expression, joins, projectionPush, materialized);
                }


            }

            Operator second;

            if(groupBy == null && !(selectItems.get(selectItems.size() - 1).getExpression() instanceof Function)){
                second = first;
            }else{
                second = new SumOperator(groupBy, selectItems, first);
            }

            Operator third;

            boolean ordered = false;

            if(orderByElements == null){
                third = second;
            }else{
                ordered = true;
                third = new SortOperator(orderByElements, second);
            }

            Operator fourth;

            if(selectItems.size() == 1 && selectItems.get(0).getExpression() instanceof AllColumns){
                fourth = third;
            }else{
                fourth = new ProjectOperator(selectItems, third);
            }

            Operator fifth;

            if(distinct == null){
                fifth = fourth;
            }else{
                fifth = new DuplicateEliminationOperator(fourth, ordered);
            }

            return fifth;

//            if(fromItem != null && selectItems != null){
//                //判断是否是project：
//
//                if (selectItems.size() == 1){
//                    if(selectItems.get(0).getExpression() instanceof AllColumns){
//                        //使用Scan Operator
//                        if(where == null){
//                            //使用Scan Operator
//                            if(joins == null){
//                                if(orderByElements == null){
//                                    return new ScanOperator(fromItem);
//                                }else{
//                                    return new SortOperator(fromItem, orderByElements);
//                                }
//
//                            }else{
//                                if(orderByElements == null){
//                                    return new JoinOperator(fromItem, null, joins);
//                                }else{
//                                    return new SortOperator(fromItem, null, joins, orderByElements);
//                                }
//
//                            }
//
//                        }else{
//                            //使用Select Operator
//                            if(joins == null){
//                                if(orderByElements == null){
//                                    return new SelectOperator(fromItem, where);
//                                }else{
//                                    return new SortOperator(fromItem, where, orderByElements);
//                                }
//
//                            }else{
//                                if(orderByElements == null){
//                                    return new JoinOperator(fromItem, where, joins);
//                                }else{
//                                    return new SortOperator(fromItem, where, joins, orderByElements);
//                                }
//
//                            }
//
//                        }
//                    }else{
//                        //使用Project Operator
//                        if(joins == null){
//                            if(orderByElements == null){
//                                return new ProjectOperator(fromItem, where, selectItems);
//                            }else{
//                                return new SortOperator(fromItem, where, selectItems, null, orderByElements);
//                            }
//
//                        }else{
//                            if(orderByElements == null){
//                                return new ProjectOperator(fromItem, where, selectItems, joins);
//                            }
//                            else{
//                                return new SortOperator(fromItem, where, selectItems, joins, orderByElements);
//                            }
//                        }
//                    }
//                }else{
//                    if(joins == null){
//                        if(orderByElements == null){
//                            return new ProjectOperator(fromItem, where, selectItems);
//                        }else{
//                            return new SortOperator(fromItem, where, selectItems, null, orderByElements);
//                        }
//                    }else{
//                        if(orderByElements == null){
//                            return new ProjectOperator(fromItem, where, selectItems, joins);
//                        }
//                        else{
//                            return new SortOperator(fromItem, where, selectItems, joins, orderByElements);
//                        }
//                    }
//                }
//
//            }else{
//                throw new NullPointerException("The table or column be specified");
//            }
//
//
//        }else{
//            throw new NullPointerException("The statement is null");
//        }

    }
        return null;
    }

//    private Operator recursiveConstructJoin(FromItem fromItem, Expression expression, List<SelectItem<?>> selectItems, List<Join> joins) {
//        JoinExpressionDeParser joinExpressionDeParser = new JoinExpressionDeParser();
//        String tableName;
//        if(joins.get(0).getFromItem().getAlias() == null){
//            tableName = joins.get(0).toString().toUpperCase();
//            joinExpressionDeParser.setTuple(tableName);
//        }else{
//            tableName = joins.get(0).getFromItem().getAlias().toString().trim().toUpperCase();
//            joinExpressionDeParser.setTuple(tableName);
//        }
//
//        List<SelectItem<?>> leftItems = new ArrayList<>();
//        List<SelectItem<?>> rightItems = new ArrayList<>();
//
//        for(SelectItem<?> item : selectItems){
//            String column = item.toString().toUpperCase();
//            String columnTable = column.split("\\.")[0];
//
//            if(columnTable.equals(tableName)){
//                rightItems.add(item);
//            }else{
//                leftItems.add(item);
//            }
//        }
//
//        Expression expressionSingle = null;
//        Expression otherExpression = null;
//        if(expression != null){
//            expression.accept(joinExpressionDeParser);
//            expressionSingle = joinExpressionDeParser.getThisExpressionSingle();
//            otherExpression = joinExpressionDeParser.getOtherExpression();
//        }
//
//        Expression expressionJoin = joinExpressionDeParser.getThisExpressionJoin();
//
//        Operator left;
//        Operator right;
//
//        //如果joins的长度为1，则不再创建左深连接树，left和right都变成Select Operator
//        if(joins.size() == 1){
//            left = constructScanSelectProject(fromItem, otherExpression, leftItems);
//            right = constructScanSelectProject(joins.get(0).getFromItem(), expressionSingle, rightItems);
//
//        }
//
//        //如果joins的长度不为1，则获取并移除joins的最后一个table，为其创建select operator；为left则是新的join operator
//        else{
//            Join join = joins.remove(joins.size() - 1);
//            left = recursiveConstructJoin(fromItem, otherExpression, leftItems, joins);
//            right = constructScanSelectProject(join.getFromItem(), expressionSingle, rightItems);
//        }
//
//        return new JoinOperator(expressionJoin, left, right);
//
//    }

    /**
     * recursively construct the binary join operator from the component
     * @param fromItem the left table
     * @param expression the join condition expression
     * @param joins the table need to join
     * @param materialized whether apply the materialization to optimize the query plan
     * @return the constructed join operator
     */
    private Operator recursiveConstructJoin(FromItem fromItem, Expression expression, List<Join> joins, boolean materialized) {
        JoinExpressionDeParser joinExpressionDeParser = new JoinExpressionDeParser();
        String tableName;
        if(joins.get(joins.size() - 1).getFromItem().getAlias() == null){
            tableName = joins.get(joins.size() - 1).toString().toUpperCase();
            joinExpressionDeParser.setTuple(tableName);
        }else{
            tableName = joins.get(joins.size() - 1).getFromItem().getAlias().toString().trim().toUpperCase();
            joinExpressionDeParser.setTuple(tableName);
        }

        Expression expressionSingle = null;
        Expression otherExpression = null;
        if(expression != null){
            expression.accept(joinExpressionDeParser);
            expressionSingle = joinExpressionDeParser.getThisExpressionSingle();
            otherExpression = joinExpressionDeParser.getOtherExpression();
        }

        Expression expressionJoin = joinExpressionDeParser.getThisExpressionJoin();

        Operator left;
        Operator right;

        //如果joins的长度为1，则不再创建左深连接树，left和right都变成Select Operator
        if(joins.size() == 1){
            left = constructScanSelect(fromItem, otherExpression, false);
            right = constructScanSelect(joins.get(0).getFromItem(), expressionSingle, materialized);

        }

        //如果joins的长度不为1，则获取并移除joins的最后一个table，为其创建select operator；为left则是新的join operator
        else{
            Join join = joins.remove(joins.size() - 1);
            left = recursiveConstructJoin(fromItem, otherExpression, joins, materialized);
            right = constructScanSelect(join.getFromItem(), expressionSingle, materialized);
        }

        return new JoinOperator(expressionJoin, left, right);

    }

    /**
     * recursively construct the binary join operator from the component and apply projection pushdown and selection pushdown
     * @param fromItem the left table
     * @param expression the join condition
     * @param joins the tables need to join
     * @param projections the required columns
     * @param materialized whether apply the materialization
     * @return the constructed join operator
     */
    private Operator recursiveConstructJoin(FromItem fromItem, Expression expression, List<Join> joins, Map<String, Column> projections, boolean materialized) {
        JoinExpressionDeParser joinExpressionDeParser = new JoinExpressionDeParser();
        String rightTableName;
        if(joins.get(joins.size() - 1).getFromItem().getAlias() == null){
            rightTableName = joins.get(joins.size() - 1).toString().toUpperCase();
            joinExpressionDeParser.setTuple(rightTableName);
        }else{
            rightTableName = joins.get(joins.size() - 1).getFromItem().getAlias().toString().trim().toUpperCase();
            joinExpressionDeParser.setTuple(rightTableName);
        }

        Expression expressionSingle = null;
        Expression otherExpression = null;
        if(expression != null){
            expression.accept(joinExpressionDeParser);
            expressionSingle = joinExpressionDeParser.getThisExpressionSingle();
            otherExpression = joinExpressionDeParser.getOtherExpression();
        }

        Expression expressionJoin = joinExpressionDeParser.getThisExpressionJoin();
        Map<String, Column> result = joinExpressionDeParser.getRequired();
        result.putAll(projections);

        List<SelectItem<?>> leftItems = new ArrayList<>();
        List<SelectItem<?>> rightItems = new ArrayList<>();

        Set<String> keys =  result.keySet();
        List<String> deleted = new ArrayList<>();
        for(String columnName : keys){
            SelectItem<?> selectItem = new SelectItem<>(result.get(columnName));
            if(columnName.split("\\.")[0].equals(rightTableName)){

                rightItems.add(selectItem);
//                result.remove(columnName);
                deleted.add(columnName);
            }else{
                leftItems.add(selectItem);
            }
        }


        for(String columnName : deleted){
            result.remove(columnName);
        }



//        for(String columnName : result){
//            Column column = new Column();
//            new Table("")
//        }

        Operator left;
        Operator right;

        //如果joins的长度为1，则不再创建左深连接树，left和right都变成Select Operator
        if(joins.size() == 1){
            left = constructScanSelectProject(fromItem, otherExpression, leftItems, false);
            right = constructScanSelectProject(joins.get(0).getFromItem(), expressionSingle, rightItems, materialized);
        }

        //如果joins的长度不为1，则获取并移除joins的最后一个table，为其创建select operator；为left则是新的join operator
        else{
            Join join = joins.remove(joins.size() - 1);
            left = recursiveConstructJoin(fromItem, otherExpression,  joins, result, materialized);
            right = constructScanSelectProject(join.getFromItem(), expressionSingle, rightItems, materialized);
        }

        return new JoinOperator(expressionJoin, left, right);

    }


    /**
     * construct the child operator of the join operator without projection
     * @param fromItem one of the table
     * @param expression the expression that can be applied to the table
     * @param materialized whether to materialize the selection result
     * @return the constructed operator
     */
    private Operator constructScanSelect(FromItem fromItem, Expression expression, boolean materialized){
        Operator first;
        Operator scan = new ScanOperator(fromItem);
        Operator selection = null;
        if(expression != null){
            if(materialized){
                selection = new SelectOperatorMaterialized(expression, scan);
            }else{
                selection = new SelectOperator(expression, scan);
            }
        }


        if(selection == null){
            first = scan;
        }else{
            first = selection;
        }



        return first;
    }

    /**
     * construct the child operator of the join operator with projection
     * @param fromItem one of the table
     * @param expression the expression that can be applied to the table
     * @param selectItems the columns need to project
     * @param materialized whether to materialize the selection result
     * @return the constructed operator
     */
    private Operator constructScanSelectProject(FromItem fromItem, Expression expression, List<SelectItem<?>> selectItems, boolean materialized){
        Operator first;
        Operator scan = new ScanOperator(fromItem);
        Operator selection = null;
        if(expression != null){
            if(materialized){
                selection = new SelectOperatorMaterialized(expression, scan);
            }else{
                selection = new SelectOperator(expression, scan);
            }

        }

        if(selectItems != null){
            if(selection == null){
                if(materialized){
                    first = new ProjectOperatorMaterialized(selectItems, scan);
                }else{
                    first = new ProjectOperator(selectItems, scan);
                }

            }else{
                if(materialized){
                    first = new ProjectOperatorMaterialized(selectItems, selection);
                }else{
                    first = new ProjectOperator(selectItems, selection);
                }

            }
        }else{
            if(selection == null){
                first = scan;
            }else{
                first = selection;
            }
        }




        return first;
    }
}
