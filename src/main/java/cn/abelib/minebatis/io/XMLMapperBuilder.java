package cn.abelib.minebatis.io;

import cn.abelib.minebatis.Configuration;
import org.dom4j.Element;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author abel.huang
 * @date 2020/8/7 21:24
 * 解析XMLMapper
 */
public class XMLMapperBuilder {
    private Configuration configuration;

    public XMLMapperBuilder(Configuration configuration) {
        this.configuration = configuration;
    }

    public Map<String, XNode> parse(Element root) {
        List<Element> list = root.selectNodes("mappers");
        Map<String, XNode> mappers = mapperElement(list);
        bindMapperForNamespace(mappers);
        return mappers;
    }

    private Map<String, XNode> mapperElement(List<Element> list) {
        Map<String, XNode> map = new HashMap<>();
        Element element = list.get(0);
        List content = element.content();
        for (Object o : content) {
            Element e = (Element) o;
            String resource = e.attributeValue("resource");
            XMLStatementBuilder statementBuilder = new XMLStatementBuilder();
            Map<String, XNode> sub = statementBuilder.parseStatementNode(resource);
            map.putAll(sub);
        }
        return map;
    }

    /**
     * todo 解析Mapper
     * Mapper接口绑定
     */
    private void bindMapperForNamespace(Map<String, XNode> mappers) {
        String namespace = "";
        Class<?> boundType = null;
        if(!configuration.hasMapper(boundType)) {
            configuration.addLoadedResource(namespace);
            //绑定 mapper 类
            configuration.addMapper(boundType);
        }
    }

    /**
     * todo
     * 解析缓存<cache>节点
     * 针对二级缓存的配置
     */
    private void cacheElement() {

    }

    /**
     * todo
     * 解析<resultMap>节点
     */
    private void resultMapElement() {

    }
}
