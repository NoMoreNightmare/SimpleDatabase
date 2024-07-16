package TransactionManager;

public interface TransactionManager {
    long begin(); //开启事务
    void commit(long xid);
    void rollback(long xid);
    boolean isActive(long xid);
    boolean isCommitted(long xid);
    boolean isAborted(long xid);

    void close(); //关闭TM
}
