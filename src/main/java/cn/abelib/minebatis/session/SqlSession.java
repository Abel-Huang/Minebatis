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

    @Override
    void close();
}
