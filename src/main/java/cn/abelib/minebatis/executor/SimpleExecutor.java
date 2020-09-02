package cn.abelib.minebatis.executor;

import cn.abelib.minebatis.Configuration;
import cn.abelib.minebatis.cache.CacheKey;
import cn.abelib.minebatis.mapping.MappedStatement;
import cn.abelib.minebatis.executor.statement.StatementHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author abel.huang
 * @date 2020/8/7 20:42
 *
 */
public class SimpleExecutor implements Executor {
    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        return 0;
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, CacheKey cacheKey, String boundSql) throws SQLException {
        return null;
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, String boundSql) throws SQLException {
        return doQuery(ms, parameter, boundSql);
    }

    private <E> List<E> doQuery(MappedStatement ms, Object parameter, String boundSql) throws SQLException {
        Statement stmt = null;
        try {
            Configuration configuration = ms.getConfiguration();
            StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, boundSql);
            stmt = prepareStatement(handler);
            return handler.query(stmt);
        } finally {
            stmt.close();
        }
    }

    /**
     * 创建Statement的入口
     * @param handler
     * @return
     */
    private Statement prepareStatement(StatementHandler handler) {
        Statement stmt;
        /**
         * 获得数据库连接
         */
        Connection connection = getConnection();
        stmt = handler.prepare(connection);
        //
        handler.parameterize(stmt);
        return stmt;
    }

    private Connection getConnection() {
        return null;
    }
}
