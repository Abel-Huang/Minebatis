package cn.abelib.minebatis.executor;

import cn.abelib.minebatis.cache.Cache;
import cn.abelib.minebatis.cache.CacheKey;
import cn.abelib.minebatis.mapping.MappedStatement;

import java.sql.SQLException;
import java.util.List;

/**
 * @author abel.huang
 * @date 2020/8/17 12:34
 * 二级缓存执行器（装饰器模式）
 */
public class CachingExecutor implements Executor {

    private final Executor delegate;

    public CachingExecutor(Executor executor) {
        this.delegate = executor;
    }

    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        // 执行更新操作时，清空该 namespace 的二级缓存
        flushCacheIfRequired(ms);
        return delegate.update(ms, parameter);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, CacheKey cacheKey) throws SQLException {
        return query(ms, parameter);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter) throws SQLException {
        // 检查是否启用了二级缓存
        Cache cache = ms.getCache();
        if (cache != null) {
            CacheKey key = createCacheKey(ms, parameter);
            @SuppressWarnings("unchecked")
            List<E> cachedResult = (List<E>) cache.getObject(key.toString());
            if (cachedResult != null) {
                return cachedResult;
            }
            // 查询数据库
            List<E> result = delegate.query(ms, parameter);
            // 存入缓存
            cache.putObject(key.toString(), result);
            return result;
        }
        return delegate.query(ms, parameter);
    }

    @Override
    public void commit() {
        delegate.commit();
    }

    @Override
    public void rollback() {
        delegate.rollback();
    }

    @Override
    public void close() {
        delegate.close();
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
     * 根据需要清空缓存
     */
    private void flushCacheIfRequired(MappedStatement ms) {
        Cache cache = ms.getCache();
        if (cache != null) {
            cache.clear();
        }
    }
}
