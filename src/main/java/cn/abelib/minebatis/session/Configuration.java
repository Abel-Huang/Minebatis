package cn.abelib.minebatis.session;

import cn.abelib.minebatis.binding.MapperRegistry;
import cn.abelib.minebatis.executor.Executor;
import cn.abelib.minebatis.executor.SimpleExecutor;
import cn.abelib.minebatis.executor.parameter.DefaultParameterHandler;
import cn.abelib.minebatis.executor.parameter.ParameterHandler;
import cn.abelib.minebatis.executor.resultset.DefaultResultSetHandler;
import cn.abelib.minebatis.executor.resultset.ResultSetHandler;
import cn.abelib.minebatis.executor.statement.SimpleStatementHandler;
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
        mapperRegistry = new MapperRegistry();
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
     * @param ms
     * @param parameter
     * @return
     */
    public StatementHandler newStatementHandler(MappedStatement ms, Object parameter) throws ClassNotFoundException {
        return new SimpleStatementHandler(ms, parameter);
    }

    public boolean hasStatement(String statementName) {
        return mappedStatements.containsKey(statementName);
    }

    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return mapperRegistry.getMapper(type, sqlSession);
    }

    /**
     *  todo
     *  cacheing
     * 仅支持SimpleExecutor
     * @return
     */
    public Executor newExecutor() {
        Executor executor = new SimpleExecutor();
        if (cacheEnabled) {
            //executor = new CachingExecutor(executor);
        }
        return executor;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public boolean hasMapper(Class<?> type) {
        return mapperRegistry.hasMapper(type);
    }

    /**
     * todo unuse now
     * @param namespace
     */
    public void addLoadedResource(String namespace) {

    }

    public void addMappedStatement(MappedStatement ms) {
        mappedStatements.put(ms.getId(), ms);
    }

    public ResultSetHandler newResultSetHandler(MappedStatement mappedStatement) throws ClassNotFoundException {
        return new DefaultResultSetHandler(mappedStatement);
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

    /**
     * 新建参数处理器
     * @param mappedStatement
     * @param parameter
     * @return
     */
    public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameter) {
        return new DefaultParameterHandler(mappedStatement, parameter);
    }
}
