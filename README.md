# Reggie_itheima

黑马瑞吉外卖：https://www.bilibili.com/video/BV13a411q753



22.10.7-22.10.14	普通业务开发

22.10.14-22.10.16 项目优化



VMware上装载Centos7镜像：https://www.bilibili.com/video/BV13a411q753?p=120



## 一、Redis缓存优化包括：

https://www.cnblogs.com/KizunaAI/p/16302096.html

- **1、缓存短信验证码**



- **2、缓存菜品数据**

在高并发的情况下，频繁查询数据库会导致系统性能下降，服务端响应时间增长。现在需要对此方法进行缓存优化，提高系统的性能。

优化：

1、改造DishController的list方法，先从Redis中获取菜品数据，如果有则直接返回，无需查询数据库;如果没有则查询数据库，并将查询到的菜品数据放入Redis。

2、改造DishController的save和update方法，加入清理缓存的逻辑

**注意**：在使用缓存过程中，要注意保证数据库中的数据和缓存中的数据一致，如果数据库中的数据发生变化，需要及时清理缓存数据。



- **3、Spring Cache**

**Spring Cache介绍**

Spring cache是一个框架，实现了基于注解的缓存功能，只需要简单地加一个注解，就能实现缓存功能。

Spring Cache提供了一层抽象，底层可以切换不同的cache实现。具体就是通过CacheManager接口来统一不同的缓存技术。

CacheManager是Spring提供的各种缓存技术抽象接口。

针对不同的缓存技术需要实现不同的CacheManager，例如：

- RedisCacheManager：使用Redis作为缓存技术

**Spring Cache常用注解**

@EnableCaching：开启缓存注解功能

@Cacheable：在方法执行前spring先查看缓存中是否有数据，若有直接返回，没有查询后将返回值放到缓存中

@CachePut：将方法的返回值放到缓存中

@CacheEvict：将一条或多条数据从缓存中删除

```java
@CachePut（value = "usercache", key = "#user.id"）
@CacheEvict（value = "usercache", key = "#id"）
```

- value：缓存的名称。每个缓存名称下面可以有多个key
- key：缓存的key



在spring boot项目中，使用缓存技术只需在项目中导入相关缓存技术的依赖包，并在启动类上使用@EnableCaching开启缓存支持即可。

例如，使用Redis作为缓存技术，只需要导入Spring data Redis的maven坐标即可。



- **4、缓存套餐数据**

前面我们已经实现了移动端套餐查看功能，对应的服务端方法为SetmealController的list方法，此方法会根据前端提交的查询条件进行数据库查询操作。在高并发的情况下，频繁查询数据库会导致系统性能下降，服务端响应时间增长。现在需要对此方法进行缓存优化，提高系统的性能。

具体的实现思路如下:

1、导入Spring Cache和Redis相关maven坐标

2、在application.yml中配置缓存数据的过期时间

3、在启动类上加入@EnableCaching注解，开启缓存注解功能

4、在SetmealController的list方法上加入@Cacheable注解

5、在SetmealController的save和delete方法上加入CacheEvict注解



## 二、读写分离包括：

https://www.cnblogs.com/KizunaAI/p/16311032.html#%E8%AF%BB%E5%86%99%E5%88%86%E7%A6%BB

问题分析：读和写所有压力都由一台数据库承担，压力大，数据库服务器磁盘损坏则数据丢失，单点故障

**解决办法：Mysql主从复制**

MysSQL主从复制是一个异步的复制过程，底层是基于Mysql数据库自带的**二进制日志**功能。就是一台或多台AysQL数据库(slave，即**从库**）从另一台MysQL数据库(master，即**主库**）**进行日志的复制然后再解析日志并应用到自身**，最终实现从库的数据和主库的数据保持一致。MySQL主从复制是MysQL数据库自带功能，无需借助第三方工具。

MysQL复制过程分成三步:

- master将改变记录到二进制日志（ binary log)
- slave将master的binary log拷贝到它的中继日志（relay log）
- slave重做中继日志中的事件，将改变应用到自己的数据库中

![image](https://img2022.cnblogs.com/blog/2592691/202205/2592691-20220525213352219-1805015334.png)

**配置-前置条件**

提前准备好两台服务器，分别安装Mysql并启动服务成功

- 主库Master 192.168.19.128
- 从库slave 192.168.19.129

1.更改主库配置，重启Mysql服务，然后登录Mysql执行如下：

GRANT REPLICATION SLAVE ON *.* to 'xiaoming'@'%' identified by 'Root@123456';

注:上面SQL的作用是创建一个用户**xiaoming**，密码为**Root@123456**，并且给xiaoming用户授予**REPLICATION SLAVE**权限。常用于建立复制时所需要用到的用户权限，也就是slave必须被master授权具有该权限的用户，才能通过该用户复制。

2.更改从库配置，重启Mysql服务，然后登录Mysql执行如下：

change master to master_host='192.168.19.128',master_user='xiaoming',master_password='Root@123456',master_log_file='mysql-bin.000001',master_log_pos=441;

start slave;

表明本库用用户xiaoming登录作为192.168.19.128上mysql的从库。



**读写分离好处：**

面对日益增加的系统访问量，数据库的吞吐量面临着巨大瓶颈。对于同一时刻有大量并发读操作和较少写操作类型的应用系统来说，将数据库拆分为**主库和从库**，主库负责处理事务性的增删改操作，从库负责处理查询操作，能够有效的避免由数据更新导致的行锁，使得整个系统的查询性能得到极大的改善。
![image](https://img2022.cnblogs.com/blog/2592691/202205/2592691-20220525213418223-286629127.png)

**Sharding-JDBC介绍**

Sharding-JDBC定位为轻量级Java框架，在Java的JDBC层提供的额外服务。它使用客户端直连数据库,以jar包形式提供服务，无需额外部署和依赖，可理解为增强版的JDBC驱动，完全兼容JDBC和各种ORM框架。

使用Sharding-JDBC可以在程序中轻松的实现数据库读写分离。

- 适用于任何基于JDBC的ORM框架，如: JPA, Hibernate,Mybatis, Spring JDBC Template或直接使用JDBC。
- 支持任何第三方的数据库连接池，如:DBCP，C3PO,BoneCP, Druid, HikariCP等。
- 支持任意实现JDBC规范的数据库。目前支持MySQL，Oracle,SQLServer，PostgreSQL以及任何遵循SQL92标准的数据库。



## 负载均衡包括：





## 前后端分离：

