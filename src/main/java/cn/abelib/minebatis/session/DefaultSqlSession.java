package cn.abelib.minebatis.session;

import cn.abelib.minebatis.executor.Executor;
import cn.abelib.minebatis.mapping.MappedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author abel.huang
 * @date 2020/8/6 13:50
 */
public class DefaultSqlSession implements SqlSession {

    private static final Logger log = LoggerFactory.getLogger(DefaultSqlSession.class);

    private final Executor executor;
    private final Configuration configuration;

    public DefaultSqlSession(Configuration configuration, Executor executor) {
        this.configuration = configuration;
        this.executor = executor;
    }

    @Override
    public <T> T selectOne(String statement) {
        return selectOne(statement, null);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        List<T> list = this.selectList(statement, parameter);
        if (list.size() == 1) {
            return list.get(0);
        } else if (list.size() > 1) {
            throw new RuntimeException("TooManyResultException, expect: 1, found: " + list.size());
        } else {
            return null;
        }
    }

    @Override
    public <T> List<T> selectList(String statement) {
        return selectList(statement, null);
    }

    @Override
    public <T> List<T> selectList(String statement, Object parameter) {
        try {
            MappedStatement ms = configuration.getMappedStatement(statement);
            return executor.query(ms, parameter);
        } catch (Exception e) {
            log.error("Query failed for statement: {}", statement, e);
            throw new RuntimeException("Query failed for statement: " + statement, e);
        }
    }

    @Override
    public int insert(String statement) {
        return insert(statement, null);
    }

    @Override
    public int insert(String statement, Object parameter) {
        return update(statement, parameter);
    }

    @Override
    public int update(String statement) {
        return update(statement, null);
    }

    @Override
    public int update(String statement, Object parameter) {
        try {
            MappedStatement ms = configuration.getMappedStatement(statement);
            return executor.update(ms, parameter);
        } catch (Exception e) {
            log.error("Update failed for statement: {}", statement, e);
            throw new RuntimeException("Update failed for statement: " + statement, e);
        }
    }

    @Override
    public int delete(String statement) {
        return delete(statement, null);
    }

    @Override
    public int delete(String statement, Object parameter) {
        return update(statement, parameter);
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.getMapper(type, this);
    }

    @Override
    public void commit() {
        executor.commit();
    }

    @Override
    public void rollback() {
        executor.rollback();
    }

    @Override
    public void close() {
        executor.close();
    }

    @Override
    public Configuration getConfiguration() {
        return this.configuration;
    }
}
