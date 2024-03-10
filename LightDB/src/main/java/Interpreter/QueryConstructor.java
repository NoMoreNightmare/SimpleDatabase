package Interpreter;
import Operator.*;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import pojo.Parser.JoinExpressionDeParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryConstructor {
    public Operator constructor(Statement statement){
        if (statement != null) {
            Select select = (Select) statement;

            PlainSelect plainSelect = select.getPlainSelect();
            FromItem fromItem = plainSelect.getFromItem();

            Expression expression = plainSelect.getWhere();

            List<SelectItem<?>> selectItems = plainSelect.getSelectItems();

            List<Join> joins = plainSelect.getJoins();

			GroupByElement groupBy = plainSelect.getGroupBy();

//			Expression having = plainSelect.getHaving();

            Distinct distinct = plainSelect.getDistinct();


            List<OrderByElement> orderByElements = plainSelect.getOrderByElements();

            Operator first;

            if(joins == null){
                first = constructScanSelect(fromItem, expression);
            }else{
//                if(selectItems.size() == 1 && selectItems.get(0).getExpression() instanceof AllColumns){
//                    first = recursiveConstructJoin(fromItem, expression, selectItems, joins);
//                }else{
//                    List<SelectItem<?>> filter = selectItems;
//
//
//
////                    first = recursiveConstructJoin()
//                }

                first = recursiveConstructJoin(fromItem, expression, joins);
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
    private Operator recursiveConstructJoin(FromItem fromItem, Expression expression, List<Join> joins) {
        JoinExpressionDeParser joinExpressionDeParser = new JoinExpressionDeParser();
        String tableName;
        if(joins.get(0).getFromItem().getAlias() == null){
            tableName = joins.get(0).toString().toUpperCase();
            joinExpressionDeParser.setTuple(tableName);
        }else{
            tableName = joins.get(0).getFromItem().getAlias().toString().trim().toUpperCase();
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
            left = constructScanSelect(fromItem, otherExpression);
            right = constructScanSelect(joins.get(0).getFromItem(), expressionSingle);

        }

        //如果joins的长度不为1，则获取并移除joins的最后一个table，为其创建select operator；为left则是新的join operator
        else{
            Join join = joins.remove(joins.size() - 1);
            left = recursiveConstructJoin(fromItem, otherExpression,  joins);
            right = constructScanSelect(join.getFromItem(), expressionSingle);
        }

        return new JoinOperator(expressionJoin, left, right);

    }


    private Operator constructScanSelect(FromItem fromItem, Expression expression){
        Operator first;
        Operator scan = new ScanOperator(fromItem);
        Operator selection = null;
        if(expression != null){
            selection = new SelectOperator(expression, scan);
        }


        if(selection == null){
            first = scan;
        }else{
            first = selection;
        }


        return first;
    }

    private Operator constructScanSelectProject(FromItem fromItem, Expression expression, List<SelectItem<?>> selectItems){
        Operator first;
        Operator scan = new ScanOperator(fromItem);
        Operator selection = null;
        if(expression != null){
            selection = new SelectOperator(expression, scan);
        }

        if(selectItems != null){
            if(selection == null){
                first = new ProjectOperator(selectItems, scan);
            }else{
                first = new ProjectOperator(selectItems, selection);
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
