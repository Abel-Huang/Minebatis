package cn.abelib.minebatis.executor.resultset;

import cn.abelib.minebatis.mapping.MappedStatement;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author abel.huang
 * @date 2020-08-17 23:44
 * 默认结果集处理器
 */
public class DefaultResultSetHandler implements ResultSetHandler {

    private final Class<?> resultType;

    public DefaultResultSetHandler(MappedStatement mappedStatement) throws ClassNotFoundException {
        this.resultType = Class.forName(mappedStatement.getResultType());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> handleResultSets(Statement stmt) throws SQLException {
        List<T> results = new ArrayList<>();
        ResultSet rs = stmt.getResultSet();

        if (rs == null) {
            return results;
        }

        try {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                T obj = (T) resultType.getDeclaredConstructor().newInstance();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);

                    if (value == null) {
                        continue;
                    }

                    // 下划线转驼峰
                    String propertyName = underscoreToCamelCase(columnName);

                    // 设置属性值
                    setProperty(obj, propertyName, value);
                }
                results.add(obj);
            }
        } catch (Exception e) {
            throw new SQLException("Failed to handle result sets", e);
        }

        return results;
    }

    /**
     * 设置对象属性值
     */
    private void setProperty(Object obj, String propertyName, Object value) {
        if (value == null) {
            return;
        }

        Class<?> clazz = obj.getClass();

        // 尝试通过 setter 方法设置
        String setterName = "set" + capitalize(propertyName);

        // 获取字段类型
        Field field = findField(clazz, propertyName);
        if (field == null) {
            return;
        }

        Class<?> fieldType = field.getType();
        Object convertedValue = convertValue(value, fieldType);

        if (convertedValue == null) {
            return;
        }

        // 尝试调用 setter 方法
        try {
            Method setter = clazz.getMethod(setterName, fieldType);
            setter.invoke(obj, convertedValue);
        } catch (NoSuchMethodException e) {
            // setter 不存在，尝试直接设置字段
            try {
                field.setAccessible(true);
                field.set(obj, convertedValue);
            } catch (IllegalAccessException ex) {
                // 忽略
            }
        } catch (Exception e) {
            // 忽略设置失败的属性
        }
    }

    /**
     * 查找类中的字段（包括父类）
     */
    private Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    /**
     * 将数据库值转换为目标类型
     */
    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        // 类型已经匹配
        if (targetType.isInstance(value)) {
            return value;
        }

        // 数值类型转换
        if (value instanceof Number) {
            Number number = (Number) value;

            if (targetType == Integer.class || targetType == int.class) {
                return number.intValue();
            } else if (targetType == Long.class || targetType == long.class) {
                return number.longValue();
            } else if (targetType == Double.class || targetType == double.class) {
                return number.doubleValue();
            } else if (targetType == Float.class || targetType == float.class) {
                return number.floatValue();
            } else if (targetType == Short.class || targetType == short.class) {
                return number.shortValue();
            } else if (targetType == Byte.class || targetType == byte.class) {
                return number.byteValue();
            } else if (targetType == BigDecimal.class) {
                if (value instanceof BigDecimal) {
                    return value;
                }
                return BigDecimal.valueOf(number.doubleValue());
            }
        }

        // BigDecimal 特殊处理
        if (value instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) value;
            if (targetType == Integer.class || targetType == int.class) {
                return bd.intValue();
            } else if (targetType == Long.class || targetType == long.class) {
                return bd.longValue();
            } else if (targetType == Double.class || targetType == double.class) {
                return bd.doubleValue();
            } else if (targetType == Float.class || targetType == float.class) {
                return bd.floatValue();
            }
        }

        // 日期类型转换
        if (value instanceof Timestamp) {
            Timestamp ts = (Timestamp) value;
            if (targetType == Date.class || targetType == java.sql.Date.class) {
                return new Date(ts.getTime());
            } else if (targetType == Timestamp.class) {
                return value;
            }
        }

        if (value instanceof java.sql.Date) {
            java.sql.Date date = (java.sql.Date) value;
            if (targetType == Date.class) {
                return new Date(date.getTime());
            } else if (targetType == Timestamp.class) {
                return new Timestamp(date.getTime());
            }
        }

        // 字符串转换
        if (value instanceof String) {
            String str = (String) value;
            if (targetType == String.class) {
                return str;
            } else if (targetType == Character.class || targetType == char.class) {
                return str.isEmpty() ? '\0' : str.charAt(0);
            }
        }

        // Boolean 转换
        if (value instanceof Boolean && (targetType == Boolean.class || targetType == boolean.class)) {
            return value;
        }

        // 字节数组
        if (value instanceof byte[] && targetType == byte[].class) {
            return value;
        }

        // 其他情况尝试直接返回
        return value;
    }

    /**
     * 下划线命名转驼峰命名
     */
    private String underscoreToCamelCase(String name) {
        if (name == null || !name.contains("_")) {
            return name;
        }

        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    result.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }

        return result.toString();
    }

    /**
     * 首字母大写
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
