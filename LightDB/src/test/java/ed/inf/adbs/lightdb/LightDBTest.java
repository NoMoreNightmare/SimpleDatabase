package ed.inf.adbs.lightdb;

import Operator.Operator;
import Operator.ScanOperator;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.junit.Test;
import pojo.Catalog;
import pojo.PropertyInTest;
import pojo.Tuple;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

/**
 * Unit tests for LightDB.
 */
public class LightDBTest {
	
	/**
	 * Rigorous Test :-)
	 */
	@Test
	public void shouldAnswerWithTrue() {
		assertTrue(true);
	}

	@Test
	public void test() throws IOException, JSQLParserException {
		Properties properties = LightDB.loadProperties();
		String filename = properties.getProperty("input-path") + "mytest.sql";
		Statement statement = CCJSqlParserUtil.parse(new FileReader(filename));
//            Statement statement = CCJSqlParserUtil.parse("SELECT * FROM Boats");
		if (statement != null) {
			System.out.println("Read statement: " + statement);
			Select select = (Select) statement;
			System.out.println("Select body is " + select.getSelectBody());

			PlainSelect plainSelect = select.getPlainSelect();
			FromItem fromItem = plainSelect.getFromItem();
			System.out.println(fromItem);

//			GroupByElement groupBy = plainSelect.getGroupBy();
//			System.out.println(groupBy.getGroupByExpressions());
//
//			Expression having = plainSelect.getHaving();
//			System.out.println(having.toString());

			Distinct distinct = plainSelect.getDistinct();
			System.out.println(distinct);

//			List<Join> joins = plainSelect.getJoins();
//			System.out.println(joins.get(0).getFromItem() + " " + joins.get(0).getOnExpressions() + " " + joins.get(0).getRightItem());

			List<SelectItem<?>> selectItems = plainSelect.getSelectItems();
			System.out.println(selectItems);

			Expression where = plainSelect.getWhere();
			System.out.println(where);
			System.out.println(where instanceof AndExpression);
			if(where instanceof AndExpression){
				AndExpression andExpression = (AndExpression) where;
				System.out.println(andExpression.getLeftExpression());
				System.out.println(andExpression.getRightExpression());

				Expression left = andExpression.getLeftExpression();
				Expression right = andExpression.getRightExpression();

				if(left instanceof EqualsTo){
					EqualsTo leftEqual = (EqualsTo) left;
					System.out.println(leftEqual.getLeftExpression() + ":" + leftEqual.getRightExpression());
					System.out.println(leftEqual.getLeftExpression() instanceof Column);
					System.out.println(leftEqual.getRightExpression() instanceof LongValue);
				}
			}

			List<OrderByElement> orderByElements = plainSelect.getOrderByElements();
			System.out.println(orderByElements);
		}
	}

	@Test
	public void ScanOperatorSimpleTest() throws FileNotFoundException, JSQLParserException {
		Statement statement = CCJSqlParserUtil.parse(new FileReader(PropertyInTest.properties.getProperty("input-path")));
//            Statement statement = CCJSqlParserUtil.parse("SELECT * FROM Boats");

		String tableName = "Boats";
		if (statement != null) {
			Select select = (Select) statement;
			PlainSelect plainSelect = select.getPlainSelect();
			tableName = plainSelect.getFromItem().toString();
			System.out.println(tableName);
		}
		Catalog catalog = Catalog.getInstance();



		Operator operator = new ScanOperator(tableName);
		operator.dump();
	}


}
