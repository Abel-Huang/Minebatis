# Minebatis

一个轻量级的 MyBatis 框架实现，用于学习 MyBatis 核心原理。实现了 SQL 映射、动态代理、参数处理、结果集映射等核心功能。

## 功能特性

- **SQL 映射**: XML 配置方式定义 SQL 语句
- **Mapper 代理**: JDK 动态代理实现 Mapper 接口绑定
- **参数处理**: 支持 `#{param}` 占位符参数映射
- **结果集映射**: 自动将 ResultSet 映射为 Java 对象
- **缓存支持**: 一级缓存、LRU 缓存实现
- **配置解析**: dom4j 解析 MyBatis 配置文件

## 项目架构

```
cn.abelib.minebatis
├── binding                     # Mapper 接口绑定
│   ├── MapperProxy             # 动态代理 InvocationHandler
│   ├── MapperProxyFactory      # 代理工厂
│   ├── MapperRegistry          # Mapper 注册中心
│   └── MapperMethod            # SQL 方法执行器
│
├── builder                     # XML 解析构建器
│   ├── XMLConfigBuilder        # 主配置文件解析
│   ├── XMLMapperBuilder        # Mapper 文件解析
│   └── XMLStatementBuilder     # SQL 语句解析
│
├── cache                       # 缓存模块
│   ├── Cache                   # 缓存接口
│   ├── CacheKey                # 缓存键生成
│   ├── PerpetualCache          # 永久缓存实现
│   └── LruCache                # LRU 缓存装饰器
│
├── executor                    # 执行器模块
│   ├── Executor                # 执行器接口
│   ├── SimpleExecutor          # 简单执行器
│   ├── CachingExecutor         # 缓存执行器
│   ├── parameter/
│   │   ├── ParameterHandler            # 参数处理器接口
│   │   └── DefaultParameterHandler     # 默认参数处理
│   ├── resultset/
│   │   ├── ResultSetHandler            # 结果集处理器接口
│   │   └── DefaultResultSetHandler     # 默认结果集处理
│   └── statement/
│       ├── StatementHandler            # Statement 处理器接口
│       └── SimpleStatementHandler      # 简单 Statement 处理
│
├── mapping                     # 映射配置
│   ├── MappedStatement         # SQL 映射信息封装
│   └── SqlCommandType          # SQL 命令类型枚举
│
├── session                     # 会话模块
│   ├── SqlSession              # 会话接口
│   ├── DefaultSqlSession       # 默认会话实现
│   ├── SqlSessionFactory       # 会话工厂接口
│   ├── DefaultSqlSessionFactory # 默认会话工厂
│   └── SqlSessionFactoryBuilder # 会话工厂构建器
│
├── io                          # IO 工具
│   ├── Resources               # 资源加载工具
│   └── ClassLoaderWrapper      # 类加载器包装
│
└── utils                       # 工具类
    ├── StringUtils             # 字符串工具
    └── ParamNameResolver       # 参数名解析器
```

## 核心模块说明

### 1. 会话体系

```
SqlSessionFactoryBuilder       # 构建 SqlSessionFactory
        ↓
SqlSessionFactory              # 创建 SqlSession
        ↓
SqlSession                     # 执行 SQL、获取 Mapper
        ↓
Executor                       # 执行器
        ↓
StatementHandler               # Statement 处理
        ↓
ResultSetHandler               # 结果集处理
```

### 2. Mapper 代理机制

```
SqlSession.getMapper(StudentMapper.class)
        ↓
MapperRegistry.getMapper()
        ↓
MapperProxyFactory.newInstance()
        ↓
Proxy.newProxyInstance() → MapperProxy (InvocationHandler)
        ↓
MapperMethod.execute() → SqlSession.selectOne/selectList()
```

**MapperProxy 核心逻辑**:

```java
public Object invoke(Object proxy, Method method, Object[] args) {
    // Object 方法直接调用
    if (Object.class.equals(method.getDeclaringClass())) {
        return method.invoke(this, args);
    }
    // 执行 SQL
    MapperMethod mapperMethod = cachedMapperMethod(method);
    return mapperMethod.execute(sqlSession, args);
}
```

### 3. SQL 执行流程

```
1. 解析配置
   XMLConfigBuilder.parse() → Configuration
   XMLMapperBuilder.parse() → MappedStatement

2. 创建会话
   SqlSessionFactoryBuilder.build() → SqlSessionFactory
   SqlSessionFactory.openSession() → SqlSession

3. 执行查询
   SqlSession.selectOne() → Executor.query()
   Executor.doQuery() → StatementHandler.query()
   StatementHandler → ParameterHandler → ResultSetHandler

4. 结果返回
   ResultSetHandler.handleResultSets() → List<Entity>
```

### 4. 缓存机制

```
Cache (接口)
    ├── PerpetualCache      # 永久缓存（基于 HashMap）
    └── LruCache            # LRU 缓存装饰器

CacheKey                     # 缓存键生成
    ├── SQL 语句
    ├── 参数值
    └── 分页参数
```

## 使用方法

### 1. 配置文件

**mybatis-config.xml** (主配置):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">

<configuration>
    <settings>
        <setting name="cacheEnabled" value="true"/>
    </settings>

    <environments default="development">
        <environment id="development">
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver" value="com.mysql.jdbc.Driver"/>
                <property name="url" value="jdbc:mysql://127.0.0.1:3306/mydb"/>
                <property name="username" value="root"/>
                <property name="password" value="password"/>
            </dataSource>
        </environment>
    </environments>

    <mappers>
        <mapper resource="mapper/student_mapper.xml"/>
    </mappers>
</configuration>
```

**mapper/student_mapper.xml** (SQL 映射):

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="cn.abelib.example.StudentMapper">
    <select id="queryUserInfoById" parameterType="java.lang.Long"
            resultType="cn.abelib.example.Student">
        SELECT * FROM student WHERE id = #{id}
    </select>
</mapper>
```

### 2. 定义实体和 Mapper 接口

**实体类**:

```java
public class Student {
    private Long id;
    private String name;
    private Integer age;
    // getter/setter
}
```

**Mapper 接口**:

```java
public interface StudentMapper {
    Student queryUserInfoById(Long id);
}
```

### 3. 使用示例

```java
public class MybatisExample {
    public static void main(String[] args) {
        // 1. 加载配置文件
        InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");

        // 2. 构建 SqlSessionFactory
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        // 3. 获取 SqlSession
        SqlSession session = sqlSessionFactory.openSession();

        // 方式一：直接通过 SQL ID 查询
        Student student = session.selectOne("cn.abelib.example.StudentMapper.queryUserInfoById", 1L);

        // 方式二：通过 Mapper 接口查询（推荐）
        StudentMapper mapper = session.getMapper(StudentMapper.class);
        Student student = mapper.queryUserInfoById(1L);

        System.out.println(student);
    }
}
```

## 实现细节

### MappedStatement 构建

每个 SQL 语句对应一个 MappedStatement 对象：

```java
public class MappedStatement {
    private String id;                      // SQL 唯一标识
    private String sql;                     // SQL 语句
    private String sqlCommandType;          // SELECT/INSERT/UPDATE/DELETE
    private Map<Integer, String> parameter; // 参数映射
    private String parameterType;           // 参数类型
    private String resultType;              // 返回类型
    private Configuration configuration;    // 配置引用
}
```

使用建造者模式创建：

```java
MappedStatement ms = new MappedStatement.Builder(
    configuration, id, sqlCommandType, sql,
    parameter, parameterType, resultType
).build();
```

### 参数处理

`DefaultParameterHandler` 负责将参数设置到 PreparedStatement：

```java
public void setParameters(PreparedStatement ps) {
    Map<Integer, String> parameterMap = mappedStatement.getParameter();
    for (Map.Entry<Integer, String> entry : parameterMap.entrySet()) {
        int index = entry.getKey();
        String paramName = entry.getValue();
        Object value = getParamValue(paramName);
        ps.setObject(index, value);
    }
}
```

### 结果集映射

`DefaultResultSetHandler` 负责将 ResultSet 映射为 Java 对象：

```java
public <E> List<E> handleResultSets(Statement stmt) {
    ResultSet rs = stmt.getResultSet();
    String resultType = mappedStatement.getResultType();
    Class<?> clazz = Class.forName(resultType);

    List<E> results = new ArrayList<>();
    while (rs.next()) {
        E obj = (E) clazz.newInstance();
        // 反射设置属性值
        for (Field field : clazz.getDeclaredFields()) {
            Object value = rs.getObject(field.getName());
            field.setAccessible(true);
            field.set(obj, value);
        }
        results.add(obj);
    }
    return results;
}
```

### 缓存键生成

```java
public class CacheKey {
    private int hashCode;
    private List<Object> objects;

    public void update(Object object) {
        objects.add(object);
        hashCode = hashCode * 37 + object.hashCode();
    }
}

// 生成缓存键
CacheKey cacheKey = new CacheKey();
cacheKey.update(ms.getId());          // SQL ID
cacheKey.update(parameter);           // 参数值
cacheKey.update(rowBounds.getOffset()); // 分页参数
```

## 执行流程图

```
┌─────────────────────────────────────────────────────────────────┐
│                        Application                              │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│  SqlSessionFactoryBuilder.build(inputStream)                    │
│    └── XMLConfigBuilder.parse()                                 │
│          ├── settingsElement()                                  │
│          ├── dataSourceElement()                                │
│          └── mapperElement()                                    │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│  SqlSessionFactory.openSession()                                │
│    └── DefaultSqlSession                                        │
│          └── Executor (SimpleExecutor)                          │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│  SqlSession.getMapper(StudentMapper.class)                      │
│    └── MapperProxyFactory.newInstance()                         │
│          └── Proxy.newProxyInstance(MapperProxy)                │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│  mapper.queryUserInfoById(1L)                                   │
│    └── MapperProxy.invoke()                                     │
│          └── MapperMethod.execute()                             │
│                └── SqlSession.selectOne()                       │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│  Executor.query()                                               │
│    └── SimpleExecutor.doQuery()                                 │
│          ├── Configuration.newStatementHandler()                │
│          ├── StatementHandler.prepare()    → PreparedStatement │
│          ├── ParameterHandler.setParameters()                   │
│          └── StatementHandler.query()                           │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│  ResultSetHandler.handleResultSets()                            │
│    └── 反射映射 ResultSet → List<Entity>                        │
└─────────────────────────────────────────────────────────────────┘
```

## 已实现功能

| 功能 | 状态 |
|------|------|
| XML 配置解析 | ✅ |
| Mapper 接口动态代理 | ✅ |
| SQL 执行 | ✅ |
| 参数映射 (`#{param}`) | ✅ |
| 结果集映射 | ✅ |
| 一级缓存 | ✅ |
| LRU 缓存 | ✅ |
| 缓存键生成 | ✅ |

## 待实现功能

| 功能 | 优先级 | 状态 |
|------|--------|------|
| 事务支持 | 高 | TODO |
| 二级缓存 | 高 | TODO |
| 连接池 | 中 | TODO |
| INSERT/UPDATE/DELETE 操作 | 中 | TODO |
| 动态 SQL (`<if>`, `<where>` 等) | 中 | TODO |
| 结果映射 (`<resultMap>`) | 中 | TODO |
| 插件机制 | 低 | TODO |
| 延迟加载 | 低 | TODO |
| TypeHandler | 低 | TODO |

## 快速开始

```bash
# 克隆项目
git clone https://github.com/Abel-Huang/Minebatis.git

# 编译
mvn compile

# 运行测试
mvn test

# 运行示例（需配置数据库）
mvn exec:java -Dexec.mainClass="cn.abelib.example.MybatisExample"
```

## 技术栈

- Java 21
- dom4j (XML 解析)
- jaxen (XPath)
- MySQL Connector 5.1.46
- Lombok
- Guava

## 参考资料

- [MyBatis 官方文档](https://mybatis.org/mybatis-3/zh/index.html)
- [MyBatis 源码](https://github.com/mybatis/mybatis-3)

## License

MIT License
