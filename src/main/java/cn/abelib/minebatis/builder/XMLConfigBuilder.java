package cn.abelib.minebatis.builder;

import cn.abelib.minebatis.Configuration;
import cn.abelib.minebatis.utils.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author abel.huang
 * @date 2020/8/7 21:24
 * 解析XMLConfig
 */
public class XMLConfigBuilder {
    private SAXReader saxReader;
    private Configuration configuration;

    public XMLConfigBuilder() {
        this.saxReader = new SAXReader();
        this.configuration = new Configuration();
    }

    public Configuration parse(InputStream inputStream) {
        try {
            Document document = saxReader.read(new InputSource(inputStream));
            parseConfiguration(document.getRootElement());
            return configuration;
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 配置解析
     * @param root
     * @return
     */
    private void parseConfiguration(Element root) {
        settingsElement(root);

        dataSourceElement(root);

        mapperElement(root);
    }

    /**
     * 解析Setting节点，目前仅支持cacheEnabled
     * @param root
     */
    private void settingsElement(Element root) {
        List<Element> list = root.selectNodes("//setting");
        for (Element e : list) {
            String name = e.attributeValue("name");
            String value = e.attributeValue("value");
            if (StringUtils.isEquals(name, "cacheEnabled")) {
                boolean cacheEnabled = Boolean.parseBoolean(value);
                configuration.setCacheEnabled(cacheEnabled);
                break;
            }
        }
    }

    /**
     * 解析数据源相关
     */
    private void dataSourceElement(Element root) {
        List<Element> list = root.selectNodes("//dataSource");
        Map<String, String> dataSource = new HashMap<>(4);
        Element element = list.get(0);
        List content = element.content();
        for (Object o : content) {
            Element e = (Element) o;
            String name = e.attributeValue("name");
            String value = e.attributeValue("value");
            dataSource.put(name, value);
        }
        configuration.setDataSource(dataSource);
    }

    /**
     * 解析Mapper文件位置
     * @param root
     */
    private void mapperElement(Element root) {
        XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(configuration);
        xmlMapperBuilder.parse(root);
    }
}
