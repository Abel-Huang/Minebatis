package cn.abelib.minebatis.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author abel.huang
 * @date 2026/4/11
 * 非池化数据源
 * 每次获取连接都创建新连接，适用于测试或简单场景
 * 生产环境建议使用第三方连接池（HikariCP、Druid）
 */
public class UnpooledDataSource implements DataSource {

    private String driver;
    private String url;
    private String username;
    private String password;

    private boolean driverLoaded;

    public UnpooledDataSource() {
    }

    public UnpooledDataSource(String driver, String url, String username, String password) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public UnpooledDataSource(Properties properties) {
        this.driver = properties.getProperty("driver");
        this.url = properties.getProperty("url");
        this.username = properties.getProperty("username");
        this.password = properties.getProperty("password");
    }

    @Override
    public Connection getConnection() throws SQLException {
        loadDriver();
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public String getDriver() {
        return driver;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setDriver(String driver) {
        this.driver = driver;
        this.driverLoaded = false;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private void loadDriver() {
        if (!driverLoaded) {
            try {
                Class.forName(driver);
                driverLoaded = true;
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Failed to load driver: " + driver, e);
            }
        }
    }
}
