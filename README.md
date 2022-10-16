# Reggie_itheima

黑马瑞吉外卖：https://www.bilibili.com/video/BV13a411q753



22.10.7-22.10.14	普通业务开发

22.10.14-22.10.16 项目优化



VMware上装载Centos7镜像：https://www.bilibili.com/video/BV13a411q753?p=120



## 一、Redis缓存优化：

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



## 二、读写分离：

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



## 三、Nginx负载均衡：

https://www.cnblogs.com/KizunaAI/p/16311032.html#nginx

### **Nginx概述**

Nginx是一款轻量级的web服务器/反向代理服务器及电子邮件（IMAP/POP3）代理服务器。其特点是占有内存少，并发能力强，事实上nginx的并发能力在同类型的网页服务器中表现较好，中国大陆使用nginx的网站有:百度、京东、新浪、网易、腾讯、淘宝等。

Nginx是由伊戈尔·赛索耶夫为俄罗斯访问量第二的Rambler .ru站点（俄文: Paw6nep)开发的，第一个公开版本0.1.e发布于2004年10月4日。

官网: <https://nginx.org/>



### **Nginx优势**

1、作为静态Web服务器来部署静态资源。**Nginx处理静态文件、索引文件，自动索引的效率非常高**

2、作为代理服务器，Nginx可以实现无缓存的反向代理加速，提高网站运行速度

3、作为负载均衡服务器，Nginx既可以在内部直接支持Rails和PHP，也可以支持HTTP代理服务器对外进行服务，同时还**支持简单的容错和利用算法进行负载均衡**

4、在性能方面，Nginx是专门为性能优化而开发的，实现上非常注重效率。它采用内核Poll模型，可以支持更多的并发连接，最大可以支持对5万个并发连接数的响应，而且只占用很低的内存资源

5、在稳定性方面，Nginx采取了**分阶段资源分配技术**，使得CPU与内存的占用率非常低。Nginx官方表示，Nginx保持1万个没有活动的连接，而这些连接只占用2.5MB内存，因此，类似DOS这样的攻击对Nginx来说基本上是没有任何作用的

6、在高可用性方面，**Nginx支持热部署，启动速度特别迅速，因此可以在不间断服务的情况下，对软件版本或者配置进行升级，即使运行数月也无需重新启动，几乎可以做到7x24小时不间断地运行**



### **Nginx下载与安装**

可以到Nginx官方网站下载Nginx的安装包，地址为: <https://nginx.org/en/download.html>

安装过程:
1、安装依赖包yum -y install gcc pcre-devel zlib-devel openssl openssl-devel

2、下载Nginx安装包wget <https://nginx.org/download/nginx-1.16.1.tar.gz>(需要先yum install wget)

3、解压tar -zxvf nginx-1.16.1.tar.gz

4、cd nginx-1.16.1

5、./configure --prefix=/usr/local/nginx

6、make && make install（编译与编译后安装）

### Nginx目录结构

- **如何展示树形结构？**tree命令，先yum install tree

安装完Nginx后，我们先来熟悉一下Nginx的目录结构，如下图:
![image](https://img2022.cnblogs.com/blog/2592691/202205/2592691-20220525213430017-2131615208.png)

重点目录/文件:

- conf/nginx.conf：nginx配置文件
- html：存放静态文件(html、css、Js等)
- logs：日志目录，存放日志文件
- sbin/nginx：二进制文件，用于启动、停止Nginx服务

### Nginx命令

#### 查看版本

在sbin目录下输入`./nginx -v`
![image](https://img2022.cnblogs.com/blog/2592691/202205/2592691-20220525213439288-1059195245.png)

#### 检查配置文件正确性

在启动Nginx服务之前，可以先检查一下conf/nginx.conf文件配置的是否有错误，命令如下:`./nginx -t`

![image](https://img2022.cnblogs.com/blog/2592691/202205/2592691-20220525213446774-1869088051.png)

#### 启动和停止

在sbin目录下：

启动Nginx服务使用如下命令:`./nginx`

停止Nginx服务使用如下命令:`./nginx -s stop`

启动完成后可以查看Nginx进程:`ps -ef | grep nginx`

修改运行的进程数目：
`vim usr/local/nginx/conf/nginx.conf`：

```
worker_processes  2;
```



#### 重新加载配置文件

可以通过修改profile文件配置环境变量，在`/`目录下可以直接使用nginx命令

vim etc/profile

```
PATH=/usr/local/nginx/sbin:$JAVA_HOME/bin:$PATH
```

使配置文件生效：`source /etc/profile`

重启Nginx：`nginx -s reload`

停止Nginx：`nginx -s stop`

启动Nginx：`nginx`

### Nginx配置文件结构

**整体结构介绍**

Nginx配置文件(conf/nginx.conf)整体分为三部分:

- 全局块：和Nginx运行相关的全局配置
- events块：和网络连接相关的配置
- http块：代理、缓存、日志记录、虚拟主机配置
  - http全局块
  - Server块
    - Server全局块
    - location块

**注意**:http块中可以配置多个Server块，每个Server块中可以配置多个location块。

![image](https://img2022.cnblogs.com/blog/2592691/202205/2592691-20220525213459886-1202400981.png)



### Nginx具体应用

#### 部署静态资源

Nginx可以作为静态web服务器来部署静态资源。静态资源指在服务端真实存在并且能够直接展示的一些文件，比如常见的html页面、css文件、js文件、图片、视频等资源。

相对于Tomcat，Nginx处理静态资源的能力更加高效，所以在生产环境下，一般都会将静态资源部署到Nginx中。

将静态资源部署到Nginx非常简单，只需要将文件复制到Nginx安装目录下的html目录中即可。

```
server {
  listen 80;                #监听端口
  server_name localhost;    #服务器名称
  location/{                #匹配客户端请求url
    root html;              #指定静态资源根目录
    index index.html;       #指定默认首页
}
```

#### 反向代理

- 正向代理

  是一个位于客户端和原始服务器(origin server)之间的服务器，为了从原始服务器取得内容，客户端向代理发送一个请求并指定目标(原始服务器)，然后代理向原始服务器转交请求并将获得的内容返回给客户端。

  正向代理的典型用途是为在防火墙内的局域网客户端提供访问Internet的途径。

  正向代理一般是**在客户端设置代理服务器**，通过代理服务器转发请求，最终访问到目标服务器。
  ![image](https://img2022.cnblogs.com/blog/2592691/202205/2592691-20220525213510103-1625991610.png)

- 反向代理

  反向代理服务器位于用户与目标服务器之间，但是对于用户而言，反向代理服务器就相当于目标服务器，即用户直接访问反向代理服务器就可以获得目标服务器的资源，反向代理服务器负责将请求转发给目标服务器。

  用户**不需要知道目标服务器的地址**，也无须在用户端作任何设定。
  ![image](https://img2022.cnblogs.com/blog/2592691/202205/2592691-20220525213519321-202537398.png)

- 配置反向代理

```
server {
  listen       82;
  server_name  localhost;

  location / {
          proxy_pass http://192.168.188.101:8080; #反向代理配置
  } 
}

```

#### 负载均衡

早期的网站流量和业务功能都比较简单，单台服务器就可以满足基本需求，但是随着互联网的发展，业务流量越来越大并且业务逻辑也越来越复杂，单台服务器的性能及单点故障问题就凸显出来了，因此需要多台服务器组成应用集群，进行性能的水平扩展以及避免单点故障出现。

- 应用集群:将同一应用部署到多台机器上，组成应用集群，接收负载均衡器分发的请求，进行业务处理并返回响应数据
- 负载均衡器:将用户请求根据对应的负载均衡算法分发到应用集群中的一台服务器进行处理
  ![image](https://img2022.cnblogs.com/blog/2592691/202205/2592691-20220525213528386-1881671315.png)

**配置负载均衡**:
修改ngnix.conf

```
upstream targetserver{    #upstream指令可以定义一组服务器
  server 192.168.188.101:8080;
  server 192.168.188.101:8081;
}

server {
  listen  8080;
  server_name     localhost;
  location / {
          proxy_pass http://targetserver;
  }
}

```

**负载均衡策略**
![image](https://img2022.cnblogs.com/blog/2592691/202205/2592691-20220525213534999-1519097614.png)





## 四、前后端分离：

