package cn.abelib.minebatis.builder;

import cn.abelib.minebatis.builder.XMLConfigBuilder;
import cn.abelib.minebatis.io.Resources;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Author: abel.huang
 * @Date: 2020-08-31 01:18
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
    public void parseTest() {
        xmlConfigBuilder.parse(inputStream);
    }
}
