package Parser;
import Operator.*;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.util.List;

public class QueryConstructor {
    public Operator constructor(Statement statement){
        if (statement != null) {
            Select select = (Select) statement;

            PlainSelect plainSelect = select.getPlainSelect();
            FromItem fromItem = plainSelect.getFromItem();

			GroupByElement groupBy = plainSelect.getGroupBy();
//			Expression having = plainSelect.getHaving();

            Distinct distinct = plainSelect.getDistinct();

			List<Join> joins = plainSelect.getJoins();
            List<SelectItem<?>> selectItems = plainSelect.getSelectItems();

            Expression where = plainSelect.getWhere();


            List<OrderByElement> orderByElements = plainSelect.getOrderByElements();

            if(fromItem != null && selectItems != null){
                //判断是否是project：现在假设没有select *, column_i from MyTable;这种情况
                if (selectItems.size() == 1){
                    if(selectItems.get(0).getExpression() instanceof AllColumns){
                        //使用Scan Operator
                        if(where == null){
                            //使用Scan Operator
                            return new ScanOperator(fromItem.toString());
                        }else{
                            //使用Select Operator
                            return new SelectOperator(fromItem.toString(), where);
                        }
                    }else{
                        //使用Project Operator
                        if(where == null){
                            //使用Scan Operator
                            return new ProjectOperator(fromItem.toString(), selectItems);
                        }else{
                            //使用Select Operator
                            return new ProjectOperator(fromItem.toString(), where, selectItems);
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
