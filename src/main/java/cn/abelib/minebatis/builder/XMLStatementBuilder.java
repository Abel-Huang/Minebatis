package cn.abelib.minebatis.builder;

import cn.abelib.minebatis.cache.Cache;
import cn.abelib.minebatis.cache.PerpetualCache;
import cn.abelib.minebatis.session.Configuration;
import cn.abelib.minebatis.io.Resources;
import cn.abelib.minebatis.mapping.MappedStatement;
import cn.abelib.minebatis.mapping.SqlCommandType;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author abel.huang
 * @date 2020-08-18 23:06
 * 解析SQL节点
 */
public class XMLStatementBuilder {

    private static final String SQL_PATTERN = "(#\\{(.*?)})";

    private final Configuration configuration;
    private final MapperBuilderAssistant builderAssistant;

    public XMLStatementBuilder(Configuration configuration, MapperBuilderAssistant builderAssistant) {
        this.configuration = configuration;
        this.builderAssistant = builderAssistant;
    }

    /**
     * 解析 Mapper XML 文件
     * @param resource 资源路径
     */
    public void parseStatementNode(String resource) {
        try {
            Reader reader = Resources.getResourceAsReader(resource);
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(new InputSource(reader));
            Element root = document.getRootElement();

            // 命名空间
            String namespace = root.attributeValue("namespace");
            builderAssistant.setCurrentNamespace(namespace);

            // 解析 <cache> 标签（二级缓存）
            cacheElement(root, namespace);

            // 解析 SQL 语句
            parseStatements(root, namespace);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 解析所有 SQL 语句
     */
    private void parseStatements(Element root, String namespace) {
        // 解析 select
        parseStatement(root, namespace, "select", SqlCommandType.SELECT);

        // 解析 insert
        parseStatement(root, namespace, "insert", SqlCommandType.INSERT);

        // 解析 update
        parseStatement(root, namespace, "update", SqlCommandType.UPDATE);

        // 解析 delete
        parseStatement(root, namespace, "delete", SqlCommandType.DELETE);
    }

    /**
     * 解析指定类型的 SQL 语句
     */
    private void parseStatement(Element root, String namespace, String tagName, String sqlCommandType) {
        List<Element> nodes = root.selectNodes(tagName);
        for (Element node : nodes) {
            String id = node.attributeValue("id");
            String parameterType = node.attributeValue("parameterType");
            String resultType = node.attributeValue("resultType");
            String sql = node.getText().trim();

            // 参数解析
            Map<Integer, String> parameter = new HashMap<>();
            Pattern pattern = Pattern.compile(SQL_PATTERN);
            Matcher matcher = pattern.matcher(sql);
            String parsedSql = sql;
            for (int i = 1; matcher.find(); i++) {
                String g1 = matcher.group(1);
                String g2 = matcher.group(2);
                parameter.put(i, g2);
                parsedSql = parsedSql.replace(g1, "?");
            }

            String msId = namespace + "." + id;

            // 获取该 namespace 的缓存
            Cache cache = configuration.getCache(namespace);

            MappedStatement.Builder mappedStatementBuilder = new MappedStatement.Builder(
                    configuration, msId, sqlCommandType, parsedSql, parameter, parameterType, resultType);

            // 设置缓存
            if (cache != null) {
                mappedStatementBuilder.cache(cache);
            }

            MappedStatement mappedStatement = mappedStatementBuilder.build();
            configuration.addMappedStatement(mappedStatement);
        }
    }

    /**
     * 解析 <cache> 标签
     */
    private void cacheElement(Element root, String namespace) {
        Element cacheElement = (Element) root.selectSingleNode("cache");
        if (cacheElement != null) {
            // 创建缓存实例
            Cache cache = new PerpetualCache(namespace);
            configuration.addCache(namespace, cache);
        }
    }
}
