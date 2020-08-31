package cn.abelib.minebatis.io;

import com.google.common.collect.Maps;
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

    public Map<String, XNode> parseStatementNode(String resource) {
        Map<String, XNode> map = Maps.newHashMap();
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
                XNode xNode = new XNode();
                xNode.setNamespace(namespace);
                xNode.setId(id);
                xNode.setParameterType(parameterType);
                xNode.setResultType(resultType);
                xNode.setSql(sql);
                xNode.setParameter(parameter);

                map.put(namespace + "." + id, xNode);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return map;
    }
}
