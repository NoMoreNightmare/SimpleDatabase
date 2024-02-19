package Interpreter;
import Operator.*;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryConstructor {
    public Operator constructor(Statement statement){
        if (statement != null) {
            Select select = (Select) statement;

            PlainSelect plainSelect = select.getPlainSelect();
            FromItem fromItem = plainSelect.getFromItem();

            Expression where = plainSelect.getWhere();

            List<SelectItem<?>> selectItems = plainSelect.getSelectItems();

            List<Join> joins = plainSelect.getJoins();

            Map<String, String> aliases = new HashMap<>();

			GroupByElement groupBy = plainSelect.getGroupBy();
//			Expression having = plainSelect.getHaving();

            Distinct distinct = plainSelect.getDistinct();


            List<OrderByElement> orderByElements = plainSelect.getOrderByElements();

            if(fromItem != null && selectItems != null){
                //判断是否是project：
                if (selectItems.size() == 1){
                    if(selectItems.get(0).getExpression() instanceof AllColumns){
                        //使用Scan Operator
                        if(where == null){
                            //使用Scan Operator
                            if(joins == null){
                                return new ScanOperator(fromItem);
                            }else{
                                return new JoinOperator(fromItem, null, joins);
                            }

                        }else{
                            //使用Select Operator
                            if(joins == null){
                                return new SelectOperator(fromItem, where);
                            }else{
                                return new JoinOperator(fromItem, where, joins);
                            }

                        }
                    }else{
                        //使用Project Operator
                        if(joins == null){
                            return new ProjectOperator(fromItem, where, selectItems);
                        }else{
                            return new ProjectOperator(fromItem, where, selectItems, joins);
                        }
                    }
                }

            }else{
                throw new NullPointerException("The table or column be specified");
            }


        }else{
            throw new NullPointerException("The statement is null");
        }

        return null;
    }
}
