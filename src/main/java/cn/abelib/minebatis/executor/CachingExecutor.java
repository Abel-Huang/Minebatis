package cn.abelib.minebatis.executor;

import cn.abelib.minebatis.cache.Cache;
import cn.abelib.minebatis.cache.CacheKey;
import cn.abelib.minebatis.mapping.MappedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

/**
 * @author abel.huang
 * @date 2020/8/17 12:34
 * 二级缓存执行器（装饰器模式）
 *
 * 二级缓存事务逻辑：
 * 1. 查询结果先存入临时缓存（localOutputHolder）
 * 2. commit 时才将临时缓存写入二级缓存
 * 3. rollback 时清空临时缓存
 * 4. 这样可以防止未提交事务污染二级缓存
 */
public class CachingExecutor implements Executor {

    private static final Logger log = LoggerFactory.getLogger(CachingExecutor.class);

    private final Executor delegate;

    /**
     * 临时缓存：存储当前事务期间的查询结果
     * key: 缓存对象, value: 待提交的缓存条目
     */
    private final Map<Cache, Map<String, Object>> localOutputHolder = new HashMap<>();

    /**
     * 待清空的缓存集合（commit时执行）
     */
    private final Set<Cache> cachesToFlushOnCommit = new HashSet<>();

    public CachingExecutor(Executor executor) {
        this.delegate = executor;
    }

    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        // 标记需要在 commit 时清空该缓存
        markCacheForFlush(ms);
        return delegate.update(ms, parameter);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, CacheKey cacheKey) throws SQLException {
        return query(ms, parameter);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter) throws SQLException {
        Cache cache = ms.getCache();
        if (cache == null) {
            return delegate.query(ms, parameter);
        }

        CacheKey key = createCacheKey(ms, parameter);
        String keyStr = key.toString();

        // 1. 先查二级缓存
        @SuppressWarnings("unchecked")
        List<E> cachedResult = (List<E>) cache.getObject(keyStr);
        if (cachedResult != null) {
            log.debug("Second level cache hit for statement: {}", ms.getId());
            return cachedResult;
        }

        // 2. 查临时缓存（当前事务未提交的数据）
        Map<String, Object> localCache = localOutputHolder.get(cache);
        if (localCache != null) {
            @SuppressWarnings("unchecked")
            List<E> localResult = (List<E>) localCache.get(keyStr);
            if (localResult != null) {
                log.debug("Local output cache hit for statement: {}", ms.getId());
                return localResult;
            }
        }

        // 3. 查询数据库
        log.debug("Cache miss for statement: {}, querying database", ms.getId());
        List<E> result = delegate.query(ms, parameter);

        // 4. 存入临时缓存（不直接写入二级缓存，等 commit 时写入）
        if (localCache == null) {
            localCache = new HashMap<>();
            localOutputHolder.put(cache, localCache);
        }
        localCache.put(keyStr, result);

        return result;
    }

    @Override
    public void commit() {
        // 1. 先执行 delegate 的 commit（清空一级缓存、提交事务）
        delegate.commit();

        // 2. 清空需要清空的缓存
        for (Cache cache : cachesToFlushOnCommit) {
            cache.clear();
            log.debug("Flushed cache on commit: {}", cache.getId());
        }
        cachesToFlushOnCommit.clear();

        // 3. 将临时缓存写入二级缓存
        for (Map.Entry<Cache, Map<String, Object>> entry : localOutputHolder.entrySet()) {
            Cache cache = entry.getKey();
            Map<String, Object> localCache = entry.getValue();
            for (Map.Entry<String, Object> cacheEntry : localCache.entrySet()) {
                cache.putObject(cacheEntry.getKey(), cacheEntry.getValue());
            }
            log.debug("Committed {} entries to cache: {}", localCache.size(), cache.getId());
        }
        localOutputHolder.clear();
    }

    @Override
    public void rollback() {
        // 1. 先执行 delegate 的 rollback
        delegate.rollback();

        // 2. 清空待清空的缓存集合
        cachesToFlushOnCommit.clear();

        // 3. 清空临时缓存（不写入二级缓存）
        localOutputHolder.clear();
        log.debug("Rollback: cleared local output cache");
    }

    @Override
    public void close() {
        // 关闭时清空临时缓存
        localOutputHolder.clear();
        cachesToFlushOnCommit.clear();
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
     * 标记缓存需要在 commit 时清空
     */
    private void markCacheForFlush(MappedStatement ms) {
        Cache cache = ms.getCache();
        if (cache != null) {
            cachesToFlushOnCommit.add(cache);
            // 同时清空该缓存的临时数据
            localOutputHolder.remove(cache);
        }
    }
}
