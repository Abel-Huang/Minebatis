package cn.abelib.minebatis.datasource;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author abel.huang
 * @date 2026/4/11
 * 数据源测试
 */
public class UnpooledDataSourceTest {

    @Test
    public void testConstructorWithParameters() {
        UnpooledDataSource dataSource = new UnpooledDataSource(
                "com.mysql.jdbc.Driver",
                "jdbc:mysql://localhost:3306/test",
                "root",
                "password"
        );

        assertEquals("com.mysql.jdbc.Driver", dataSource.getDriver());
        assertEquals("jdbc:mysql://localhost:3306/test", dataSource.getUrl());
        assertEquals("root", dataSource.getUsername());
        assertEquals("password", dataSource.getPassword());
    }

    @Test
    public void testConstructorWithProperties() {
        Properties props = new Properties();
        props.setProperty("driver", "com.mysql.jdbc.Driver");
        props.setProperty("url", "jdbc:mysql://localhost:3306/test");
        props.setProperty("username", "root");
        props.setProperty("password", "password");

        UnpooledDataSource dataSource = new UnpooledDataSource(props);

        assertEquals("com.mysql.jdbc.Driver", dataSource.getDriver());
        assertEquals("jdbc:mysql://localhost:3306/test", dataSource.getUrl());
        assertEquals("root", dataSource.getUsername());
        assertEquals("password", dataSource.getPassword());
    }

    @Test
    public void testSetters() {
        UnpooledDataSource dataSource = new UnpooledDataSource();
        dataSource.setDriver("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/test");
        dataSource.setUsername("root");
        dataSource.setPassword("password");

        assertEquals("com.mysql.jdbc.Driver", dataSource.getDriver());
        assertEquals("jdbc:mysql://localhost:3306/test", dataSource.getUrl());
        assertEquals("root", dataSource.getUsername());
        assertEquals("password", dataSource.getPassword());
    }

    @Test
    public void testDataSourceInterface() {
        DataSource dataSource = new UnpooledDataSource(
                "com.mysql.jdbc.Driver",
                "jdbc:mysql://localhost:3306/test",
                "root",
                "password"
        );

        assertTrue(dataSource instanceof DataSource);
        assertNotNull(dataSource.getDriver());
        assertNotNull(dataSource.getUrl());
    }
}
