package cn.abelib.minebatis.session;

import java.io.Closeable;
import java.util.List;

/**
 * @author abel.huang
 * @date 2020/8/6 13:53
 */
public interface SqlSession extends Closeable {
    <T> T selectOne(String statement);

    <T> T selectOne(String statement, Object parameter);

    <T> List<T> selectList(String statement);

    <T> List<T> selectList(String statement, Object parameter);

    int insert(String statement);

    int insert(String statement, Object parameter);

    int update(String statement);

    int update(String statement, Object parameter);

    int delete(String statement);

    int delete(String statement, Object parameter);

    <T> T getMapper(Class<T> type);

    void commit();

    void rollback();

    @Override
    void close();

    Configuration getConfiguration();
}
