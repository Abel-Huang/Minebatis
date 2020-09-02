package cn.abelib.minebatis.executor;

import cn.abelib.minebatis.cache.CacheKey;
import cn.abelib.minebatis.mapping.MappedStatement;

import java.sql.SQLException;
import java.util.List;

/**
 * @author abel.huang
 * @date 2020/8/17 12:34
 */
public class CachingExecutor implements Executor {
    // todo 代理模式
    public CachingExecutor(Executor executor) {

    }

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
        return null;
    }
}
