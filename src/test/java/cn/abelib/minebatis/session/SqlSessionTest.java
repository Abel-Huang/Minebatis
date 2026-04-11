package cn.abelib.minebatis.session;

import cn.abelib.minebatis.executor.Executor;
import cn.abelib.minebatis.mapping.MappedStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author abel.huang
 * @date 2026/4/11
 * SqlSession 集成测试
 */
public class SqlSessionTest {

    private Configuration configuration;
    private Executor executor;
    private DefaultSqlSession sqlSession;

    @Before
    public void setUp() {
        configuration = new Configuration();
        executor = mock(Executor.class);

        // 注册测试用的 MappedStatement
        MappedStatement selectMs = new MappedStatement.Builder(
                configuration,
                "cn.abelib.minebatis.session.SqlSessionTest$TestMapper.selectById",
                "SELECT",
                "SELECT * FROM test WHERE id = ?",
                null,
                "java.lang.Long",
                "java.lang.Object"
        ).build();

        MappedStatement selectListMs = new MappedStatement.Builder(
                configuration,
                "cn.abelib.minebatis.session.SqlSessionTest$TestMapper.selectAll",
                "SELECT",
                "SELECT * FROM test",
                null,
                null,
                "java.lang.Object"
        ).build();

        MappedStatement insertMs = new MappedStatement.Builder(
                configuration,
                "cn.abelib.minebatis.session.SqlSessionTest$TestMapper.insert",
                "INSERT",
                "INSERT INTO test(id, name) VALUES(?, ?)",
                null,
                "java.lang.Object",
                null
        ).build();

        MappedStatement updateMs = new MappedStatement.Builder(
                configuration,
                "cn.abelib.minebatis.session.SqlSessionTest$TestMapper.update",
                "UPDATE",
                "UPDATE test SET name = ? WHERE id = ?",
                null,
                "java.lang.Object",
                null
        ).build();

        MappedStatement deleteMs = new MappedStatement.Builder(
                configuration,
                "cn.abelib.minebatis.session.SqlSessionTest$TestMapper.deleteById",
                "DELETE",
                "DELETE FROM test WHERE id = ?",
                null,
                "java.lang.Long",
                null
        ).build();

        configuration.addMappedStatement(selectMs);
        configuration.addMappedStatement(selectListMs);
        configuration.addMappedStatement(insertMs);
        configuration.addMappedStatement(updateMs);
        configuration.addMappedStatement(deleteMs);

        sqlSession = new DefaultSqlSession(configuration, executor);
    }

    @Test
    public void testSelectOne() throws Exception {
        TestUser user = new TestUser(1L, "test");
        List<TestUser> resultList = new ArrayList<>();
        resultList.add(user);

        // 使用 doReturn 避免泛型类型问题
        doReturn(resultList).when(executor).query(any(MappedStatement.class), eq(1L));

        TestUser result = sqlSession.selectOne(
                "cn.abelib.minebatis.session.SqlSessionTest$TestMapper.selectById", 1L);

        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.id);
        assertEquals("test", result.name);
    }

    @Test
    public void testSelectOneWithNoResult() throws Exception {
        List<TestUser> emptyList = new ArrayList<>();
        doReturn(emptyList).when(executor).query(any(MappedStatement.class), any());

        TestUser result = sqlSession.selectOne(
                "cn.abelib.minebatis.session.SqlSessionTest$TestMapper.selectById", 999L);

        assertNull(result);
    }

    @Test(expected = RuntimeException.class)
    public void testSelectOneWithMultipleResults() throws Exception {
        List<TestUser> multipleResults = new ArrayList<>();
        multipleResults.add(new TestUser(1L, "test1"));
        multipleResults.add(new TestUser(2L, "test2"));

        doReturn(multipleResults).when(executor).query(any(MappedStatement.class), any());

        sqlSession.selectOne("cn.abelib.minebatis.session.SqlSessionTest$TestMapper.selectAll");
    }

    @Test
    public void testSelectList() throws Exception {
        List<TestUser> expectedList = new ArrayList<>();
        expectedList.add(new TestUser(1L, "test1"));
        expectedList.add(new TestUser(2L, "test2"));

        doReturn(expectedList).when(executor).query(any(MappedStatement.class), any());

        List<TestUser> result = sqlSession.selectList(
                "cn.abelib.minebatis.session.SqlSessionTest$TestMapper.selectAll");

        assertEquals(2, result.size());
        assertEquals("test1", result.get(0).name);
        assertEquals("test2", result.get(1).name);
    }

    @Test
    public void testInsert() throws Exception {
        when(executor.update(any(MappedStatement.class), any())).thenReturn(1);

        int result = sqlSession.insert(
                "cn.abelib.minebatis.session.SqlSessionTest$TestMapper.insert",
                new TestUser(1L, "test"));

        assertEquals(1, result);
        verify(executor).update(any(MappedStatement.class), any());
    }

    @Test
    public void testUpdate() throws Exception {
        when(executor.update(any(MappedStatement.class), any())).thenReturn(1);

        int result = sqlSession.update(
                "cn.abelib.minebatis.session.SqlSessionTest$TestMapper.update",
                new TestUser(1L, "newName"));

        assertEquals(1, result);
    }

    @Test
    public void testDelete() throws Exception {
        when(executor.update(any(MappedStatement.class), eq(1L))).thenReturn(1);

        int result = sqlSession.delete(
                "cn.abelib.minebatis.session.SqlSessionTest$TestMapper.deleteById", 1L);

        assertEquals(1, result);
        verify(executor).update(any(MappedStatement.class), eq(1L));
    }

    @Test
    public void testCommit() {
        sqlSession.commit();
        verify(executor).commit();
    }

    @Test
    public void testRollback() {
        sqlSession.rollback();
        verify(executor).rollback();
    }

    @Test
    public void testClose() {
        sqlSession.close();
        verify(executor).close();
    }

    @Test
    public void testGetConfiguration() {
        Configuration config = sqlSession.getConfiguration();
        assertNotNull(config);
        assertEquals(configuration, config);
    }

    @Test
    public void testGetMapper() {
        configuration.addMapper(TestMapper.class);

        TestMapper mapper = sqlSession.getMapper(TestMapper.class);

        assertNotNull(mapper);
        // 返回的是代理对象
        assertTrue(java.lang.reflect.Proxy.isProxyClass(mapper.getClass()));
    }

    // ===== 测试用类 =====

    public static class TestUser {
        private Long id;
        private String name;

        public TestUser() {}

        public TestUser(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public void setId(Long id) { this.id = id; }
        public void setName(String name) { this.name = name; }
    }

    public interface TestMapper {
        TestUser selectById(Long id);
        List<TestUser> selectAll();
        int insert(TestUser user);
        int update(TestUser user);
        int deleteById(Long id);
    }
}
