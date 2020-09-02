package cn.abelib.minebatis.executor.statement;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author abel.huang
 * @date 2020/8/7 21:06
 */
public interface StatementHandler {
    int update(Statement statement, String boundSql) throws SQLException;

    <E> List<E> query(Statement statement) throws SQLException;

    Statement prepare(Connection connection);

    void parameterize(Statement stmt);
}
