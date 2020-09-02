package cn.abelib.minebatis;

import cn.abelib.minebatis.binding.MapperRegistry;
import cn.abelib.minebatis.executor.CachingExecutor;
import cn.abelib.minebatis.executor.Executor;
import cn.abelib.minebatis.executor.SimpleExecutor;
import cn.abelib.minebatis.executor.statement.SimpleStatementHandler;
import cn.abelib.minebatis.session.SqlSession;
import cn.abelib.minebatis.mapping.MappedStatement;
import cn.abelib.minebatis.executor.statement.StatementHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author abel.huang
 * @date 2020/8/6 13:50
 */
public class Configuration {
    private Map<String, String> dataSource;
    private boolean cacheEnabled;
    private Map<String, MappedStatement> mappedStatements;
    private MapperRegistry mapperRegistry;

    public Configuration() {
        mapperRegistry = new MapperRegistry(this);
        mappedStatements = new HashMap<>();
    }

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

    public MappedStatement getMappedStatement(String id) {
        return mappedStatements.get(id);
    }

    /**
     * todo
     * @param executor
     * @param ms
     * @param parameter
     * @param boundSql
     * @return
     */
    public StatementHandler newStatementHandler(Executor executor,  MappedStatement ms, Object parameter, String boundSql) {
        StatementHandler statementHandler = new SimpleStatementHandler(executor, ms, parameter, boundSql);

        return statementHandler;
    }

    public boolean hasStatement(String statementName) {
        return mappedStatements.containsKey(statementName);
    }

    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return mapperRegistry.getMapper(type, sqlSession);
    }

    /**
     *  todo
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

    public boolean hasMapper(Class<?> type) {
        return mapperRegistry.hasMapper(type);
    }

    public void addLoadedResource(String namespace) {

    }

    public void addMappedStatement(MappedStatement ms) {
        mappedStatements.put(ms.getId(), ms);
    }

    /**
     * 绑定mapper类
     * @param type
     * @param <T>
     */
    public <T> void addMapper(Class<T> type) {
        mapperRegistry.addMapper(type);
    }

    public Map<String, MappedStatement> getMappedStatements() {
        return this.mappedStatements;
    }
}
