package cn.abelib.minebatis.session;

import cn.abelib.minebatis.executor.Executor;
import cn.abelib.minebatis.transaction.Transaction;

/**
 * @author abel.huang
 * @date 2020/8/6 13:42
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {

    private final Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SqlSession openSession() {
        Transaction transaction = configuration.newTransaction();
        Executor executor = configuration.newExecutor(transaction);
        return new DefaultSqlSession(configuration, executor);
    }

    @Override
    public SqlSession openSession(boolean autoCommit) {
        Transaction transaction = configuration.newTransaction(autoCommit);
        Executor executor = configuration.newExecutor(transaction);
        return new DefaultSqlSession(configuration, executor);
    }
}
