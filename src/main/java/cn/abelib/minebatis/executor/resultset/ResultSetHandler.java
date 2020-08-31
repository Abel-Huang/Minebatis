package cn.abelib.minebatis.executor.resultset;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author abel.huang
 * @date 2020/8/7 21:07
 * 结果集映射
 */
public interface ResultSetHandler {
    /**
     * 生成对应的结果对象集合
     * @param stmt
     * @param <E>
     * @return
     * @throws SQLException
     */
    <E> List<E> handleResultSets(Statement stmt) throws SQLException;
}
