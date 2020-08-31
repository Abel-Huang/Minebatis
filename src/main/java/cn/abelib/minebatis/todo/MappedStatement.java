package cn.abelib.minebatis.todo;

import cn.abelib.minebatis.Configuration;

import java.util.List;

/**
 * @author abel.huang
 * @date 2020/8/17 14:46
 * 保存映射节点
 */

public class MappedStatement {
    private String id;
    private SqlSource sqlSource;
    private String resource;
    private String sql;
    private String sqlCommandType;

    public Configuration getConfiguration() {
        return null;
    }

    public String getId() {
        return null;
    }

    public String getSqlCommandType() {
        return null;
    }

    public List<ResultMap> getResultMaps() {
        return null;
    }

    public String[] getResultSets() {
        return new String[0];
    }
}
