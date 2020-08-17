package cn.abelib.minebatis.todo;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author abel.huang
 * @date 2020/8/7 21:07
 * 结果集映射
 */
public interface ResultSetHandler {
    <E> List<E> handleResultSets(Statement stmt) throws SQLException;

    void handleOutputParameters(CallableStatement cs) throws SQLException;
}
