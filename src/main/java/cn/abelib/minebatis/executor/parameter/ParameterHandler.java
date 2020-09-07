package cn.abelib.minebatis.executor.parameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @Author: abel.huang
 * @Date: 2020-08-28 01:07
 */
public interface ParameterHandler {
    Object getParameterObject();

    void setParameters(PreparedStatement ps)
            throws SQLException, IllegalAccessException;
}
