package cn.abelib.minebatis.executor;

import cn.abelib.minebatis.cache.CacheKey;
import cn.abelib.minebatis.cache.PerpetualCache;
import cn.abelib.minebatis.mapping.MappedStatement;
import cn.abelib.minebatis.session.Configuration;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author abel.huang
 * @date 2026/4/11
 * 二级缓存事务测试
 */
public class CachingExecutorTest {

    private Configuration configuration;
    private Executor delegate;
    private CachingExecutor cachingExecutor;
    private PerpetualCache cache;

    @Before
    public void setUp() {
        configuration = new Configuration();
        delegate = mock(Executor.class);
        cachingExecutor = new CachingExecutor(delegate);
        cache = new PerpetualCache("test-cache");
    }

    @Test
    public void testQueryWithoutCache() throws Exception {
        MappedStatement ms = createMappedStatementWithoutCache();

        List<Object> mockResult = new ArrayList<>();
        mockResult.add(new Object());
        doReturn(mockResult).when(delegate).query(eq(ms), any());

        List<Object> result = cachingExecutor.query(ms, 1L);

        assertEquals(mockResult, result);
        verify(delegate).query(ms, 1L);
    }

    @Test
    public void testQueryStoredInLocalCacheBeforeCommit() throws Exception {
        MappedStatement ms = createMappedStatementWithCache();

        List<Object> mockResult = new ArrayList<>();
        mockResult.add("test-data");
        doReturn(mockResult).when(delegate).query(eq(ms), any());

        // 第一次查询
        List<Object> result1 = cachingExecutor.query(ms, 1L);
        assertEquals(mockResult, result1);

        // 第二次查询应该命中临时缓存
        List<Object> result2 = cachingExecutor.query(ms, 1L);
        assertSame(result1, result2);

        // 二级缓存应该还没有数据（因为还没 commit）
        String keyStr = createCacheKey(ms, 1L);
        assertNull(cache.getObject(keyStr));

        // delegate 只被调用一次
        verify(delegate, times(1)).query(ms, 1L);
    }

    @Test
    public void testCommitWritesToSecondLevelCache() throws Exception {
        MappedStatement ms = createMappedStatementWithCache();

        List<Object> mockResult = new ArrayList<>();
        mockResult.add("test-data");
        doReturn(mockResult).when(delegate).query(eq(ms), any());

        // 查询
        cachingExecutor.query(ms, 1L);

        // commit 前，二级缓存为空
        String keyStr = createCacheKey(ms, 1L);
        assertNull(cache.getObject(keyStr));

        // commit
        cachingExecutor.commit();

        // commit 后，二级缓存有数据
        List<?> cachedResult = (List<?>) cache.getObject(keyStr);
        assertNotNull(cachedResult);
        assertEquals(mockResult, cachedResult);
    }

    @Test
    public void testRollbackDiscardsLocalCache() throws Exception {
        MappedStatement ms = createMappedStatementWithCache();

        List<Object> mockResult = new ArrayList<>();
        mockResult.add("test-data");
        doReturn(mockResult).when(delegate).query(eq(ms), any());

        // 查询
        cachingExecutor.query(ms, 1L);

        // rollback
        cachingExecutor.rollback();

        // rollback 后，二级缓存应该仍然为空
        String keyStr = createCacheKey(ms, 1L);
        assertNull(cache.getObject(keyStr));
    }

    @Test
    public void testUpdateMarksCacheForFlush() throws Exception {
        MappedStatement ms = createMappedStatementWithCache();

        // 先查询并 commit，使二级缓存有数据
        List<Object> mockResult = new ArrayList<>();
        mockResult.add("test-data");
        doReturn(mockResult).when(delegate).query(eq(ms), any());
        cachingExecutor.query(ms, 1L);
        cachingExecutor.commit();

        String keyStr = createCacheKey(ms, 1L);
        assertNotNull(cache.getObject(keyStr));

        // 执行更新
        doReturn(1).when(delegate).update(eq(ms), any());
        cachingExecutor.update(ms, new Object());

        // commit 后，缓存应该被清空
        cachingExecutor.commit();
        assertNull(cache.getObject(keyStr));
    }

    @Test
    public void testCloseClearsLocalCache() throws Exception {
        MappedStatement ms = createMappedStatementWithCache();

        List<Object> mockResult = new ArrayList<>();
        mockResult.add("test-data");
        doReturn(mockResult).when(delegate).query(eq(ms), any());

        // 查询
        cachingExecutor.query(ms, 1L);

        // close
        cachingExecutor.close();

        // 二级缓存应该为空
        String keyStr = createCacheKey(ms, 1L);
        assertNull(cache.getObject(keyStr));
        verify(delegate).close();
    }

    // ===== 辅助方法 =====

    private MappedStatement createMappedStatementWithoutCache() {
        return new MappedStatement.Builder(
                configuration,
                "test.selectById",
                "SELECT",
                "SELECT * FROM test WHERE id = ?",
                null,
                "java.lang.Long",
                "java.lang.Object"
        ).build();
    }

    private MappedStatement createMappedStatementWithCache() {
        MappedStatement ms = new MappedStatement.Builder(
                configuration,
                "test.selectById",
                "SELECT",
                "SELECT * FROM test WHERE id = ?",
                null,
                "java.lang.Long",
                "java.lang.Object"
        ).cache(cache).build();
        return ms;
    }

    private String createCacheKey(MappedStatement ms, Object parameter) {
        CacheKey cacheKey = new CacheKey();
        cacheKey.update(ms.getId());
        cacheKey.update(parameter);
        return cacheKey.toString();
    }
}
