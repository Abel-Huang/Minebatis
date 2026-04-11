package cn.abelib.minebatis.executor;

import cn.abelib.minebatis.cache.CacheKey;
import cn.abelib.minebatis.cache.PerpetualCache;
import cn.abelib.minebatis.mapping.MappedStatement;
import cn.abelib.minebatis.executor.statement.StatementHandler;
import cn.abelib.minebatis.session.Configuration;
import cn.abelib.minebatis.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author abel.huang
 * @date 2020/8/7 20:42
 * 简单执行器实现
 * 包含一级缓存（Session级别）
 */
public class SimpleExecutor implements Executor {

    private static final Logger log = LoggerFactory.getLogger(SimpleExecutor.class);

    private final Transaction transaction;
    /**
     * 一级缓存（Session级别）
     */
    private final PerpetualCache localCache;

    public SimpleExecutor(Transaction transaction) {
        this.transaction = transaction;
        this.localCache = new PerpetualCache("LocalCache");
    }

    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        // 执行更新操作时，清空一级缓存
        clearLocalCache();
        return doUpdate(ms, parameter);
    }

    protected int doUpdate(MappedStatement ms, Object parameter) throws SQLException {
        Statement stmt = null;
        try {
            Configuration configuration = ms.getConfiguration();
            Connection connection = transaction.getConnection();
            StatementHandler handler = configuration.newStatementHandler(ms, parameter);
            stmt = prepareStatement(handler, connection);
            return handler.update(stmt);
        } catch (Exception e) {
            log.error("Update failed for statement: {}", ms.getId(), e);
            throw new SQLException("Update failed for statement: " + ms.getId(), e);
        } finally {
            closeStatement(stmt);
        }
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, CacheKey cacheKey) throws SQLException {
        return query(ms, parameter);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter) throws SQLException {
        // 创建缓存键
        CacheKey key = createCacheKey(ms, parameter);

        // 先查一级缓存
        @SuppressWarnings("unchecked")
        List<E> cachedResult = (List<E>) localCache.getObject(key.toString());
        if (cachedResult != null) {
            log.debug("Local cache hit for statement: {}", ms.getId());
            return cachedResult;
        }

        // 缓存未命中，查询数据库
        log.debug("Local cache miss for statement: {}", ms.getId());
        List<E> result = doQuery(ms, parameter);

        // 存入一级缓存
        localCache.putObject(key.toString(), result);

        return result;
    }

    protected <E> List<E> doQuery(MappedStatement ms, Object parameter) throws SQLException {
        Statement stmt = null;
        try {
            Configuration configuration = ms.getConfiguration();
            Connection connection = transaction.getConnection();
            StatementHandler handler = configuration.newStatementHandler(ms, parameter);
            stmt = prepareStatement(handler, connection);
            return handler.query(stmt);
        } catch (Exception e) {
            log.error("Query failed for statement: {}", ms.getId(), e);
            throw new SQLException("Query failed for statement: " + ms.getId(), e);
        } finally {
            closeStatement(stmt);
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

    /**
     * 安全关闭 Statement
     * 避免关闭异常覆盖原始异常
     */
    private void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.warn("Failed to close statement", e);
            }
        }
    }

    /**
     * 创建缓存键
     */
    private CacheKey createCacheKey(MappedStatement ms, Object parameter) {
        CacheKey cacheKey = new CacheKey();
        cacheKey.update(ms.getId());
        cacheKey.update(parameter);
        return cacheKey;
    }

    /**
     * 清空一级缓存
     */
    private void clearLocalCache() {
        localCache.clear();
    }

    @Override
    public void commit() {
        // 提交时清空一级缓存
        clearLocalCache();
        try {
            transaction.commit();
        } catch (SQLException e) {
            log.error("Transaction commit failed", e);
            throw new RuntimeException("Transaction commit failed", e);
        }
    }

    @Override
    public void rollback() {
        // 回滚时清空一级缓存
        clearLocalCache();
        try {
            transaction.rollback();
        } catch (SQLException e) {
            log.error("Transaction rollback failed", e);
            throw new RuntimeException("Transaction rollback failed", e);
        }
    }

    @Override
    public void close() {
        // 关闭时清空一级缓存
        clearLocalCache();
        try {
            transaction.close();
        } catch (SQLException e) {
            log.error("Transaction close failed", e);
            throw new RuntimeException("Transaction close failed", e);
        }
    }
}
