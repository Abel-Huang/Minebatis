package cn.abelib.minebatis.executor.statement;

import cn.abelib.minebatis.executor.parameter.ParameterHandler;
import cn.abelib.minebatis.executor.resultset.ResultSetHandler;
import cn.abelib.minebatis.mapping.MappedStatement;
import cn.abelib.minebatis.session.Configuration;

import java.sql.*;
import java.util.List;

/**
 * @Author: abel.huang
 * @Date: 2020-08-18 00:07
 * todo
 */
public class SimpleStatementHandler implements StatementHandler {
    private final MappedStatement mappedStatement;
    private final ParameterHandler parameterHandler;
    private final Configuration configuration;
    private final ResultSetHandler resultSetHandler;

    public SimpleStatementHandler(MappedStatement ms, Object parameter) throws ClassNotFoundException {
        this.mappedStatement = ms;
        this.configuration = mappedStatement.getConfiguration();
        this.parameterHandler = configuration.newParameterHandler(mappedStatement, parameter);
        this.resultSetHandler = configuration.newResultSetHandler(mappedStatement);
    }

    @Override
    public int update(Statement statement) throws SQLException {
        return 0;
    }

    @Override
    public <E> List<E> query(Statement statement) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;
        // 执行SQL
        ps.execute();
        return resultSetHandler.handleResultSets(ps);
    }

    /**
     * todo 参数设置
     * @param connection
     * @return
     */
    @Override
    public Statement prepare(Connection connection) {
        Statement statement;
        try {
            statement = connection.prepareStatement(mappedStatement.getSql());
            statement.setFetchSize(mappedStatement.getFetchSize());
            return statement;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void parameterize(Statement stmt) throws SQLException, IllegalAccessException {
        parameterHandler.setParameters((PreparedStatement) stmt);
    }
}
