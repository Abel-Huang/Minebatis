package cn.abelib.minebatis.builder;

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
 * @Author: abel.huang
 * @Date: 2020-08-18 23:06
 * 解析SQL节点
 */
public class XMLStatementBuilder {
    private final String SQL_PATTERN = "(#\\{(.*?)})";
    private Configuration configuration;
    private MapperBuilderAssistant builderAssistant;

    public XMLStatementBuilder(Configuration configuration, MapperBuilderAssistant builderAssistant) {
        this.configuration = configuration;
        this.builderAssistant = builderAssistant;
    }

    /**
     * todo 后面需要继续完善和拆分
     * @param resource
     */
    public void parseStatementNode(String resource) {
        try {
            Reader reader = Resources.getResourceAsReader(resource);
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(new InputSource(reader));
            Element root = document.getRootElement();
            //命名空间
            String namespace = root.attributeValue("namespace");
            // SELECT todo 其他的语句 INSERT DELETE UPDATE 其他节点
            List<Element> selectNodes = root.selectNodes("select");
            for (Element node : selectNodes) {
                String id = node.attributeValue("id");
                String parameterType = node.attributeValue("parameterType");
                String resultType = node.attributeValue("resultType");
                String sql = node.getText().trim();
                // ? 匹配
                Map<Integer, String> parameter = new HashMap<>();
                Pattern pattern = Pattern.compile(SQL_PATTERN);
                Matcher matcher = pattern.matcher(sql);
                for (int i = 1; matcher.find(); i++) {
                    String g1 = matcher.group(1);
                    String g2 = matcher.group(2);
                    parameter.put(i, g2);
                    sql = sql.replace(g1, "?");
                }
                String msId = namespace + "." + id;
                MappedStatement.Builder mappedStatementBuilder = new MappedStatement
                        .Builder(configuration, msId, SqlCommandType.SELECT, sql, parameter, parameterType,resultType);
                MappedStatement mappedStatement = mappedStatementBuilder.build();

                configuration.addMappedStatement(mappedStatement);
                builderAssistant.setCurrentNamespace(namespace);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
