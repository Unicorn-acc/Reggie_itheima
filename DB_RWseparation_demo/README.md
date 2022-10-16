## **读写分离入门案例**



#### Sharding-JDBC介绍

Sharding-JDBC定位为轻量级Java框架，在Java的JDBC层提供的额外服务。它使用客户端直连数据库,以jar包形式提供服务，无需额外部署和依赖，可理解为增强版的JDBC驱动，完全兼容JDBC和各种ORM框架。

使用Sharding-JDBC可以在程序中轻松的实现数据库读写分离。

- 适用于任何基于JDBC的ORM框架，如: JPA, Hibernate,Mybatis, Spring JDBC Template或直接使用JDBC。
- 支持任何第三方的数据库连接池，如:DBCP，C3PO,BoneCP, Druid, HikariCP等。
- 支持任意实现JDBC规范的数据库。目前支持MySQL，Oracle,SQLServer，PostgreSQL以及任何遵循SQL92标准的数据库。



#### 使用Sharding-JDBC实现读写分离步骤

1、导入maven坐标

```
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-jdbc-spring-boot-starter</artifactId>
    <version>4.0.0-RC1</version>
</dependency>

```

2、在配置文件中配置读写分离规则

```
spring:
  shardingsphere:
    datasource:
      names:
        master,slave
      # 主数据源
      master:
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://192.168.188.100:3306/rw?characterEncoding=utf-8
        username: root
        password: 123456
      # 从数据源
      slave:
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://192.168.188.101:3306/rw?characterEncoding=utf-8
        username: root
        password: 123456
    masterslave:
      # 读写分离配置
      load-balance-algorithm-type: round_robin #轮询
      # 最终的数据源名称
      name: dataSource
      # 主库数据源名称
      master-data-source-name: master
      # 从库数据源名称列表，多个逗号分隔
      slave-data-source-names: slave
    props:
      sql:
        show: true #开启SQL显示，默认false
```

3、在配置文件中配置允许bean定义覆盖配置项

```
spring:
    main:
        allow-bean-definition-overriding: true
```

---

启动：会创建两个dataSource

2022-10-16 15:10:21.405  INFO 19336 --- [           main] com.alibaba.druid.pool.DruidDataSource   : {dataSource-1} inited
2022-10-16 15:10:22.514  INFO 19336 --- [           main] com.alibaba.druid.pool.DruidDataSource   : {dataSource-2} inited

使用Postman发送请求，在Controller中打断点，可以观察dataSource中存在主从数据库。

Get：localhost:8080/user/12345

Post：localhost:8080/user；填入Body中name和age值

**可以看到：插入在主库，查询在从库**