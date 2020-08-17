package cn.abelib.example;

import cn.abelib.minebatis.io.Resources;
import cn.abelib.minebatis.session.SqlSession;
import cn.abelib.minebatis.session.SqlSessionFactory;
import cn.abelib.minebatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author abel.huang
 * @date 2020/5/19 11:39
 */
public class MybatisExample {
    public static void main(String[] args) {
        String resource = "mybatis-config.xml";
        InputStream inputStream;
        try {
            inputStream = Resources.getResourceAsStream(resource);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

            SqlSession session = sqlSessionFactory.openSession();
            try {
                Student user = session.selectOne("cn.abelib.example.IStudentDao.queryUserInfoById", 1L);
                System.out.println(user);
            } finally {
                session.close();
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
