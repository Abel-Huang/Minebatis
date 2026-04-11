package cn.abelib.minebatis.executor.parameter;

import cn.abelib.minebatis.mapping.MappedStatement;
import cn.abelib.minebatis.session.Configuration;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * @author abel.huang
 * @date 2026/4/11
 * 参数处理器单元测试
 */
public class DefaultParameterHandlerTest {

    private Configuration configuration;
    private MappedStatement mappedStatement;
    private PreparedStatement preparedStatement;

    @Before
    public void setUp() {
        configuration = new Configuration();
        preparedStatement = mock(PreparedStatement.class);

        // 创建测试用的 MappedStatement
        mappedStatement = new MappedStatement.Builder(
                configuration,
                "test.query",
                "SELECT",
                "SELECT * FROM test WHERE id = ? AND name = ?",
                Map.of(1, "id", 2, "name"),
                "java.lang.Long",
                "java.lang.Object"
        ).build();
    }

    @Test
    public void testNullParameter() throws SQLException, IllegalAccessException {
        DefaultParameterHandler handler = new DefaultParameterHandler(mappedStatement, null);
        handler.setParameters(preparedStatement);

        // 空参数应该直接返回，不设置任何值
        verify(preparedStatement, never()).setObject(anyInt(), any());
    }

    @Test
    public void testSingleLongParameter() throws SQLException, IllegalAccessException {
        // 创建单个参数的 MappedStatement
        MappedStatement ms = new MappedStatement.Builder(
                configuration,
                "test.queryById",
                "SELECT",
                "SELECT * FROM test WHERE id = ?",
                Map.of(1, "id"),
                "java.lang.Long",
                "java.lang.Object"
        ).build();

        DefaultParameterHandler handler = new DefaultParameterHandler(ms, 100L);
        handler.setParameters(preparedStatement);

        verify(preparedStatement).setLong(1, 100L);
    }

    @Test
    public void testSingleIntegerParameter() throws SQLException, IllegalAccessException {
        MappedStatement ms = new MappedStatement.Builder(
                configuration,
                "test.queryById",
                "SELECT",
                "SELECT * FROM test WHERE id = ?",
                Map.of(1, "id"),
                "java.lang.Integer",
                "java.lang.Object"
        ).build();

        DefaultParameterHandler handler = new DefaultParameterHandler(ms, 42);
        handler.setParameters(preparedStatement);

        verify(preparedStatement).setInt(1, 42);
    }

    @Test
    public void testSingleStringParameter() throws SQLException, IllegalAccessException {
        MappedStatement ms = new MappedStatement.Builder(
                configuration,
                "test.queryByName",
                "SELECT",
                "SELECT * FROM test WHERE name = ?",
                Map.of(1, "name"),
                "java.lang.String",
                "java.lang.Object"
        ).build();

        DefaultParameterHandler handler = new DefaultParameterHandler(ms, "test");
        handler.setParameters(preparedStatement);

        verify(preparedStatement).setString(1, "test");
    }

    @Test
    public void testMapParameter() throws SQLException, IllegalAccessException {
        MappedStatement ms = new MappedStatement.Builder(
                configuration,
                "test.queryByConditions",
                "SELECT",
                "SELECT * FROM test WHERE id = ? AND name = ?",
                Map.of(1, "id", 2, "name"),
                "java.util.Map",
                "java.lang.Object"
        ).build();

        Map<String, Object> params = new HashMap<>();
        params.put("id", 1L);
        params.put("name", "test");

        DefaultParameterHandler handler = new DefaultParameterHandler(ms, params);
        handler.setParameters(preparedStatement);

        verify(preparedStatement).setLong(1, 1L);
        verify(preparedStatement).setString(2, "test");
    }

    @Test
    public void testDateParameter() throws SQLException, IllegalAccessException {
        MappedStatement ms = new MappedStatement.Builder(
                configuration,
                "test.queryByDate",
                "SELECT",
                "SELECT * FROM test WHERE create_time = ?",
                Map.of(1, "createTime"),
                "java.util.Map",
                "java.lang.Object"
        ).build();

        Map<String, Object> params = new HashMap<>();
        Date now = new Date();
        params.put("createTime", now);

        DefaultParameterHandler handler = new DefaultParameterHandler(ms, params);
        handler.setParameters(preparedStatement);

        verify(preparedStatement).setTimestamp(eq(1), any());
    }

    @Test
    public void testBigDecimalParameter() throws SQLException, IllegalAccessException {
        MappedStatement ms = new MappedStatement.Builder(
                configuration,
                "test.queryByAmount",
                "SELECT",
                "SELECT * FROM test WHERE amount = ?",
                Map.of(1, "amount"),
                "java.math.BigDecimal",
                "java.lang.Object"
        ).build();

        BigDecimal amount = new BigDecimal("123.45");
        DefaultParameterHandler handler = new DefaultParameterHandler(ms, amount);
        handler.setParameters(preparedStatement);

        verify(preparedStatement).setBigDecimal(1, amount);
    }

    @Test
    public void testObjectParameter() throws SQLException, IllegalAccessException {
        MappedStatement ms = new MappedStatement.Builder(
                configuration,
                "test.insert",
                "INSERT",
                "INSERT INTO test(id, name, age) VALUES(?, ?, ?)",
                Map.of(1, "id", 2, "name", 3, "age"),
                "cn.abelib.minebatis.executor.parameter.DefaultParameterHandlerTest$TestUser",
                "java.lang.Object"
        ).build();

        TestUser user = new TestUser();
        user.id = 1L;
        user.name = "test";
        user.age = 25;

        DefaultParameterHandler handler = new DefaultParameterHandler(ms, user);
        handler.setParameters(preparedStatement);

        verify(preparedStatement).setLong(1, 1L);
        verify(preparedStatement).setString(2, "test");
        verify(preparedStatement).setInt(3, 25);
    }

    /**
     * 测试用的实体类
     */
    public static class TestUser {
        public Long id;
        public String name;
        public Integer age;
    }
}
