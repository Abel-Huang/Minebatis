package cn.abelib.minebatis.executor;

import cn.abelib.minebatis.cache.CacheKey;
import cn.abelib.minebatis.mapping.MappedStatement;

import java.sql.SQLException;
import java.util.List;

/**
 * @author abel.huang
 * @date 2020/8/7 20:28
 *  todo 支持事务, 缓存
 */
public interface Executor {
    int update(MappedStatement ms, Object parameter) throws SQLException;

    <E> List<E> query(MappedStatement ms, Object parameter, CacheKey cacheKey) throws SQLException;

    <E> List<E> query(MappedStatement ms, Object parameter) throws SQLException;
}
