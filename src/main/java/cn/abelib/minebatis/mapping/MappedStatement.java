package cn.abelib.minebatis.mapping;

import cn.abelib.minebatis.session.Configuration;

import java.util.Map;

/**
 * @author abel.huang
 * @date 2020/8/17 14:46
 * 用于存放映射SQL节点的属性(每个SQL Node对应一个MappedStatement)
 */

public class MappedStatement {
    /**
     * 节点的id属性 + 命名空间namespace
     */
    private Configuration configuration;
    private String id;
    private String sql;
    private String sqlCommandType;
    private Map<Integer, String> parameter;
    private String parameterType;
    private String resultType;


    private MappedStatement() {
        // private constructor
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public String getParameterType() {
        return this.parameterType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getResultType() {
        return this.resultType;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return this.sql;
    }

    public void setParameter(Map<Integer, String> parameter) {
        this.parameter = parameter;
    }

    public Map<Integer, String> getParameter() {
        return this.parameter;
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    public String getSqlCommandType() {
        return sqlCommandType;
    }

    /**
     * todo
     * @return
     */
    public int getFetchSize() {
        return 100;
    }

    /**
     * MappedStatement的建造者模式
     */
    public static class Builder {
        private MappedStatement mappedStatement = new MappedStatement();

        public Builder(Configuration configuration, String id,
                       String sqlCommandType, String sql,
                       Map<Integer, String> parameter,
                       String parameterType, String resultType) {
            mappedStatement.configuration = configuration;
            mappedStatement.id = id;
            mappedStatement.sqlCommandType = sqlCommandType;
            mappedStatement.sql = sql;
            mappedStatement.parameter = parameter;
            mappedStatement.parameterType = parameterType;
            mappedStatement.resultType = resultType;
        }

        public MappedStatement build() {
            return mappedStatement;
        }
    }
}
