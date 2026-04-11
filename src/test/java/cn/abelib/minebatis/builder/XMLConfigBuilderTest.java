package cn.abelib.minebatis.builder;

import cn.abelib.minebatis.io.Resources;
import cn.abelib.minebatis.session.Configuration;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * @author abel.huang
 * @date 2020-08-31 01:18
 * XML 配置解析测试
 */
public class XMLConfigBuilderTest {

    private XMLConfigBuilder xmlConfigBuilder;
    private InputStream inputStream;

    @Before
    public void init() throws IOException {
        String resource = "mybatis-config.xml";
        inputStream = Resources.getResourceAsStream(resource);
        xmlConfigBuilder = new XMLConfigBuilder();
    }

    @Test
    public void testParse() {
        Configuration configuration = xmlConfigBuilder.parse(inputStream);

        assertNotNull(configuration);
        assertTrue(configuration.isCacheEnabled());
        assertNotNull(configuration.getMappedStatements());
    }

    @Test
    public void testParseMappedStatements() {
        Configuration configuration = xmlConfigBuilder.parse(inputStream);

        // 验证 Mapper SQL 语句已解析
        assertNotNull(configuration.getMappedStatements());
        assertTrue(configuration.getMappedStatements().size() > 0);
    }

    @Test
    public void testHasStatement() {
        Configuration configuration = xmlConfigBuilder.parse(inputStream);

        // 验证特定的 SQL 语句已注册
        assertTrue(configuration.hasStatement("cn.abelib.example.StudentMapper.queryUserInfoById"));
    }

    @Test
    public void testGetMappedStatement() {
        Configuration configuration = xmlConfigBuilder.parse(inputStream);

        assertNotNull(configuration.getMappedStatement("cn.abelib.example.StudentMapper.queryUserInfoById"));
    }
}
