package tools;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.UnsupportedStatement;

import java.util.ArrayList;
import java.util.List;

public class Begin extends UnsupportedStatement implements Statement{
    public Begin(List<String> declarations) {
        super(declarations);
    }

    public Begin(String upfront, List<String> declarations) {
        super(upfront, declarations);
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    @Override
    public String toString() {
        return "BEGIN";
    }
}
