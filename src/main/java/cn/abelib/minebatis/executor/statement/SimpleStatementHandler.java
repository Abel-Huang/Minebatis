package cn.abelib.minebatis.executor.statement;

import cn.abelib.minebatis.executor.parameter.ParameterHandler;
import cn.abelib.minebatis.executor.resultset.ResultSetHandler;
import cn.abelib.minebatis.mapping.MappedStatement;
import cn.abelib.minebatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;

/**
 * @author abel.huang
 * @date 2020-08-18 00:07
 * 简单 Statement 处理器实现
 */
public class SimpleStatementHandler implements StatementHandler {

    private static final Logger log = LoggerFactory.getLogger(SimpleStatementHandler.class);

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
        PreparedStatement ps = (PreparedStatement) statement;
        ps.execute();
        return ps.getUpdateCount();
    }

    @Override
    public <E> List<E> query(Statement statement) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;
        ps.execute();
        return resultSetHandler.handleResultSets(ps);
    }

    @Override
    public Statement prepare(Connection connection) {
        Statement statement;
        try {
            statement = connection.prepareStatement(mappedStatement.getSql());
            statement.setFetchSize(mappedStatement.getFetchSize());
            return statement;
        } catch (SQLException e) {
            log.error("Failed to prepare statement for SQL: {}", mappedStatement.getSql(), e);
            throw new RuntimeException("Failed to prepare statement", e);
        }
    }

    @Override
    public void parameterize(Statement stmt) throws SQLException, IllegalAccessException {
        parameterHandler.setParameters((PreparedStatement) stmt);
    }
}
