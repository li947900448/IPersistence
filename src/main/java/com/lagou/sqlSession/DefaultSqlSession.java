package com.lagou.sqlSession;

import com.lagou.pojo.Configuration;
import com.lagou.pojo.MappedStatement;
import com.lagou.pojo.SqlComandType;

import java.lang.reflect.*;
import java.util.List;

public class DefaultSqlSession implements SqlSession {

    private Configuration configuration;

    public DefaultSqlSession(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public <E> List<E> selectList(MappedStatement mappedStatement, Object... params) throws Exception {

        //将要去完成对simpleExecutor里的query方法的调用
        simpleExecutor simpleExecutor = new simpleExecutor();
        List<Object> list = simpleExecutor.query(configuration, mappedStatement, params);

        return (List<E>) list;
    }

    @Override
    public <T> T selectOne(MappedStatement mappedStatement, Object... params) throws Exception {
        List<Object> objects = selectList(mappedStatement, params);
        if(objects.size()==1){
            return (T) objects.get(0);
        }else {
            throw new RuntimeException("查询结果为空或者返回结果过多");
        }


    }

    @Override
    public Integer update(MappedStatement mappedStatement, Object... params) throws Exception {
        //将要去完成对simpleExecutor里的query方法的调用
        simpleExecutor simpleExecutor = new simpleExecutor();
        return simpleExecutor.update(configuration, mappedStatement, params);
    }

    @Override
    public Integer delete(MappedStatement mappedStatement, Object... params) throws Exception {
        return update(mappedStatement, params);
    }

    @Override
    public Integer insert(MappedStatement mappedStatement, Object... params) throws Exception {
        return update(mappedStatement, params);
    }

    @Override
    public <T> T getMapper(Class<?> mapperClass) {
        // 使用JDK动态代理来为Dao接口生成代理对象，并返回
        Configuration configuration = this.configuration;
        Object proxyInstance = Proxy.newProxyInstance(DefaultSqlSession.class.getClassLoader(), new Class[]{mapperClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 底层都还是去执行JDBC代码 //根据不同情况，来调用selctList或者selectOne
                // 准备参数 1：statmentid :sql语句的唯一标识：namespace.id= 接口全限定名.方法名
                // 方法名：findAll
                String methodName = method.getName();
                String className = method.getDeclaringClass().getName();

                String statementId = className+"."+methodName;

                // 判断执行什么sql
                MappedStatement mappedStatement = configuration.getMappedStatementMap().get(statementId);

                switch (mappedStatement.getSqlComandType()){
                    case INSERT:{
                        return insert(mappedStatement, args);
                    }
                    case UPDATE:{
                        return update(mappedStatement, args);
                    }
                    case SELECT:{
                        // 获取被调用方法的返回值类型
                        Type genericReturnType = method.getGenericReturnType();
                        // 判断是否进行了 泛型类型参数化
                        if(genericReturnType instanceof ParameterizedType){

                            List<Object> objects = selectList(mappedStatement, args);
                            return objects;
                        }

                        return selectOne(mappedStatement,args);
                    }
                    case DELETE:{
                        return delete(mappedStatement, args);
                    }
                    default:
                        throw new RuntimeException("Unknown execution method for: " + mappedStatement.getId());
                }
            }
        });

        return (T) proxyInstance;
    }

}
