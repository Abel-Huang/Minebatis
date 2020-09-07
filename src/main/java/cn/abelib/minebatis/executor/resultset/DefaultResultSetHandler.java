package cn.abelib.minebatis.executor.resultset;

import cn.abelib.minebatis.mapping.MappedStatement;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: abel.huang
 * @Date: 2020-08-17 23:44
 */
public class DefaultResultSetHandler implements ResultSetHandler {
    private final Class<?> clazz;

    public DefaultResultSetHandler(MappedStatement mappedStatement) throws ClassNotFoundException {
        this.clazz = Class.forName(mappedStatement.getResultType());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> handleResultSets(Statement stmt) throws SQLException {
        final List<T> multiResults = new ArrayList<>();
        ResultSet resultSet = stmt.getResultSet();
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            // 每次遍历行值
            while (resultSet.next()) {
                T obj = (T) clazz.newInstance();
                for (int i = 1; i <= columnCount; i++) {
                    Object value = resultSet.getObject(i);
                    String columnName = metaData.getColumnName(i);
                    String setMethod = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
                    Method method;
                    if (value instanceof Timestamp) {
                        method = clazz.getMethod(setMethod, Date.class);
                    } else {
                        method = clazz.getMethod(setMethod, value.getClass());
                    }
                    method.invoke(obj, value);
                }
                multiResults.add(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return multiResults;
    }
}
