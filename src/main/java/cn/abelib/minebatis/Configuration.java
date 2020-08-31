package cn.abelib.minebatis;

import cn.abelib.minebatis.binding.MapperRegistry;
import cn.abelib.minebatis.executor.CachingExecutor;
import cn.abelib.minebatis.executor.Executor;
import cn.abelib.minebatis.executor.SimpleExecutor;
import cn.abelib.minebatis.executor.statement.SimpleStatementHandler;
import cn.abelib.minebatis.io.XNode;
import cn.abelib.minebatis.session.SqlSession;
import cn.abelib.minebatis.todo.BoundSql;
import cn.abelib.minebatis.todo.MappedStatement;
import cn.abelib.minebatis.executor.statement.StatementHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author abel.huang
 * @date 2020/8/6 13:50
 */
public class Configuration {
    private Map<String, String> dataSource;
    private Map<String, XNode> mapperElement;
    private boolean cacheEnabled;

    /**
     * todo
     */
    private MapperRegistry mapperRegistry;

    public void setDataSource(Map<String, String> dataSource) {
        this.dataSource = dataSource;
        try {
            Class.forName(dataSource.get("driver"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * todo 后续需要自定义连接池类
     * 创建数据库连接
     * @return
     */
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(dataSource.get("url"), dataSource.get("username"), dataSource.get("password"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setMapperElement(Map<String, XNode> mapperElement) {
        this.mapperElement = mapperElement;
    }

    public Map<String, XNode> getMapperElement() {
        return mapperElement;
    }

    public MappedStatement getMappedStatement(String statement) {
        return null;
    }

    /**
     * todo
     * @param executor
     * @param ms
     * @param parameter
     * @param boundSql
     * @return
     */
    public StatementHandler newStatementHandler(Executor executor,  MappedStatement ms, Object parameter, BoundSql boundSql) {
        StatementHandler statementHandler = new SimpleStatementHandler(executor, ms, parameter, boundSql);

        return statementHandler;
    }

    public boolean hasStatement(String statementName) {
        return false;
    }

    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return mapperRegistry.getMapper(type, sqlSession);
    }

    /**
     * 仅支持SimpleExecutor
     * @return
     */
    public Executor newExecutor() {
        Executor executor = new SimpleExecutor();
        if (cacheEnabled) {
            executor = new CachingExecutor(executor);
        }
        return executor;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public boolean hasMapper(Class<?> boundType) {
        return false;
    }

    public void addLoadedResource(String namespace) {

    }

    /**
     * 绑定mapper类
     * @param type
     * @param <T>
     */
    public <T> void addMapper(Class<T> type) {
        mapperRegistry.addMapper(type);
    }
}
