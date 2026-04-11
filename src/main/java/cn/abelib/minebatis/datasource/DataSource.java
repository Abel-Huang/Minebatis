package cn.abelib.minebatis.datasource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author abel.huang
 * @date 2026/4/11
 * 数据源接口
 * 支持注入第三方连接池（HikariCP、Druid等）
 */
public interface DataSource {

    /**
     * 获取数据库连接
     */
    Connection getConnection() throws SQLException;

    /**
     * 获取驱动类名
     */
    String getDriver();

    /**
     * 获取连接URL
     */
    String getUrl();

    /**
     * 获取用户名
     */
    String getUsername();

    /**
     * 获取密码
     */
    String getPassword();
}
