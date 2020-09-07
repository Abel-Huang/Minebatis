package cn.abelib.minebatis.session;

import cn.abelib.minebatis.executor.Executor;
import cn.abelib.minebatis.mapping.MappedStatement;
import java.util.*;

/**
 * @author abel.huang
 * @date 2020/8/6 13:50
 */
public class DefaultSqlSession implements SqlSession {
    private Executor executor;
    private Configuration configuration;

    /**
     * @param configuration
     */
    public DefaultSqlSession(Configuration configuration) {
        this.executor = configuration.newExecutor();
        this.configuration = configuration;
    }

    @Override
    public <T> T selectOne(String statement) {
        return selectOne(statement, null);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        List<T> list = this.selectList(statement, parameter);
        // 返回正确的结果
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
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 获得Mapper
     * @param type
     * @param <T>
     * @return
     */
    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.getMapper(type, this);
    }

    @Override
    public void close() {

    }

    @Override
    public Configuration getConfiguration() {
        return this.configuration;
    }
}
