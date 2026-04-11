package cn.abelib.minebatis.transaction;

import cn.abelib.minebatis.session.Configuration;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author abel.huang
 * @date 2026/4/11
 * JDBC 事务实现
 */
public class JdbcTransaction implements Transaction {

    private final Configuration configuration;
    private Connection connection;
    private boolean autoCommit;

    public JdbcTransaction(Configuration configuration) {
        this.configuration = configuration;
    }

    public JdbcTransaction(Connection connection) {
        this.connection = connection;
        try {
            this.autoCommit = connection.getAutoCommit();
        } catch (SQLException e) {
            this.autoCommit = true;
        }
        this.configuration = null;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = configuration.getConnection();
            connection.setAutoCommit(autoCommit);
        }
        return connection;
    }

    @Override
    public void commit() throws SQLException {
        if (connection != null && !connection.getAutoCommit()) {
            connection.commit();
        }
    }

    @Override
    public void rollback() throws SQLException {
        if (connection != null && !connection.getAutoCommit()) {
            connection.rollback();
        }
    }

    @Override
    public void close() throws SQLException {
        if (connection != null) {
            rollback();
            connection.close();
            connection = null;
        }
    }
}
