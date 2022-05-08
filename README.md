# Liberg
> A high-performance and lightweight suites for spring-boot-based web development.
> **Liberg**为从零基础小白到中高级Java Web开发者提供一站式的极速开发体验和解决方案。
> 特别适合快速迭代的中小项目。

源代码仓库地址：

- gitee: [https://gitee.com/liberg-cn/Liberg](https://gitee.com/liberg-cn/Liberg)

- github: [https://github.com/liberg-cn/Liberg](https://github.com/liberg-cn/Liberg)



## 设计理念

从外部视角来看，**Web系统=数据+功能**。`Liberg`就是围绕“**数据**”和“**接口**”来设计和构架整个Web系统。

**数据**：由`data.entity`包下的实体类进行承载，映射到数据库中的表。`entity`的一个字段映射为数据库表的一列。通过注解`@dbmap(isMap=false)`设定为不映射的**类**或**成员**除外。

**接口**：即`service.interfaces`包下的接口声明类，映射为`controller.api.XxxControllor`和`service.XxxService`。

其中，`XxxController`是承接`http`请求的入口。每一个接口方法，映射为一个接口`uri`。

`XxxService`是开发者实现业务逻辑、编写逻辑代码的地方。

基于`Liberg`来开发`Web`应用，开发者只需要定义好**实体类**和**接口类**（对外提供`API`），然后编写具体业务逻辑代码即可。其他`ORM`等相关胶水代码由`LibergCoder`插件进行自动的创建和维护。

```text
  _      _ _                    
 | |    (_| |                   
 | |     _| |__   ___ _ __ __ _ 
 | |    | | '_ \ / _ | '__/ _` |
 | |____| | |_) |  __| | | (_| |
 |______|_|_.__/ \___|_|  \__, |
                           __/ |
 Liberg (v2.0.0)          |___/ 
```

使用`Liberg`开发Web服务，需要用到`Liberg ORM`库（`普通Jar`）和`LibergCoder`（`Idea插件Jar`）两部分。

- **Liberg**：轻量**零反射**`ORM`核心，除了`mysql-connector-java`和`logback`外，无其他依赖。可用于任何Java服务端程序开发。
- **LibergCoder**：Idea插件，负责`Spring Boot` Java代码生成——解析**实体类**和**接口类**生成脚手架代码，加速项目开发。

## 真正零反射的ORM

`Liberg`是一个真正实现“**零反射**”的`Java ORM`框架。

**反射**赋予了Java动态编程的能力。通过反射可以在运行时通过**全限定类名**动态地创建对象、获取对象的属性和方法、为对象赋予新的属性值、调用对象中的方法等等。利用Java的反射机制，动态地创建对象、为对象属性赋值，就可以很容易地实现将数据表中的一条记录转换为一个`Java`实体类的对象。几乎所有`ORM`（Object Relational Mapping）框架就就是这个套路。差别不过是在反射的基础上做了多大程度的优化。

反射的强大特性在代码自动化和框架场景中大有用武之地，但也有一个缺点：那就是——**慢**。

通过`Liberg ORM`精致地封装，配合`LibergCoder`代码生成，`Liberg`真正做到了实体关系映射上的“**零反射**”。

总之，Liberg同时注重**性能**和**开发体验**，是一个极轻量级的`ORM`框架。

## 其他特点

### 便捷的双向映射

`LibergCoder`插件读取`Java实体类`，自动建表、自动跟踪`DDL`升级，生成`XxxDao`数据访问类。`Liberg ORM`管理数据库连接池，完成数据行到实体类对象的转换。

支持的映射类型如下：

| 序号 | Java实体字段类型                     | 数据表字段类型 |
| ---- | ------------------------------------ | -------------- |
| 1    | byte                                 | TINYINT        |
| 2    | int                                  | INT            |
| 3    | long                                 | BIGINT         |
| 4    | @dbmap(length=x) String，其中x<=4095 | VARCHAR(x)     |
| 5    | @dbmap(length=y) String，其中y>=4096 | TEXT           |

> 提示
>
> 浮点数为了保留精度，请使用long/String类型进行存储。

### 极速开发：创建最少的文件、手敲最少的代码

很多需要手动创建的代码、配置文件，`LibergCoder`自动完成创建和维护。在`LibergCoder`插件的加持下，实现了完全自动化的`Spring Boot Web`项目初始化，包括`.properties`配置创建、`pom.xml`依赖添加、数据库创建、数据表创建、表结构升级等等。

`LibergCoder`会创建和维护必要的支撑代码，开发者只写业务：**定义实体类、定义`API`接口、编写业务方法的具体实现**。

**让开发者创建最少的文件、写最少的代码**。这是`Liberg`项目诞生的初衷。

### `SQL DQL/DML` 风格的代码

```java
// 示例data.dao.UserDao.java
// 其中columnXxx在UserDao的父类data.dao.impl.UserDaoImpl中定义
// 而UserDaoImpl是完全由代码插件LibergCoder维护的

// 通过openId字段查询单条记录
return getEq(columnOpenId, "xxxxxxx");

// 通过parentId字段查询多条记录，最多返回50条数据
return getEqs(columnParentId, 1000, 50);

// 查询用户名为name，或者密码为xxx并且年龄大于30的一条记录，
// 返回一条找到的User记录，若没有符合条件的记录返回null
return select()
        .whereEq(columnName, name)
        .or()
        .eq(columnPassword, encryptPwd("xxx"))
        .gt(columnAge, 30)
        .one();
```

```java
// data.DBUpgrader.java
// 由LibergCoder维护的，数据库/表结构升级类
private void upgradeTo2(Statement stat) throws SQLException {
    // 在user表的id列后添加一个bigint类型的_company_id列
    alter("user")
        .addColumn("_company_id", dbImpl.typeLong(), "id")
        .exec(stat);
}
```

更多示例，请参见[附录I SQL风格的代码](#附录I-SQL风格的代码)。

### BaseDao中提供了丰富的`CRUD`系列方法，**彻底甩掉`xml`**

- save/update/getEq/getEqs/delete同步操作

- asyncSave/asyncUpdate异步操作

- batchSave/batchUpdate/batchSaveOrUpdate批量操作

- 编程式事务

  ```java
  transaction(this::doSth());
  String result = transaction(()->{doSth(); return "result";});
  ```



BaseDao中的**所有操作不会用到任何反射代码**。

`LibergCoder`生成的`XxxDaoImpl`继承自`BaseDao<T>`，并自动创建了表字段对应的所有列`columnXxx`，`CRUD`方便快捷。

总之，`Liberg + LibergCoder ==  零反射的ORM框架 + SpringBoot代码自动化工具`。

## 快速开始

1. 在`Idea`中创建`Spring Initializr`项目，仅仅需要勾选`Spring Web`依赖。

2. 下载本地安装Idea插件[LibergCoder插件，当前最新版本2.0.0](https://gitee.com/liberg-cn/LibergCoder/blob/master/LibergCoder.jar)，安装完成，重启Idea，菜单栏末尾多出一个`LibergCoder`菜单。

3. 下载本项目源代码，本地`Maven install`，将`liberg.jar`包安装到本地`Maven仓库`中。

4. **打开SpringBoot项目的启动类（带@SpringBootApplication注解），然后点击Idea的LibergCoder菜单，执行`Initialize...`完成项目代码的初始化**。

   **`Initialize`执行完成之后，整个项目会被IDEA重新载入**。

   `Initialize`操作会执行如下动作：

   - 在项目目录下创建`LibergCoder`的配置文件`LibergConfig.properties`。

   - 修改`pom.xml`，增加`mysql-connector-java`和`fastjson`依赖，如果缺失的话。

   - 修改`resources`下的`application.properties`文件，增加一些数据库和`application.name, server.port`等默认配置。

   - 创建`data.dao、data.entity、data.type、service.interfaces、controller.api、misc`等package。

   - 创建`misc.InitializeRunner`类，该类完成数据库的初始化（建库、建表、初始化数据等等）。

   - 创建`misc.ResponseBodyProcessor`类，完成`fastjson`序列化，并且加入跨域和`JSONP`的支持。

   - 创建`data.DBConfig`类，用于从`application.properties`文件加载数据库的配置。

   - 创建`data.DBImpl`类，这个类是数据库建表、数据库版本升级，以及数据初始化的入口，由LibergCoder插件维护。

   - 创建`data.DBInitializer`类，开发者在这里增加数据初始化的代码，如果有需要的话。

   - 创建`data.DBUpgrader`类，由LibergCoder维护的数据库版本自动升级实现类。一般情况下不需要额外关心。

     `Initialize`一般仅需要执行一次，如果某些支撑文件缺失，可以执行`Initialize`重新创建出来。

     

5. **创建一个entity类，比如data/entity/User.java**

   ```java
   public class User {
       // 实体数据缓存到id列的LRU容器中，缓存容量为10万
       @cache(cap=TenThousands.X10)
       public long id; //实体类要有一个long类型的id作为数据表的自增主键
       
       //isIndex为true表示需要给此列创建索引
       // companyId+name可以唯一确定一条记录，这里定义为一个缓存组
       @dbmap(isIndex=true) @cache(groupCap=TenThousands.X10, group="g1")
       public long companyId;
       @dbmap(isIndex=true) @dbmap(isIndex=true) @cache(seq=1, group="g1")
       public String name;// 映射到varchar(255)
       
       @dbmap(length=31) // 映射到varchar(31)
       public String password;
       public byte age; // byte映射为TINYINT字段
       public long roleId;// long映射为BIGINT字段
       public long createTime;
       
       //isMap为false的成员不映射到数据表的字段
       @dbmap(isMap=false)  
       public Role role;
       @dbmap(isMap=false)  //可用于额外填充附加信息返回给客户端
       public UserDetail userDetail;
   }
   ```

   在IDEA中打开`User.java`文件后，执行`LibergCoder--Build entity/interface...`，`LibergCoder`插件会解析此实体类，生成或修改相关的代码文件。比如：

   - 自动创建`data.dao.impl.UserDaoImpl`类，用于支持对`user`表的`CRUD`操作。

     ```java
     public class UserDaoImpl extends BaseDao<User> {
         public static final String TABLE_NAME = "user";
         public static final CachedColumn<User, Long> columnId;
         public static final CachedColumn<User, Long> columnCompanyId;
         public static final CachedColumn<User, String> columnName;
         public static final Column<User, String> columnPassword;
         public static final Column<User, Byte> columnAge;
         public static final Column<User, Long> columnRoleId;
         public static final Column<User, Long> columnCreateTime;
         public static final CachedColumnPair<User, Long, String> $companyId$name;
         // 生成的其他方法
     }
     ```
     
   - 自动创建继承`UserDaoImpl`的`data.dao.UserDao`类，开发者在此文件中实现自己的逻辑。

   - 自动修改`data.DBImpl`加入`user`表的建表逻辑。

   - 自动修改`User.java`文件，给每个字段增加`fastjson`提供的`@JSONField`注解。

6. **创建一个接口，比如service/interfaces/IUserService.java**

   ```java
   public interface IUserService {
       //接口中的方法须冠以public修饰符，否则当前版本LibergCoder不能识别
       public Response register(String userName, String password) throws OperatorException;
       public Response login(String userName, String password) throws OperatorException;
       public User getByName(String userName) throws OperatorException;
       public boolean nameExists(String userName) throws OperatorException;
   }
   ```

   在IDEA中打开`IUserService.java`文件后，执行`LibergCoder--Build entity/interface...`，`LibergCoder`插件会解析此`interface`，生成或修改相关的代码文件。比如：

   - 自动创建`controller.api.UserController`类，用于承接`HTTP`请求。

     ```java
     @RestController
     @RequestMapping("/api/user")
     public class UserController {
         private final UserService service;
         private static final Logger logger = LoggerFactory.getLogger(UserController.class);
         // 省去一些模板代码
         
         @PostMapping("/register")
         public Response register(@RequestParam("un") String userName, 
                                  @RequestParam("p") String password) throws OperatorException {
             try {
                 return service.register(userName, password);
             } catch (OperatorException e) {
                 logger.error("Error: [" + e.code() + "]" + e.desc(), e);
                 return Response.fail(e);
             }
         }
         
         @PostMapping("/getByName")
         public Response getByName(@RequestParam("un") String userName) throws OperatorException {
             try {
                 return Response.ok(service.getByName(userName));
             } catch (OperatorException e) {
                 logger.error("Error: [" + e.code() + "]" + e.desc(), e);
                 return Response.fail(e);
             }
         }
         //...
     }
     ```
     
     
     
   - 创建`service.UserService`类，这是`IUserService`的实现类，开发者在此编写代码，完成接口需要实现的具体业务逻辑。
   
   - 在`resources/static/api-doc`目录下创建Markdown格式的接口文档。
   
7. 运行`SpringBoot`项目，看看发生了什么。



如果遇到了麻烦，请参考附录`liberg-demo`示例项目：[附录II 相关项目(gitee)](#附录II-相关项目(gitee))。



## Liberg+LibergCoder开发体验

把复杂留给框架和工具，让开发顺畅丝滑。重复的事情，无须重复做。

1. Liberg自动初始化Web项目、添加maven依赖，甚至连数据库都提供了默认配置。
2. Liberg由工具维护数据库脚本，包括建库、建表、DDL升级等等。一切变得简单高效，一切尽在java程序员的掌控之中。
3. 所有代码可见、可控，封装良好。
4. 基于数据和接口来构建整个系统，有了entity（***保存什么数据***）和interface（***对外提供什么功能***），其他都有了。
5. 基于SpringBoot，遵循SpringMVC规范，天然拥有强大的框架整合能力和开发生态。
6. 基于java接口（interface），快速建立Controller、Service，自动生成接口定义文档（供前端和测试使用）。



如果您也认可`Liberg`这种**零反射ORM+插件生成支撑代码**的开发方式，欢迎加入贡献代码，一起交流进步。

---



## 附录I SQL风格的代码

`SQL`风格的查询、更新代码演示。

- 单条件查询

```java
// XxxDao文件中，单条件查询，支持eq(=)，ne(<>)，gt(>)，ge(>=)，lt(<)，le(<=)，like
// 查询满足条件的单条记录
Xxx record = getEq(columnStatus, 0);
// 查询满足条件的记录，最多返回50条
List<Xxx> records = getEqs(columnStatus, 0, 50);
```

- 查询单列

```java
// 只查询用户名这一列，按id列降序，返回一个List<String>
return select(columnName)
        .whereGt(columnId, 0)
        .eq(columnNickName, nickName)
        .desc(columnId)
        .all(50);
```

- 查询部分列（`Segment`）

```java
//只查询id和name两列，返回Segment<User>
Segment<User> userSegment = select(columnId, columnName)
                .whereEq(columnAge, 18)
                .one();
long id = userSegment.get(columnId);
String name = userSegment.get(columnName);
```

- 分页查询

```java
// 分页查询
return select()
    .whereEq(columnIndustryId, industryId)
    .eq(columnSceneId, sceneId)
    .page(pageNum, pageSize);
```

- 预编译查询

```java
// prepare方式的预编译查询：这里演示只查询用户名
final PreparedSelectWhere<String> psw = prepareSelect(columnName)
    .whereGe$(columnId)
    .eq$(columnAge);

try (final PreparedSelectExecutor<String> prepare = psw.prepare()) {
    // 填充条件参数
    prepare.setParameter(columnId, 1);
    prepare.setParameter(columnAge, 10);
    // 第一次查询
    prepare.one();
    
    // 填充条件参数
    prepare.setParameter(columnId, 2);
    prepare.setParameter(columnAge, 30);
    // 再次查询
    prepare.one();
}
```

- 更新

```java
// 演示更新操作：更新用户名、密码、增大年龄，通过id相等条件
update().set(columnName, newName)
        .set(columnPassword, newPassword)
        .increment(columnAge, ageIncrement)
        .whereEq(columnId, id)
        .execute();
```

- 多表join

```java
UserDao userDao = UserDao.self();
RoleDao roleDao = RoleDao.self();
JoinQuery jq = JoinQuery.basedOn(userDao)
    .innerJoin(roleDao).eq(userDao.columnRoleId, roleDao.columnId)
    .where(userDao).eq(userDao.columnName, "张三")
    .asc(userDao.columnId).limit(10);
final JoinResult all = jq.all(50);
```



## 附录II 相关项目

### gitee

- Liberg项目：[https://gitee.com/liberg-cn/Liberg](https://gitee.com/liberg-cn/liberg-demo)

- liberg-demo:  `Liberg+LibergCoder`的`Spring Boot`演示项目

  https://gitee.com/liberg-cn/liberg-demo
  
- LibergCoder: Idea插件

  https://gitee.com/liberg-cn/LibergCoder

### github

- Liberg项目：[https://github.com/liberg-cn/Liberg](https://github.com/liberg-cn/liberg-demo)

- liberg-demo:  `Liberg+LibergCoder`的`Spring Boot`演示项目

  https://github.com/liberg-cn/liberg-demo

- LibergCoder: Idea插件

  https://github.com/liberg-cn/LibergCoder

## 附录III 升级日志

#### 2022/04/30  V1.3.2 --> V2.0.0

- 将解析实体类生成的DAO相关代码，单独拎出来，隔离到`dao.imp.XxxDaoImpl.java`文件中，完全由`LibergCoder`维护。开发者只需要关心`dao.XxxDao.java`文件。
- 支持自动在**单列**和**多列组合**上建立查询缓存(LRU淘汰）。如果命中缓存，就可以**省掉从数据库读取数据、然后根据数据构建实体对象**的过程，大大提升查询的速度。

## 交流

>  Liberg ORM，高效、简洁、专注于Web。
>
>  QQ交流群：126193402
>
>  加微信拉群：linysuccess

多谢Star/Fork支持🤝🤝🤝