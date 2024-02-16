package ed.inf.adbs.lightdb;

import static org.junit.Assert.assertTrue;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

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
		String filename = properties.getProperty("input-path") + "mytest2.sql";
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

			List<Join> joins = plainSelect.getJoins();
			System.out.println(joins.get(0).getFromItem() + " " + joins.get(0).getOnExpressions() + " " + joins.get(0).getRightItem());

			List<SelectItem<?>> selectItems = plainSelect.getSelectItems();
			System.out.println(selectItems);

			Expression where = plainSelect.getWhere();
			System.out.println(where);

			List<OrderByElement> orderByElements = plainSelect.getOrderByElements();
			System.out.println(orderByElements);
		}
	}
}
