package cn.abelib.minebatis.io;

import java.util.Map;

/**
 * @author abel.huang
 * @date 2020/8/6 13:54
 */
public class XNode {
    private String namespace;
    private String id;
    private String parameterType;
    private String resultType;
    private String sql;
    private Map<Integer, String> parameter;

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return this.namespace;
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
}
