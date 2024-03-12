package ed.inf.adbs.lightdb;

import Interpreter.QueryConstructor;
import Interpreter.TopInterpreter;
import Operator.*;
import com.sun.jdi.BooleanValue;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.IsBooleanExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.SimpleNode;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.junit.Test;
import pojo.Catalog;
import pojo.Parser.JoinExpressionDeParser;
import pojo.PropertyInTest;
import pojo.Tuple;

import javax.security.auth.Refreshable;
import java.io.*;
import java.util.*;

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
//		Properties properties = LightDB.loadProperties();
		String filename = "samples/input/mytest.sql";
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

		FromItem tableName = null;
		if (statement != null) {
			Select select = (Select) statement;
			PlainSelect plainSelect = select.getPlainSelect();
			tableName = plainSelect.getFromItem();
			System.out.println(tableName);
		}
		Catalog catalog = Catalog.getInstance();



		Operator operator = new ScanOperator(tableName);
		operator.dump();
	}

	@Test
	public void SelectOperatorSimpleTest() throws FileNotFoundException, JSQLParserException {
		Statement statement = CCJSqlParserUtil.parse(new FileReader(PropertyInTest.properties.getProperty("input-path")));
//            Statement statement = CCJSqlParserUtil.parse("SELECT * FROM Boats");

		FromItem tableName = null;
		Expression expression = null;
		if (statement != null) {
			Select select = (Select) statement;
			PlainSelect plainSelect = select.getPlainSelect();
			tableName = plainSelect.getFromItem();
			expression = plainSelect.getWhere();

		}


		Catalog catalog = Catalog.getInstance();



//		Operator operator = new SelectOperator(tableName, expression);
		Operator operator = new QueryConstructor().constructor(statement);
		operator.dump();
	}

	@Test
	public void ProjectOperatorSimpleTest() throws FileNotFoundException, JSQLParserException {
//		Statement statement = CCJSqlParserUtil.parse(new FileReader(PropertyInTest.properties.getProperty("input-path")));
            Statement statement = CCJSqlParserUtil.parse("SELECT Boats.e,Boats.D FROM BOATS where Boats.F = 8");

		FromItem tableName = null;
		Expression expression = null;
		List<SelectItem<?>> selectItems = null;
		if (statement != null) {
			Select select = (Select) statement;
			PlainSelect plainSelect = select.getPlainSelect();
			tableName = plainSelect.getFromItem();
			expression = plainSelect.getWhere();
			selectItems = plainSelect.getSelectItems();
		}

		Catalog catalog = Catalog.getInstance();



//		Operator operator = new ProjectOperator(tableName, expression, selectItems);
		Operator operator = new QueryConstructor().constructor(statement);
		operator.dump();
	}

	@Test
	public void JoinOperatorSimpleTest() throws FileNotFoundException, JSQLParserException {
//		Statement statement = CCJSqlParserUtil.parse(new FileReader(PropertyInTest.properties.getProperty("input-path")));
//		Statement statement = CCJSqlParserUtil.parse("SELECT S.A, B.E FROM Sailors S, Reserves R, Boats B where S.A = R.G;");
		Statement statement = CCJSqlParserUtil.parse("SELECT DISTINCT Sailors.A, Boats.E FROM Sailors, Reserves, Boats where Sailors.A = Reserves.G group by Sailors.A, Boats.E order by Boats.E,Sailors.A;");
//		Statement statement = CCJSqlParserUtil.parse("SELECT E.E, B.E FROM Boats E, Boats B where E.E = B.E;");

		TopInterpreter topInterpreter = new TopInterpreter();
		topInterpreter.setStatement(statement);
		topInterpreter.dump();


	}

	@Test
	public void testDuplicate() throws JSQLParserException {
		Set<Tuple> tuples = new HashSet<>();
		Statement statement = CCJSqlParserUtil.parse("SELECT Sailors.B FROM Sailors");

		QueryConstructor queryConstructor = new QueryConstructor();
		Operator operator = queryConstructor.constructor(statement);

		Tuple tuple = operator.getNextTuple();
		while(tuple != null){
			tuples.add(tuple);
			tuple = operator.getNextTuple();
		}

		for(Tuple mytuple : tuples){
//			System.out.println(mytuple);
			System.out.println(mytuple.getValues());
		}

	}

	@Test
	public void idonknow() throws FileNotFoundException, JSQLParserException {
//		Statement statement = CCJSqlParserUtil.parse(new FileReader(PropertyInTest.properties.getProperty("input-path")));
            Statement statement = CCJSqlParserUtil.parse("SELECT Boats.d FROM Boats");

		FromItem tableName = null;
		Expression expression = null;
		SelectItem selectItem = null;
		if (statement != null) {
			Select select = (Select) statement;
			PlainSelect plainSelect = select.getPlainSelect();
			tableName = plainSelect.getFromItem();
			expression = plainSelect.getWhere();
			List<SelectItem<?>> selectItems = plainSelect.getSelectItems();
			for(SelectItem<?> item : selectItems){
				System.out.println(item.getExpression() instanceof Column);
				System.out.println(item.getExpression() instanceof AllColumns);
			}

//			Operator operator = new ProjectOperator(tableName, null, selectItems);
			Operator operator = new QueryConstructor().constructor(statement);
			operator.dump();
		}

		Catalog catalog = Catalog.getInstance();




	}


	@Test
	public void idonkknow2() throws FileNotFoundException, JSQLParserException {
		Statement statement = CCJSqlParserUtil.parse("SELECT Boats.id FROM Boats,Sheep,test where Boats.id = Sheep.id and Boats.id = test.id and Sheep.id = test.id");
		Select select = (Select) statement;
		PlainSelect plainSelect = select.getPlainSelect();
		List<Join> joins = plainSelect.getJoins();
//		joins.remove(0);
		System.out.println(joins);
		AndExpression where =(AndExpression) plainSelect.getWhere();
		System.out.println(where.getLeftExpression());
		System.out.println(where.getRightExpression());
		System.out.println(where.withLeftExpression(where.getRightExpression()));

		System.out.println(new AndExpression().withLeftExpression(new LongValue(1)).withRightExpression(new LongValue(1)).withRightExpression(new LongValue(2)));

	}

	@Test
	public void testParser() throws JSQLParserException {
		Tuple left = new Tuple();
		left.setTableName("whatever");
		Tuple right = new Tuple();
		right.setTableName("TEST");

		Statement statement = CCJSqlParserUtil.parse("SELECT Boats.id FROM Boats,Sheep,test where test.id = boats.id and test.id = test.id and 1 = 2 and test.id = 2 and Boats.id = 2");

		Select select = (Select) statement;
		PlainSelect plainSelect = select.getPlainSelect();

		JoinExpressionDeParser joinExpressionDeParser = new JoinExpressionDeParser();
		joinExpressionDeParser.setTuple(right.tableName);

		plainSelect.getWhere().accept(joinExpressionDeParser);
		System.out.println(joinExpressionDeParser.getThisExpressionSingle());
		System.out.println(joinExpressionDeParser.getThisExpressionJoin());
		System.out.println(joinExpressionDeParser.getOtherExpression());

		List<String> column1 = new ArrayList<>();
		column1.add("1");
		column1.add("2");

		List<String> column2 = new ArrayList<>();
		column2.add("3");
		column2.add("4");
		column1.addAll(column2);
		System.out.println(column1);
	}

	@Test
	public void testOrder() throws JSQLParserException {
//		Statement statement = CCJSqlParserUtil.parse("SELECT Sailors.A, Boats.E FROM Sailors, Reserves, Boats where Sailors.A = Reserves.G order by Sailors.A, Boats.E;");
//		PlainSelect plainSelect = ((Select)statement).getPlainSelect();
//		SortOperator sortOperator = new SortOperator(plainSelect.getFromItem(), plainSelect.getWhere(), plainSelect.getSelectItems(), plainSelect.getJoins(), plainSelect.getOrderByElements());
//		sortOperator.dump();

		Statement statement = CCJSqlParserUtil.parse("SELECT sum(Sailors.A*Sailors.A*Sailors.A) FROM Sailors, Reserves, Boats " +
				"where Sailors.A = Reserves.G group by Sailors.A, Boats.E order by Sailors.A, Boats.E;");
		PlainSelect plainSelect = ((Select)statement).getPlainSelect();
		List<SelectItem<?>> selectItems = plainSelect.getSelectItems();
		List<OrderByElement> orderByElements = plainSelect.getOrderByElements();
//		if(selectItems.get(0).getExpression() instanceof Function){
//			Function function = (Function) selectItems.get(0).getExpression();
//			System.out.println(function.getParameters().get(0) instanceof Multiplication);
//			System.out.println(function.getParameters().get(0) instanceof Column);
//			Multiplication multiplication = (Multiplication) function.getParameters().get(0);
//			System.out.println(multiplication.getLeftExpression().toString().equals(orderByElements.get(0).getExpression().toString()));
////			System.out.println(((Multiplication)function.getParameters().get(0)).getLeftExpression());
//
//		}
//		System.out.println(plainSelect.getGroupBy().getGroupByExpressionList().get(0).toString());
//		TopInterpreter top = new TopInterpreter();
//		top.setStatement(statement);
//		top.dump();

		Operator operator = new QueryConstructor().constructor(statement);
		operator.dump();
		operator.reset();
		System.out.println("---------------------------");
		operator.dump();

	}

	@Test
	public void testProvidedSQL() throws FileNotFoundException, JSQLParserException {
		Statement statement = CCJSqlParserUtil.parse(new FileReader(PropertyInTest.properties.getProperty("input-path")));

		TopInterpreter topInterpreter = new TopInterpreter();
		topInterpreter.setStatement(statement);
		topInterpreter.dump();

	}

	@Test
	public void buffer() throws IOException, JSQLParserException {
		Statement statement = CCJSqlParserUtil.parse("select * from Sailors S where true");
		PlainSelect plainSelect = ((Select) statement).getPlainSelect();
		Expression where = plainSelect.getWhere();
		Operator operator = new QueryConstructor().constructor(statement);

	}

	@Test
	public void additionalSum() throws JSQLParserException {
		Statement statement = CCJSqlParserUtil.parse("select *,sum(S.A) from Sailors S group by S.A, S.B, S.C");
		Operator operator = new QueryConstructor().constructor(statement);
		operator.dump();

	}

	@Test
	public void testLoop() throws JSQLParserException {
		Statement statement = CCJSqlParserUtil.parse("select sum(S1.C*S2.B) from Sailors S1, Sailors S2, Sailors S3 where S1.A <= 4 and S2.B = 100 and S3.C >= 100 group by S1.C order by S1.C");
		Operator operator = new QueryConstructor().constructor(statement);
		operator.dump();
	}


}
