package cn.abelib.minebatis.cache;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author abel.huang
 * @date 2026/4/11
 * 缓存单元测试
 */
public class CacheTest {

    private PerpetualCache perpetualCache;
    private LruCache lruCache;

    @Before
    public void setUp() {
        perpetualCache = new PerpetualCache("test-cache");
        lruCache = new LruCache(perpetualCache);
        lruCache.setSize(3);
    }

    // ===== PerpetualCache 测试 =====

    @Test
    public void testPerpetualCachePutAndGet() {
        perpetualCache.putObject("key1", "value1");

        assertEquals("value1", perpetualCache.getObject("key1"));
        assertEquals(1, perpetualCache.getSize());
    }

    @Test
    public void testPerpetualCacheRemove() {
        perpetualCache.putObject("key1", "value1");
        perpetualCache.putObject("key2", "value2");

        Object removed = perpetualCache.removeObject("key1");

        assertEquals("value1", removed);
        assertNull(perpetualCache.getObject("key1"));
        assertEquals(1, perpetualCache.getSize());
    }

    @Test
    public void testPerpetualCacheClear() {
        perpetualCache.putObject("key1", "value1");
        perpetualCache.putObject("key2", "value2");

        perpetualCache.clear();

        assertNull(perpetualCache.getObject("key1"));
        assertNull(perpetualCache.getObject("key2"));
        assertEquals(0, perpetualCache.getSize());
    }

    @Test
    public void testPerpetualCacheGetId() {
        assertEquals("test-cache", perpetualCache.getId());
    }

    @Test
    public void testPerpetualCacheOverwrite() {
        perpetualCache.putObject("key1", "value1");
        perpetualCache.putObject("key1", "value2");

        assertEquals("value2", perpetualCache.getObject("key1"));
        assertEquals(1, perpetualCache.getSize());
    }

    @Test
    public void testPerpetualCacheNullValue() {
        perpetualCache.putObject("key1", null);

        assertNull(perpetualCache.getObject("key1"));
        // 注意：null 值也会被缓存
        assertEquals(1, perpetualCache.getSize());
    }

    // ===== LruCache 测试 =====

    @Test
    public void testLruCacheEviction() {
        lruCache.putObject("key1", "value1");
        lruCache.putObject("key2", "value2");
        lruCache.putObject("key3", "value3");

        assertEquals(3, lruCache.getSize());

        // 添加第4个元素，应该淘汰最久未使用的 key1
        lruCache.putObject("key4", "value4");

        assertNull(lruCache.getObject("key1"));
        assertEquals("value2", lruCache.getObject("key2"));
        assertEquals("value3", lruCache.getObject("key3"));
        assertEquals("value4", lruCache.getObject("key4"));
    }

    @Test
    public void testLruCacheAccessOrder() {
        lruCache.putObject("key1", "value1");
        lruCache.putObject("key2", "value2");
        lruCache.putObject("key3", "value3");

        // 访问 key1，使其成为最近使用
        lruCache.getObject("key1");

        // 添加新元素，应该淘汰 key2
        lruCache.putObject("key4", "value4");

        assertEquals("value1", lruCache.getObject("key1"));
        assertNull(lruCache.getObject("key2"));
        assertEquals("value3", lruCache.getObject("key3"));
        assertEquals("value4", lruCache.getObject("key4"));
    }

    @Test
    public void testLruCacheClear() {
        lruCache.putObject("key1", "value1");
        lruCache.putObject("key2", "value2");

        lruCache.clear();

        assertNull(lruCache.getObject("key1"));
        assertNull(lruCache.getObject("key2"));
        assertEquals(0, lruCache.getSize());
    }

    @Test
    public void testLruCacheRemove() {
        lruCache.putObject("key1", "value1");
        lruCache.putObject("key2", "value2");

        Object removed = lruCache.removeObject("key1");

        assertEquals("value1", removed);
        assertNull(lruCache.getObject("key1"));
        assertEquals(1, lruCache.getSize());
    }

    @Test
    public void testLruCacheGetSize() {
        assertEquals(0, lruCache.getSize());

        lruCache.putObject("key1", "value1");
        assertEquals(1, lruCache.getSize());

        lruCache.putObject("key2", "value2");
        assertEquals(2, lruCache.getSize());
    }

    // ===== CacheKey 测试 =====

    @Test
    public void testCacheKeyEquality() {
        CacheKey key1 = new CacheKey();
        key1.update("SELECT * FROM test");
        key1.update(1L);

        CacheKey key2 = new CacheKey();
        key2.update("SELECT * FROM test");
        key2.update(1L);

        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    public void testCacheKeyInequality() {
        CacheKey key1 = new CacheKey();
        key1.update("SELECT * FROM test");
        key1.update(1L);

        CacheKey key2 = new CacheKey();
        key2.update("SELECT * FROM test");
        key2.update(2L);

        assertNotEquals(key1, key2);
    }

    @Test
    public void testCacheKeyWithDifferentOrder() {
        CacheKey key1 = new CacheKey();
        key1.update("a");
        key1.update("b");

        CacheKey key2 = new CacheKey();
        key2.update("b");
        key2.update("a");

        // 不同顺序的 CacheKey 不相等
        assertNotEquals(key1, key2);
    }

    @Test
    public void testCacheKeyNullValue() {
        CacheKey key1 = new CacheKey();
        key1.update(null);

        CacheKey key2 = new CacheKey();
        key2.update(null);

        assertEquals(key1, key2);
    }

    @Test
    public void testCacheKeyClone() throws CloneNotSupportedException {
        CacheKey key1 = new CacheKey();
        key1.update("test");

        CacheKey key2 = key1.clone();

        assertEquals(key1, key2);

        // 修改克隆后的 key 不影响原 key
        key2.update("new");
        assertNotEquals(key1, key2);
    }
}
