# LightDB ReadMe file
- - -
### Join expression extraction

The condition expression in `WHERE` will be separated in `JoinExpressionDeParser`. First, visit the AndExpression in condition, break them and visit these subexpressions. If the expression is not AndExpression, then it is a single condition expression. Second, check the columns used in that expression, if it only uses columns from left table, then concatenate the current expressions belongs to left table with this expression using `AndExpression`, and if it only uses columns from right table, concatenate the current expressions belongs to left table with this expression using AndExpression. Otherwise, the expression uses columns from both tables, then it should be concatenated the expression with expressions used for join.

### Optimization
First optimization approach applied is `selection pushdown`. When executing `join` operator, use `JoinExpressionDeParser` to extract the conditions expression belongs to each table or must be used for join. For those expression only belongs to single table, push them down to the corresponding `selection` operator.

Second optimization approach applied is implementing two different methods for `duplication` operator. If the records are ordered, then use list to remove duplication, otherwise use hashset.

Third optimization approach applied is `projection pushdown`. When executing `join` operator, the columns that will be used in the future operator like `projection`, `order`, `group by`, `WHERE` expression will be kept and pushed down to the `projection` operator for corresponding single table, other columns will be removed. 

Fourth optimization approach applied is `materialization`. It is applied to the right table used in the `join` operator. After `selection` and `projection` operator for the single table is applied, store the result in disk (a temporary file) to avoid repeating recalculating `selection` and `projection` for that table. 