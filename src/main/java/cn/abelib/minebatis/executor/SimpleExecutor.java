package cn.abelib.minebatis.executor;

import cn.abelib.minebatis.session.Configuration;
import cn.abelib.minebatis.cache.CacheKey;
import cn.abelib.minebatis.mapping.MappedStatement;
import cn.abelib.minebatis.executor.statement.StatementHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public <E> List<E> query(MappedStatement ms, Object parameter, CacheKey cacheKey) throws SQLException {
        return null;
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter) throws SQLException {
        return doQuery(ms, parameter);
    }

    private <E> List<E> doQuery(MappedStatement ms, Object parameter) throws SQLException {
        Statement stmt = null;
        try {
            Configuration configuration = ms.getConfiguration();
            Connection connection = configuration.getConnection();
            StatementHandler handler = configuration.newStatementHandler(ms, parameter);
            stmt = prepareStatement(handler, connection);
            return handler.query(stmt);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (Objects.nonNull(stmt)) {
                stmt.close();
            }
        }
        return new ArrayList<>(0);
    }

    /**
     * 创建Statement的入口
     * @param handler
     * @return
     */
    private Statement prepareStatement(StatementHandler handler, Connection connection) throws SQLException, IllegalAccessException {
        Statement stmt = handler.prepare(connection);
        handler.parameterize(stmt);
        return stmt;
    }
}
