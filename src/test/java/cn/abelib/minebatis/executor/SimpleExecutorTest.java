package cn.abelib.minebatis.executor;

import cn.abelib.minebatis.cache.CacheKey;
import cn.abelib.minebatis.mapping.MappedStatement;
import cn.abelib.minebatis.session.Configuration;
import cn.abelib.minebatis.transaction.Transaction;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author abel.huang
 * @date 2026/4/11
 * 一级缓存测试
 */
public class SimpleExecutorTest {

    private Configuration configuration;
    private Transaction transaction;
    private SimpleExecutor executor;
    private SimpleExecutor spyExecutor;

    @Before
    public void setUp() throws SQLException {
        configuration = new Configuration();
        transaction = mock(Transaction.class);
        executor = new SimpleExecutor(transaction);
        spyExecutor = spy(executor);
    }

    @Test
    public void testLocalCacheHit() throws Exception {
        MappedStatement ms = createSelectStatement();

        // 模拟 doQuery 返回结果
        List<Object> mockResult = new ArrayList<>();
        mockResult.add(new Object());
        doReturn(mockResult).when(spyExecutor).doQuery(eq(ms), any());

        // 第一次查询
        List<Object> result1 = spyExecutor.query(ms, 1L);

        // 第二次相同查询应该命中缓存
        List<Object> result2 = spyExecutor.query(ms, 1L);

        // 验证结果相同（同一个引用）
        assertSame(result1, result2);

        // 验证 doQuery 只被调用一次（第二次命中缓存）
        verify(spyExecutor, times(1)).doQuery(eq(ms), eq(1L));
    }

    @Test
    public void testLocalCacheMissWithDifferentParameter() throws Exception {
        MappedStatement ms = createSelectStatement();

        List<Object> mockResult = new ArrayList<>();
        doReturn(mockResult).when(spyExecutor).doQuery(eq(ms), any());

        // 不同参数的查询，不应该命中缓存
        spyExecutor.query(ms, 1L);
        spyExecutor.query(ms, 2L);

        // 验证 doQuery 被调用两次
        verify(spyExecutor, times(2)).doQuery(eq(ms), any());
    }

    @Test
    public void testLocalCacheClearOnUpdate() throws Exception {
        MappedStatement selectMs = createSelectStatement();
        MappedStatement updateMs = createUpdateStatement();

        List<Object> mockResult = new ArrayList<>();
        doReturn(mockResult).when(spyExecutor).doQuery(eq(selectMs), any());
        doReturn(1).when(spyExecutor).doUpdate(eq(updateMs), any());

        // 第一次查询
        spyExecutor.query(selectMs, 1L);
        verify(spyExecutor, times(1)).doQuery(eq(selectMs), eq(1L));

        // 执行更新操作
        spyExecutor.update(updateMs, new Object());

        // 再次查询相同条件，应该重新查询（不命中缓存）
        spyExecutor.query(selectMs, 1L);

        // 验证 doQuery 被调用两次（更新清除了缓存）
        verify(spyExecutor, times(2)).doQuery(eq(selectMs), eq(1L));
    }

    @Test
    public void testLocalCacheClearOnCommit() throws Exception {
        MappedStatement ms = createSelectStatement();

        List<Object> mockResult = new ArrayList<>();
        doReturn(mockResult).when(spyExecutor).doQuery(eq(ms), any());

        // 第一次查询
        spyExecutor.query(ms, 1L);
        verify(spyExecutor, times(1)).doQuery(eq(ms), eq(1L));

        // 提交事务
        spyExecutor.commit();

        // 再次查询相同条件
        spyExecutor.query(ms, 1L);

        // 验证 doQuery 被调用两次
        verify(spyExecutor, times(2)).doQuery(eq(ms), eq(1L));
    }

    @Test
    public void testLocalCacheClearOnRollback() throws Exception {
        MappedStatement ms = createSelectStatement();

        List<Object> mockResult = new ArrayList<>();
        doReturn(mockResult).when(spyExecutor).doQuery(eq(ms), any());

        // 第一次查询
        spyExecutor.query(ms, 1L);
        verify(spyExecutor, times(1)).doQuery(eq(ms), eq(1L));

        // 回滚事务
        spyExecutor.rollback();

        // 再次查询相同条件
        spyExecutor.query(ms, 1L);

        // 验证 doQuery 被调用两次
        verify(spyExecutor, times(2)).doQuery(eq(ms), eq(1L));
    }

    @Test
    public void testLocalCacheClearOnClose() throws Exception {
        MappedStatement ms = createSelectStatement();

        List<Object> mockResult = new ArrayList<>();
        doReturn(mockResult).when(spyExecutor).doQuery(eq(ms), any());

        // 查询
        spyExecutor.query(ms, 1L);

        // 关闭执行器
        spyExecutor.close();

        // 验证 transaction 被关闭
        verify(transaction).close();
    }

    @Test
    public void testLocalCacheWithDifferentStatements() throws Exception {
        MappedStatement ms1 = createSelectStatement("test.selectById");
        MappedStatement ms2 = createSelectStatement("test.selectByName");

        List<Object> mockResult = new ArrayList<>();
        doReturn(mockResult).when(spyExecutor).doQuery(any(MappedStatement.class), any());

        // 不同 SQL 语句，不应该命中缓存
        spyExecutor.query(ms1, 1L);
        spyExecutor.query(ms2, "test");

        // 验证 doQuery 被调用两次
        verify(spyExecutor, times(2)).doQuery(any(MappedStatement.class), any());
    }

    @Test
    public void testCacheKeyEquality() {
        // 验证 CacheKey 的生成逻辑
        CacheKey key1 = new CacheKey();
        key1.update("test.selectById");
        key1.update(1L);

        CacheKey key2 = new CacheKey();
        key2.update("test.selectById");
        key2.update(1L);

        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    public void testCacheKeyInequality() {
        CacheKey key1 = new CacheKey();
        key1.update("test.selectById");
        key1.update(1L);

        CacheKey key2 = new CacheKey();
        key2.update("test.selectById");
        key2.update(2L);

        assertNotEquals(key1, key2);
    }

    // ===== 辅助方法 =====

    private MappedStatement createSelectStatement() {
        return createSelectStatement("test.selectById");
    }

    private MappedStatement createSelectStatement(String id) {
        MappedStatement ms = new MappedStatement.Builder(
                configuration,
                id,
                "SELECT",
                "SELECT * FROM test WHERE id = ?",
                null,
                "java.lang.Long",
                "java.lang.Object"
        ).build();
        configuration.addMappedStatement(ms);
        return ms;
    }

    private MappedStatement createUpdateStatement() {
        MappedStatement ms = new MappedStatement.Builder(
                configuration,
                "test.update",
                "UPDATE",
                "UPDATE test SET name = ? WHERE id = ?",
                null,
                "java.lang.Object",
                null
        ).build();
        configuration.addMappedStatement(ms);
        return ms;
    }
}
