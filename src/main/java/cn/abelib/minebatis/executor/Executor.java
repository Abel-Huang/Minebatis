package cn.abelib.minebatis.executor;

import cn.abelib.minebatis.cache.CacheKey;
import cn.abelib.minebatis.mapping.MappedStatement;

import java.sql.SQLException;
import java.util.List;

/**
 * @author abel.huang
 * @date 2020/8/7 20:28
 */
public interface Executor {
    int update(MappedStatement ms, Object parameter) throws SQLException;

    <E> List<E> query(MappedStatement ms, Object parameter, CacheKey cacheKey, String boundSql) throws SQLException;

    <E> List<E> query(MappedStatement ms, Object parameter, String boundSql) throws SQLException;
}
