package cn.abelib.minebatis.session;

import cn.abelib.minebatis.Configuration;
import cn.abelib.minebatis.builder.XMLConfigBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author abel.huang
 * @date 2020/8/6 13:53
 */
public class SqlSessionFactoryBuilder {

    public DefaultSqlSessionFactory build(InputStream inputStream) throws IOException {
        try {
            XMLConfigBuilder parser = new XMLConfigBuilder();
            Configuration configuration = parser.parse(inputStream);
            return new DefaultSqlSessionFactory(configuration);
        } finally {
            inputStream.close();
        }
    }
}
