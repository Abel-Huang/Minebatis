package cn.abelib.minebatis.session;

import cn.abelib.minebatis.Configuration;
import cn.abelib.minebatis.executor.Executor;
import cn.abelib.minebatis.mapping.MappedStatement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.Date;
import java.util.*;

/**
 * @author abel.huang
 * @date 2020/8/6 13:50
 */
public class DefaultSqlSession implements SqlSession {
    private Connection connection;
    private Executor executor;
    private Configuration configuration;
    private Map<String, MappedStatement> mappedStatements;

    /**
     * todo init
     * @param connection
     */
    public DefaultSqlSession(Connection connection) {
        this.connection = connection;
        this.mappedStatements = configuration.getMappedStatements();
    }

    @Override
    public <T> T selectOne(String statement) {
        return selectOne(statement, null);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        MappedStatement mappedStatement = mappedStatements.get(statement);
        Map<Integer, String> parameterMap = mappedStatement.getParameter();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mappedStatement.getSql());
            buildParameter(preparedStatement, parameter, parameterMap);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<T> objects = resultSetConverter(resultSet, Class.forName(mappedStatement.getResultType()));
            return objects.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> List<T> selectList(String statement) {
        return selectList(statement, null);
    }

    @Override
    public <T> List<T> selectList(String statement, Object parameter) {
        MappedStatement mappedStatement = mappedStatements.get(statement);
        Map<Integer, String> parameterMap = mappedStatement.getParameter();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mappedStatement.getSql());
            buildParameter(preparedStatement, parameter, parameterMap);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSetConverter(resultSet, Class.forName(mappedStatement.getResultType()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public <T> List<T> selectList(String statement, Object parameter, String boundSql) {
        try {
            MappedStatement ms = configuration.getMappedStatement(statement);
            return executor.query(ms, parameter, boundSql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 获得Mapper
     * @param type
     * @param <T>
     * @return
     */
    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.getMapper(type, this);
    }

    @Override
    public void close() {

    }

    @Override
    public Configuration getConfiguration() {
        return this.configuration;
    }

    private void buildParameter(PreparedStatement preparedStatement,
                                Object parameter,
                                Map<Integer, String> parameterMap) throws SQLException, IllegalAccessException {
        if (Objects.isNull(parameter)) {
            return;
        }
        int size = parameterMap.size();
        // 单个参数
        if (parameter instanceof Long) {
            for (int i = 1; i <= size; i++) {
                preparedStatement.setLong(i, Long.parseLong(parameter.toString()));
            }
            return;
        }

        if (parameter instanceof Integer) {
            for (int i = 1; i <= size; i++) {
                preparedStatement.setInt(i, Integer.parseInt(parameter.toString()));
            }
            return;
        }

        if (parameter instanceof String) {
            for (int i = 1; i <= size; i++) {
                preparedStatement.setString(i, parameter.toString());
            }
            return;
        }

        Map<String, Object> fieldMap = new HashMap<>();
        // 对象参数
        Field[] declaredFields = parameter.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            String name = field.getName();
            field.setAccessible(true);
            Object obj = field.get(parameter);
            field.setAccessible(false);
            fieldMap.put(name, obj);
        }

        for (int i = 1; i <= size; i++) {
            String parameterDefine = parameterMap.get(i);
            Object obj = fieldMap.get(parameterDefine);

            if (obj instanceof Short) {
                preparedStatement.setShort(i, Short.parseShort(obj.toString()));
                continue;
            }

            if (obj instanceof Integer) {
                preparedStatement.setInt(i, Integer.parseInt(obj.toString()));
                continue;
            }
            if (obj instanceof Long) {
                preparedStatement.setLong(i, Long.parseLong(obj.toString()));
                continue;
            }

            if (obj instanceof String) {
                preparedStatement.setString(i, obj.toString());
                continue;
            }
            if (obj instanceof Date) {
                preparedStatement.setDate(i, (java.sql.Date) obj);
            }
        }
    }

    private <T> List<T> resultSetConverter(ResultSet resultSet, Class<?> clazz) {
        List<T> list = new ArrayList<>();
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
                list.add(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
