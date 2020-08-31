package cn.abelib.minebatis.executor.resultset;

import cn.abelib.minebatis.Configuration;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: abel.huang
 * @Date: 2020-08-24 22:19
 */
public class ResultSetWrapper {
    private final ResultSet resultSet;

    /**
     * 列名
     */
    private final List<String> columnNames = new ArrayList<>();
    private final List<String> classNames = new ArrayList<>();
    /**
     * 列类型
     */
    private final List<Integer> jdbcTypes = new ArrayList<>();

    private final Map<String, List<String>> mappedColumnNamesMap = new HashMap<>();
    private final Map<String, List<String>> unMappedColumnNamesMap = new HashMap<>();



    public ResultSetWrapper(ResultSet resultSet, Configuration configuration) throws SQLException {
        this.resultSet = resultSet;

        final ResultSetMetaData metaData = resultSet.getMetaData();
        final int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i ++) {
            // 默认取的是 columnLabel
            columnNames.add(metaData.getColumnLabel(i));
            jdbcTypes.add(metaData.getColumnType(i));
        }


    }
}
