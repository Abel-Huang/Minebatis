package cn.abelib.minebatis.executor.statement;

import cn.abelib.minebatis.executor.Executor;
import cn.abelib.minebatis.todo.BoundSql;
import cn.abelib.minebatis.todo.MappedStatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: abel.huang
 * @Date: 2020-08-18 00:07
 * todo
 */
public class SimpleStatementHandler implements StatementHandler {

    public SimpleStatementHandler(Executor executor, MappedStatement ms, Object parameter, BoundSql boundSql) {

    }

    @Override
    public int update(Statement statement, BoundSql boundSql) throws SQLException {
        return 0;
    }

    @Override
    public <E> List<E> query(Statement statement) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;
        ps.execute();
        return new ArrayList<>();
    }

    /**
     * todo 参数设置
     * @param connection
     * @return
     */
    @Override
    public Statement prepare(Connection connection) {
        Statement statement = null;
        try {
            statement = connection.prepareStatement("");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void parameterize(Statement stmt) {

    }
}
