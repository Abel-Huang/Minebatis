package cn.abelib.minebatis.io;

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
