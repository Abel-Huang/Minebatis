package cn.abelib.minebatis.executor.parameter;

import cn.abelib.minebatis.mapping.MappedStatement;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * @author abel.huang
 * @date 2020-08-28 01:09
 * 默认参数处理器
 */
public class DefaultParameterHandler implements ParameterHandler {

    private final Object parameter;
    private final Map<Integer, String> parameterMap;

    public DefaultParameterHandler(MappedStatement mappedStatement, Object parameter) {
        this.parameter = parameter;
        this.parameterMap = mappedStatement.getParameter();
    }

    @Override
    public Object getParameterObject() {
        return parameter;
    }

    @Override
    public void setParameters(PreparedStatement ps) throws SQLException {
        if (Objects.isNull(parameter)) {
            return;
        }

        int size = parameterMap.size();

        // 处理 Map 类型参数（多参数情况）
        if (parameter instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> paramMap = (Map<String, Object>) parameter;
            for (int i = 1; i <= size; i++) {
                String paramName = parameterMap.get(i);
                Object value = paramMap.get(paramName);
                setParameter(ps, i, value);
            }
            return;
        }

        // 处理基本类型参数
        if (parameter instanceof Number) {
            for (int i = 1; i <= size; i++) {
                setParameter(ps, i, parameter);
            }
            return;
        }

        if (parameter instanceof String) {
            for (int i = 1; i <= size; i++) {
                ps.setString(i, (String) parameter);
            }
            return;
        }

        // 处理对象参数（通过反射获取字段值）
        try {
            Field[] fields = parameter.getClass().getDeclaredFields();
            for (int i = 1; i <= size; i++) {
                String paramName = parameterMap.get(i);
                Object value = getFieldValue(parameter, paramName, fields);
                setParameter(ps, i, value);
            }
        } catch (IllegalAccessException e) {
            throw new SQLException("Failed to set parameters", e);
        }
    }

    /**
     * 从对象中获取字段值
     */
    private Object getFieldValue(Object obj, String fieldName, Field[] fields) throws IllegalAccessException {
        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                field.setAccessible(true);
                Object value = field.get(obj);
                field.setAccessible(false);
                return value;
            }
        }
        return null;
    }

    /**
     * 设置参数值，处理各种类型转换
     */
    private void setParameter(PreparedStatement ps, int index, Object value) throws SQLException {
        if (value == null) {
            ps.setObject(index, null);
            return;
        }

        // 基本类型处理
        if (value instanceof Short) {
            ps.setShort(index, (Short) value);
        } else if (value instanceof Integer) {
            ps.setInt(index, (Integer) value);
        } else if (value instanceof Long) {
            ps.setLong(index, (Long) value);
        } else if (value instanceof Float) {
            ps.setFloat(index, (Float) value);
        } else if (value instanceof Double) {
            ps.setDouble(index, (Double) value);
        } else if (value instanceof BigDecimal) {
            ps.setBigDecimal(index, (BigDecimal) value);
        } else if (value instanceof String) {
            ps.setString(index, (String) value);
        } else if (value instanceof Boolean) {
            ps.setBoolean(index, (Boolean) value);
        } else if (value instanceof Byte) {
            ps.setByte(index, (Byte) value);
        } else if (value instanceof Character) {
            ps.setString(index, String.valueOf((Character) value));
        } else if (value instanceof java.sql.Date) {
            ps.setDate(index, (java.sql.Date) value);
        } else if (value instanceof java.sql.Timestamp) {
            ps.setTimestamp(index, (Timestamp) value);
        } else if (value instanceof java.sql.Time) {
            ps.setTime(index, (java.sql.Time) value);
        } else if (value instanceof Date) {
            // java.util.Date 转换为 java.sql.Timestamp
            ps.setTimestamp(index, new Timestamp(((Date) value).getTime()));
        } else if (value instanceof byte[]) {
            ps.setBytes(index, (byte[]) value);
        } else {
            // 其他类型尝试直接设置
            ps.setObject(index, value);
        }
    }
}
