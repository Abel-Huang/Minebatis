package cn.abelib.minebatis.executor.resultset;

import cn.abelib.minebatis.mapping.MappedStatement;
import cn.abelib.minebatis.session.Configuration;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author abel.huang
 * @date 2026/4/11
 * 结果集处理器单元测试
 */
public class DefaultResultSetHandlerTest {

    private Configuration configuration;

    @Before
    public void setUp() {
        configuration = new Configuration();
    }

    @Test
    public void testHandleResultSetWithBasicTypes() throws Exception {
        // 创建 MappedStatement
        MappedStatement ms = new MappedStatement.Builder(
                configuration,
                "test.select",
                "SELECT",
                "SELECT id, name, age FROM test",
                null,
                null,
                TestUser.class.getName()
        ).build();

        DefaultResultSetHandler handler = new DefaultResultSetHandler(ms);

        // 模拟 ResultSet
        ResultSet rs = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);

        when(rs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(3);
        when(metaData.getColumnName(1)).thenReturn("id");
        when(metaData.getColumnName(2)).thenReturn("name");
        when(metaData.getColumnName(3)).thenReturn("age");

        // 模拟第一行数据
        when(rs.next()).thenReturn(true, false);
        when(rs.getObject(1)).thenReturn(1L);
        when(rs.getObject(2)).thenReturn("test");
        when(rs.getObject(3)).thenReturn(25);

        Statement stmt = mock(Statement.class);
        when(stmt.getResultSet()).thenReturn(rs);

        List<TestUser> result = handler.handleResultSets(stmt);

        assertEquals(1, result.size());
        TestUser user = result.get(0);
        assertEquals(Long.valueOf(1L), user.id);
        assertEquals("test", user.name);
        assertEquals(Integer.valueOf(25), user.age);
    }

    @Test
    public void testHandleResultSetWithUnderscoreToCamelCase() throws Exception {
        MappedStatement ms = new MappedStatement.Builder(
                configuration,
                "test.select",
                "SELECT",
                "SELECT user_name, create_time FROM test",
                null,
                null,
                TestUserWithUnderscore.class.getName()
        ).build();

        DefaultResultSetHandler handler = new DefaultResultSetHandler(ms);

        ResultSet rs = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);

        when(rs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnName(1)).thenReturn("user_name");
        when(metaData.getColumnName(2)).thenReturn("create_time");

        when(rs.next()).thenReturn(true, false);
        when(rs.getObject(1)).thenReturn("john");
        when(rs.getObject(2)).thenReturn(new Timestamp(System.currentTimeMillis()));

        Statement stmt = mock(Statement.class);
        when(stmt.getResultSet()).thenReturn(rs);

        List<TestUserWithUnderscore> result = handler.handleResultSets(stmt);

        assertEquals(1, result.size());
        TestUserWithUnderscore user = result.get(0);
        assertEquals("john", user.userName);
        assertNotNull(user.createTime);
    }

    @Test
    public void testHandleResultSetWithTypeConversion() throws Exception {
        MappedStatement ms = new MappedStatement.Builder(
                configuration,
                "test.select",
                "SELECT",
                "SELECT id, score, amount FROM test",
                null,
                null,
                TestUserWithNumber.class.getName()
        ).build();

        DefaultResultSetHandler handler = new DefaultResultSetHandler(ms);

        ResultSet rs = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);

        when(rs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(3);
        when(metaData.getColumnName(1)).thenReturn("id");
        when(metaData.getColumnName(2)).thenReturn("score");
        when(metaData.getColumnName(3)).thenReturn("amount");

        when(rs.next()).thenReturn(true, false);
        // 数据库返回 Integer，实体字段是 Long
        when(rs.getObject(1)).thenReturn(1);
        // 数据库返回 BigDecimal，实体字段是 Double
        when(rs.getObject(2)).thenReturn(new BigDecimal("95.5"));
        // 数据库返回 Double，实体字段是 BigDecimal
        when(rs.getObject(3)).thenReturn(100.50);

        Statement stmt = mock(Statement.class);
        when(stmt.getResultSet()).thenReturn(rs);

        List<TestUserWithNumber> result = handler.handleResultSets(stmt);

        assertEquals(1, result.size());
        TestUserWithNumber user = result.get(0);
        assertEquals(Long.valueOf(1L), user.id);
        assertEquals(Double.valueOf(95.5), user.score);
        assertNotNull(user.amount);
    }

    @Test
    public void testHandleEmptyResultSet() throws Exception {
        MappedStatement ms = new MappedStatement.Builder(
                configuration,
                "test.select",
                "SELECT",
                "SELECT id FROM test",
                null,
                null,
                TestUser.class.getName()
        ).build();

        DefaultResultSetHandler handler = new DefaultResultSetHandler(ms);

        ResultSet rs = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);

        when(rs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(rs.next()).thenReturn(false);  // 空结果集

        Statement stmt = mock(Statement.class);
        when(stmt.getResultSet()).thenReturn(rs);

        List<TestUser> result = handler.handleResultSets(stmt);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testHandleMultipleRows() throws Exception {
        MappedStatement ms = new MappedStatement.Builder(
                configuration,
                "test.select",
                "SELECT",
                "SELECT id, name FROM test",
                null,
                null,
                TestUser.class.getName()
        ).build();

        DefaultResultSetHandler handler = new DefaultResultSetHandler(ms);

        ResultSet rs = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);

        when(rs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnName(1)).thenReturn("id");
        when(metaData.getColumnName(2)).thenReturn("name");

        // 模拟3行数据
        when(rs.next()).thenReturn(true, true, true, false);
        when(rs.getObject(1)).thenReturn(1L, 2L, 3L);
        when(rs.getObject(2)).thenReturn("user1", "user2", "user3");

        Statement stmt = mock(Statement.class);
        when(stmt.getResultSet()).thenReturn(rs);

        List<TestUser> result = handler.handleResultSets(stmt);

        assertEquals(3, result.size());
        assertEquals("user1", result.get(0).name);
        assertEquals("user2", result.get(1).name);
        assertEquals("user3", result.get(2).name);
    }

    @Test
    public void testHandleNullValues() throws Exception {
        MappedStatement ms = new MappedStatement.Builder(
                configuration,
                "test.select",
                "SELECT",
                "SELECT id, name FROM test",
                null,
                null,
                TestUser.class.getName()
        ).build();

        DefaultResultSetHandler handler = new DefaultResultSetHandler(ms);

        ResultSet rs = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);

        when(rs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnName(1)).thenReturn("id");
        when(metaData.getColumnName(2)).thenReturn("name");

        when(rs.next()).thenReturn(true, false);
        when(rs.getObject(1)).thenReturn(1L);
        when(rs.getObject(2)).thenReturn(null);  // null 值

        Statement stmt = mock(Statement.class);
        when(stmt.getResultSet()).thenReturn(rs);

        List<TestUser> result = handler.handleResultSets(stmt);

        assertEquals(1, result.size());
        assertEquals(Long.valueOf(1L), result.get(0).id);
        assertNull(result.get(0).name);
    }

    // ===== 测试用实体类 =====

    public static class TestUser {
        private Long id;
        private String name;
        private Integer age;

        public void setId(Long id) { this.id = id; }
        public void setName(String name) { this.name = name; }
        public void setAge(Integer age) { this.age = age; }
    }

    public static class TestUserWithUnderscore {
        private String userName;
        private Date createTime;

        public void setUserName(String userName) { this.userName = userName; }
        public void setCreateTime(Date createTime) { this.createTime = createTime; }
    }

    public static class TestUserWithNumber {
        private Long id;
        private Double score;
        private BigDecimal amount;

        public void setId(Long id) { this.id = id; }
        public void setScore(Double score) { this.score = score; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }
}
