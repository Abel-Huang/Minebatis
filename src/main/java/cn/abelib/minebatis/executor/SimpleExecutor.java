package cn.abelib.minebatis.executor;

import cn.abelib.minebatis.cache.CacheKey;
import cn.abelib.minebatis.mapping.MappedStatement;
import cn.abelib.minebatis.executor.statement.StatementHandler;
import cn.abelib.minebatis.session.Configuration;
import cn.abelib.minebatis.transaction.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author abel.huang
 * @date 2020/8/7 20:42
 * 简单执行器实现
 */
public class SimpleExecutor implements Executor {

    private final Transaction transaction;

    public SimpleExecutor(Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        Statement stmt = null;
        try {
            Configuration configuration = ms.getConfiguration();
            Connection connection = transaction.getConnection();
            StatementHandler handler = configuration.newStatementHandler(ms, parameter);
            stmt = prepareStatement(handler, connection);
            return handler.update(stmt);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Update failed", e);
        } finally {
            if (Objects.nonNull(stmt)) {
                stmt.close();
            }
        }
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, CacheKey cacheKey) throws SQLException {
        return doQuery(ms, parameter);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter) throws SQLException {
        return doQuery(ms, parameter);
    }

    private <E> List<E> doQuery(MappedStatement ms, Object parameter) throws SQLException {
        Statement stmt = null;
        try {
            Configuration configuration = ms.getConfiguration();
            Connection connection = transaction.getConnection();
            StatementHandler handler = configuration.newStatementHandler(ms, parameter);
            stmt = prepareStatement(handler, connection);
            return handler.query(stmt);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Query failed", e);
        } finally {
            if (Objects.nonNull(stmt)) {
                stmt.close();
            }
        }
    }

    /**
     * 创建 Statement 并设置参数
     */
    private Statement prepareStatement(StatementHandler handler, Connection connection) throws SQLException, IllegalAccessException {
        Statement stmt = handler.prepare(connection);
        handler.parameterize(stmt);
        return stmt;
    }

    @Override
    public void commit() {
        try {
            transaction.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void rollback() {
        try {
            transaction.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            transaction.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
