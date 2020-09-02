package cn.abelib.minebatis.executor.resultset;

import cn.abelib.minebatis.Configuration;
import cn.abelib.minebatis.executor.Executor;
import cn.abelib.minebatis.mapping.MappedStatement;
import cn.abelib.example.ResultMap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author: abel.huang
 * @Date: 2020-08-17 23:44
 */
public class DefaultResultSetHandler implements ResultSetHandler {
    private final Executor executor;
    private final Configuration configuration;
    private final MappedStatement mappedStatement;

    public DefaultResultSetHandler(Executor executor, Configuration configuration, MappedStatement mappedStatement) {
        this.executor = executor;
        this.configuration = configuration;
        this.mappedStatement = mappedStatement;
    }

    @Override
    public <E> List<E> handleResultSets(Statement stmt) throws SQLException {
        final List<Object> multiResults = new ArrayList<>();
        int resultSetCount = 0;

        ResultSetWrapper rsw = getFirstResultSet(stmt);
        List<ResultMap> resultMaps = mappedStatement.getResultMaps();
        int rsmCount = resultMaps.size();

        for (; Objects.nonNull(rsw) && rsmCount > resultSetCount; resultSetCount ++) {
            ResultMap rsm = resultMaps.get(resultSetCount);
            // 处理ResultSet
            handleResultSet(rsw, rsm, multiResults, null);
            // 处理下一个ResultSet
            rsw = getNextResultSet(stmt);

            cleanUpdateAfterHandlingResultSet();
        }

        return collapseSingleResultList(multiResults);
    }

    private <E> List<E> collapseSingleResultList(List<Object> multiResults) {
        return null;
    }

    /**
     * 清空集合
     */
    private void cleanUpdateAfterHandlingResultSet() {
    }

    private void handleResultSet(ResultSetWrapper rsw, ResultMap rsm, List<Object> multiResults, Object o) {

    }

    /**
     *  获得 Statement 第一个ResultSet
     * @param stmt
     * @return
     * @throws SQLException
     */
    private ResultSetWrapper getFirstResultSet(Statement stmt) throws SQLException {
        // 获得ResultSet对象
        ResultSet rs = stmt.getResultSet();
        while (Objects.isNull(rs)) {
            // 判断是否还有待处理的ResultSet
            if (stmt.getMoreResults()) {
                rs = stmt.getResultSet();
            } else {
                // 所有的ResultSet都处理完毕
                if (stmt.getUpdateCount() == -1) {
                    break;
                }
            }
        }
        return Objects.nonNull(rs) ? new ResultSetWrapper(rs, configuration) : null;
    }

    private ResultSetWrapper getNextResultSet(Statement stmt) throws SQLException {
        if (stmt.getConnection().getMetaData().supportsMultipleResultSets()) {
            // 检测是否还有待处理的结果集, 若存在封装成ResultSetWrapper对象并返回
            if (!((!stmt.getMoreResults() && (stmt.getUpdateCount()) == -1))) {
                ResultSet rs = stmt.getResultSet();
                return Objects.nonNull(rs) ? new ResultSetWrapper(rs, configuration) : null;
            }
        }
        return null;
    }
}
