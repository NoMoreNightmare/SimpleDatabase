package Operator;
import Tuple.Tuple;

public interface Operator {
    public Tuple getNextTuple();

    public void reset();

    public void dump();
}
