package cn.abelib.minebatis.session;

import cn.abelib.minebatis.datasource.UnpooledDataSource;
import cn.abelib.minebatis.transaction.JdbcTransaction;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author abel.huang
 * @date 2026/4/11
 * SqlSessionFactory 测试
 */
public class SqlSessionFactoryTest {

    private Configuration configuration;
    private DefaultSqlSessionFactory sqlSessionFactory;

    @Before
    public void setUp() {
        configuration = new Configuration();
        configuration.setDataSource(new UnpooledDataSource(
                "com.mysql.jdbc.Driver",
                "jdbc:mysql://localhost:3306/test",
                "root",
                "password"
        ));
        sqlSessionFactory = new DefaultSqlSessionFactory(configuration);
    }

    @Test
    public void testOpenSessionDefault() {
        SqlSession session = sqlSessionFactory.openSession();
        assertNotNull(session);
        assertNotNull(session.getConfiguration());
    }

    @Test
    public void testOpenSessionWithAutoCommitTrue() {
        SqlSession session = sqlSessionFactory.openSession(true);
        assertNotNull(session);
    }

    @Test
    public void testOpenSessionWithAutoCommitFalse() {
        SqlSession session = sqlSessionFactory.openSession(false);
        assertNotNull(session);
    }

    @Test
    public void testNewTransactionDefault() {
        JdbcTransaction transaction = (JdbcTransaction) configuration.newTransaction();
        assertFalse("Default autoCommit should be false", transaction.isAutoCommit());
    }

    @Test
    public void testNewTransactionWithAutoCommitTrue() {
        JdbcTransaction transaction = (JdbcTransaction) configuration.newTransaction(true);
        assertTrue("autoCommit should be true", transaction.isAutoCommit());
    }

    @Test
    public void testNewTransactionWithAutoCommitFalse() {
        JdbcTransaction transaction = (JdbcTransaction) configuration.newTransaction(false);
        assertFalse("autoCommit should be false", transaction.isAutoCommit());
    }
}
