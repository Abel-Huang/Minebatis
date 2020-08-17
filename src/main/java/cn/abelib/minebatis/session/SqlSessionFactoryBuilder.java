package cn.abelib.minebatis.session;

import cn.abelib.minebatis.Configuration;
import cn.abelib.minebatis.io.XMLConfigBuilder;

import java.io.InputStream;
import java.io.Reader;

/**
 * @author abel.huang
 * @date 2020/8/6 13:53
 */
public class SqlSessionFactoryBuilder {

    public DefaultSqlSessionFactory build(Reader reader) {
        XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder();
        Configuration configuration = xmlConfigBuilder.parse(reader);
        return new DefaultSqlSessionFactory(configuration);
    }

    public DefaultSqlSessionFactory build(InputStream inputStream) {
        XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder();
        Configuration configuration = xmlConfigBuilder.parse(inputStream);
        return new DefaultSqlSessionFactory(configuration);
    }
}
