package cn.abelib.minebatis;

import cn.abelib.minebatis.io.XNode;

import java.sql.Connection;
import java.util.Map;

/**
 * @author abel.huang
 * @date 2020/8/6 13:50
 */
public class Configuration {
    private Connection connection;
    private Map<String, String> dataSource;
    private Map<String, XNode> mapperElement;

    public void setDataSource(Map<String, String> dataSource) {
        this.dataSource = dataSource;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setMapperElement(Map<String, XNode> mapperElement) {
        this.mapperElement = mapperElement;
    }

    public Connection getConnection() {
        return connection;
    }

    public Map<String, String> getDataSource() {
        return dataSource;
    }

    public Map<String, XNode> getMapperElement() {
        return mapperElement;
    }
}
