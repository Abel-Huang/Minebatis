package cn.abelib.minebatis.builder;

import cn.abelib.minebatis.session.Configuration;
import cn.abelib.minebatis.io.Resources;
import org.dom4j.Element;
import java.util.List;
import java.util.Objects;

/**
 * @author abel.huang
 * @date 2020/8/7 21:24
 * 解析XMLMapper
 */
public class XMLMapperBuilder {
    private Configuration configuration;
    private MapperBuilderAssistant builderAssistant;

    public XMLMapperBuilder(Configuration configuration) {
        this.configuration = configuration;
        this.builderAssistant = new MapperBuilderAssistant();
    }

    public void parse(Element root) {
        List<Element> list = root.selectNodes("mappers");
        mapperElement(list);
        bindMapperForNamespace();
    }

    private void mapperElement(List<Element> list) {
        Element element = list.get(0);
        List content = element.content();
        for (Object o : content) {
            Element e = (Element) o;
            String resource = e.attributeValue("resource");
            XMLStatementBuilder statementBuilder = new XMLStatementBuilder(configuration, builderAssistant);
            statementBuilder.parseStatementNode(resource);
        }
    }

    /**
     * todo 解析Mapper
     * Mapper接口绑定
     */
    private void bindMapperForNamespace() {
        String namespace = builderAssistant.getCurrentNamespace();
        if (namespace != null) {
            Class<?> boundType = null;
            try {
                boundType = Resources.classForName(namespace);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            if (Objects.nonNull(boundType)) {
                // 检测当前类是否已经被绑定过
                if(!configuration.hasMapper(boundType)) {
                    configuration.addLoadedResource("namespace" + namespace);
                    //绑定 mapper 类
                    configuration.addMapper(boundType);
                }
            }
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
