package cn.abelib.minebatis.binding;


import cn.abelib.minebatis.session.Configuration;
import cn.abelib.minebatis.mapping.SqlCommandType;
import cn.abelib.minebatis.session.SqlSession;
import cn.abelib.minebatis.mapping.MappedStatement;
import cn.abelib.minebatis.utils.ParamNameResolver;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @Author: abel.huang
 * @Date: 2020-08-20 00:43
 */
public class MapperMethod {
    private final SqlCommand command;
    private final MethodSignature signature;

    public MapperMethod(Class<?> mapperInterface, Method method, Configuration configuration) {
        this.command = new SqlCommand(configuration, mapperInterface, method);
        this.signature = new MethodSignature(configuration, mapperInterface, method);
    }

    /**
     * todo 具体执行增删改查
     * @param sqlSession
     * @param args
     * @return
     */
    public Object execute(SqlSession sqlSession, Object[] args) {
        Object result = null;
        switch (command.getType()) {
            case SqlCommandType.INSERT:
                // todo
                break;

            case SqlCommandType.UPDATE:
                // todo
                break;

            case SqlCommandType.SELECT:
                // 判断返回值是不是多值类型
                if (signature.returnsMany) {
                    result = executeForMany(sqlSession, args);
                } else {
                    // 返回值为单元素
                    Object param = signature.convertArgsToSqlCommandParam(args);
                    result = sqlSession.selectOne(command.getName(), param);
                }
                break;

            case SqlCommandType.DELETE:
                // todo
                break;

            default:
                throw new RuntimeException("....");
        }
        // 如果返回类型为基本类型，单返回值为null，需要抛出异常
        if (Objects.isNull(result) && signature.returnsType.isPrimitive()) {
            throw new RuntimeException("");
        }
       return result;
    }

    /**
     * todo 针对多值的处理
     * @param sqlSession
     * @param args
     * @param <E>
     * @return
     */
    private <E> Object executeForMany(SqlSession sqlSession, Object[] args) {
        List<E> result;
        Object param = signature.convertArgsToSqlCommandParam(args);

        result = sqlSession.selectList(command.getName(), param);

        return result;
    }

    /**
     * name SQL语句的名称
     * type SQL语句的类型，INSERT，UPDATE，DELETE，SELECT
     */
    public static class SqlCommand {
        private final String name;
        private final String type;

        public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
            String statementName = mapperInterface.getName() + "." + method.getName();
            MappedStatement ms = null;
            if (configuration.hasStatement(statementName)) {
                ms = configuration.getMappedStatement(statementName);
            } else if (!mapperInterface.equals(method.getDeclaringClass())) {
                String parentStatementName = method.getDeclaringClass().getName() + "." + method.getName();
                if (configuration.hasStatement(parentStatementName)) {
                    ms = configuration.getMappedStatement(parentStatementName);
                }
            }
            if (Objects.isNull(ms)) {
                throw new RuntimeException();
            } else {
                name = ms.getId();
                type = ms.getSqlCommandType();
                if (Objects.isNull(type)) {
                    throw new RuntimeException();
                }
            }
        }

        public String getName() {
            return this.name;
        }

        public String getType() {
            return this.type;
        }
    }

    /**
     * 方法签名，该类包含被拦截方法(mapper方法)的一些信息
     *
     * returnsMany返回值是否是集合或者数组
     * returnsMap返回值是否是Map
     * returnType返回值类型
     *  说明： 因为不支持ResultHandler，所以返回值不会为void；
     *  returnsMany默认为List类型；
     *  Map暂时不支持
     */
    public static class MethodSignature {
        private final boolean returnsMany;
        private final Class<?> returnsType;

        private final ParamNameResolver paramNameResolver;

        /**
         * 解析参数列表
         * @param configuration
         * @param mapperInterface
         * @param method
         */
        public MethodSignature(Configuration configuration, Class<?> mapperInterface, Method method) {
            Type type = method.getAnnotatedReturnType().getType();
            if (type instanceof Class<?>) {
                this.returnsType = (Class<?>) type;
            } else if (type instanceof ParameterizedType) {
                this.returnsType = (Class<?>) ((ParameterizedType) type).getRawType();
            } else {
                this.returnsType = method.getReturnType();
            }

            this.returnsMany = this.returnsType.isArray() || Collection.class.isAssignableFrom(this.returnsType);
            this.paramNameResolver = new ParamNameResolver(method);
        }

        /**
         * todo 后续会将参数转换移到这里, 增加多个参数的处理
         *
         * @param args
         * @return
         */
        public Object convertArgsToSqlCommandParam(Object[] args) {
            return args[0];
        }
    }
}
