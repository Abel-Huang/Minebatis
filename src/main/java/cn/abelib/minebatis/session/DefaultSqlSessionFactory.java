package cn.abelib.minebatis.session;


import cn.abelib.minebatis.Configuration;

/**
 * @author abel.huang
 * @date 2020/8/6 13:42
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {
    private final Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SqlSession openSession() {
        return new DefaultSqlSession(configuration.getConnection(), configuration.getMapperElement());
    }
}


