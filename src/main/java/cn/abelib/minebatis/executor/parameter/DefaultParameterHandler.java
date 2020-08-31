package cn.abelib.minebatis.executor.parameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @Author: abel.huang
 * @Date: 2020-08-28 01:09
 */
public class DefaultParameterHandler implements ParameterHandler {
    @Override
    public Object getParameterObject() {
        return null;
    }

    @Override
    public void setParameters(PreparedStatement ps) throws SQLException {

    }
}
