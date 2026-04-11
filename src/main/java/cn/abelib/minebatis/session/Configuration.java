package cn.abelib.minebatis.session;

import cn.abelib.minebatis.binding.MapperRegistry;
import cn.abelib.minebatis.cache.Cache;
import cn.abelib.minebatis.datasource.DataSource;
import cn.abelib.minebatis.datasource.UnpooledDataSource;
import cn.abelib.minebatis.executor.CachingExecutor;
import cn.abelib.minebatis.executor.Executor;
import cn.abelib.minebatis.executor.SimpleExecutor;
import cn.abelib.minebatis.executor.parameter.DefaultParameterHandler;
import cn.abelib.minebatis.executor.parameter.ParameterHandler;
import cn.abelib.minebatis.executor.resultset.DefaultResultSetHandler;
import cn.abelib.minebatis.executor.resultset.ResultSetHandler;
import cn.abelib.minebatis.executor.statement.SimpleStatementHandler;
import cn.abelib.minebatis.mapping.MappedStatement;
import cn.abelib.minebatis.executor.statement.StatementHandler;
import cn.abelib.minebatis.transaction.JdbcTransaction;
import cn.abelib.minebatis.transaction.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author abel.huang
 * @date 2020/8/6 13:50
 */
public class Configuration {
    private DataSource dataSource;
    private boolean cacheEnabled;
    private Map<String, MappedStatement> mappedStatements;
    private MapperRegistry mapperRegistry;
    private Map<String, Cache> caches;

    public Configuration() {
        mapperRegistry = new MapperRegistry();
        mappedStatements = new HashMap<>();
        caches = new HashMap<>();
    }

    /**
     * 设置数据源配置（兼容 XML 配置）
     */
    public void setDataSource(Map<String, String> dataSourceConfig) {
        this.dataSource = new UnpooledDataSource(
                dataSourceConfig.get("driver"),
                dataSourceConfig.get("url"),
                dataSourceConfig.get("username"),
                dataSourceConfig.get("password")
        );
    }

    /**
     * 设置数据源（支持注入第三方连接池）
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 获取数据源
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * 创建数据库连接
     */
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get connection", e);
        }
    }

    public MappedStatement getMappedStatement(String id) {
        return mappedStatements.get(id);
    }

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
     * 创建执行器
     * @param transaction 事务
     * @return Executor
     */
    public Executor newExecutor(Transaction transaction) {
        Executor executor = new SimpleExecutor(transaction);
        if (cacheEnabled) {
            executor = new CachingExecutor(executor);
        }
        return executor;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public boolean hasMapper(Class<?> type) {
        return mapperRegistry.hasMapper(type);
    }

    public void addLoadedResource(String namespace) {
        // 用于防止重复加载
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

    /**
     * 添加二级缓存
     * @param namespace 命名空间
     * @param cache 缓存实例
     */
    public void addCache(String namespace, Cache cache) {
        caches.put(namespace, cache);
    }

    /**
     * 获取二级缓存
     * @param namespace 命名空间
     * @return 缓存实例
     */
    public Cache getCache(String namespace) {
        return caches.get(namespace);
    }

    /**
     * 创建新事务（默认非自动提交）
     * @return Transaction
     */
    public Transaction newTransaction() {
        return new JdbcTransaction(this, false);
    }

    /**
     * 创建新事务
     * @param autoCommit 是否自动提交
     * @return Transaction
     */
    public Transaction newTransaction(boolean autoCommit) {
        return new JdbcTransaction(this, autoCommit);
    }
}
