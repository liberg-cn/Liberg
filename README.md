# Liberg
> A high-performance and lightweight suites for spring-boot-based web development.
> **Liberg**为从零基础小白到中高级Java Web开发者提供一站式的极速开发体验和解决方案。
> 特别适合快速迭代的中小项目。

包含`Liberg`库（`普通Jar`）和`LibergCoder`（`Idea插件Jar`）两部分。

- **Liberg**：轻量`ORM`核心，除了`mysql-connector-java`和`logback`外，无其他依赖。可用于任何Java服务端程序开发。
- **LibergCoder**：`Spring Boot`代码生成插件，解析**实体**类和**接口**类生成模板代码，加速项目开发。

源代码仓库地址：

- gitee: [https://gitee.com/liberg-cn/Liberg](https://gitee.com/liberg-cn/Liberg)

- github: [https://github.com/liberg-cn/Liberg](https://github.com/liberg-cn/Liberg)

### 特点

1. `Liberg`是一个真正实现“**零反射**”的`Java ORM`框架。

   **反射**赋予了Java动态编程的能力。通过反射可以在运行时通过类名称（全限定类名）动态地创建对象、获取对象的属性和方法、为对象赋予新的属性值、调用对象中的方法等等。利用Java的反射机制，动态地创建对象、为对象属性赋值，就可以很容易地实现将数据表中的一条记录转换为一个Java实体类的对象。可以肯定的是，几乎所有`ORM`（Object Relational Mapping）框架就就是这个套路。差别不过是在反射的基础上做了多大程度的优化。

   反射的强大特性在代码自动化和框架场景中大有用武之地，但也有一个缺点：那就是——**慢**。

   利用`LibergCoder`生成代码，`Liberg`真正做到了实体关系映射上的“**零反射**”。也就是说，`Liberg`是一个极轻量级的`ORM`框架。

2. 便捷的**双向映射**。

   `LibergCoder`插件读取Java实体类，自动建表、自动完成`DDL`升级，生成`XxxDao`数据访问类。`Liberg ORM`管理数据库连接池，完成数据行到实体类对象的转换。

   已支持的映射类型如下：

   | 序号 | Java实体字段类型                     | 数据表字段类型 |
   | ---- | ------------------------------------ | -------------- |
   | 1    | byte                                 | TINYINT        |
   | 2    | int                                  | INT            |
   | 3    | long                                 | BIGINT         |
   | 4    | @dbmap(length=x) String，其中x<=4095 | VARCHAR(x)     |
   | 5    | @dbmap(length=y) String，其中y>=4096 | TEXT           |

   > 浮点数请使用long/String类型进行存储。

3. 极速开发：创建最少的文件、手敲最少的代码。

   很多需要手动创建的代码、配置文件，`LibergCoder`自动完成创建和维护。在`LibergCoder`插件的加持下，实现了完全自动化的`Spring Boot Web`项目初始化，包括`yml`配置创建、`pom.xml`依赖添加、数据库创建、数据表创建、表结构升级等等。

   `LibergCoder`会创建和维护必要的支撑代码，开发者只写业务：**定义实体类、定义`API`接口、编写业务方法的具体实现**。

   **让开发者创建最少的文件、写最少的代码**。这是`Liberg`项目诞生的初衷。

4. 最接近`SQL DQL/DML `风格的代码。比如下面这段演示代码。

   ```java
   // UserDao.java
   // 演示查询操作：查询用户名为name，或者密码为xxx并且年龄大于30的一条记录，
   // 返回一条找到的User记录，若没有符合条件的记录返回null
   return select()
           .whereEq(columnName, name)
           .or()
           .eq(columnPassword, "xxx")
           .gt(columnAge, 30)
           .one();
   ```

   更多示例，请参见[附录I SQL风格的代码](#附录I-SQL风格的代码)。

5. `BaseDao`中提供了丰富的`CRUD`系列方法。**彻底甩掉`xml`**。

   - save/update/getXxx/delete同步操作

   - asyncSave/asyncUpdate异步操作

   - batchSave/batchUpdate/batchSaveOrUpdate批量操作

   - 编程式事务

     ```java
     transaction(this::doSth());
     String result = transaction(()->{doSth(); return "result";});
     ```

     

   `LibergCoder`生成的`XxxDao`继承自`BaseDao<T>`，并自动创建了表字段对应的所有`Column<T> columnXxx`，CRUD方便快捷。

   

总之，`Liberg` + `LibergCoder` `==` 零反射的`ORM`框架 `+` SpringBoot代码自动化工具。



### 设计理念

从外部视角来看，**Web系统=数据+功能**。`Liberg`就是围绕“**数据**”和“**接口**”来设计和构架整个Web系统。

**数据**：由`data.entity`包下的实体类进行承载，映射到数据库中的表。entity的一个字段映射为数据库表的一列，通过注解设定为不映射成员除外。

**接口**：由`service.interfaces`包下的接口声明类定义，映射为`controller.api.XxxControllor`和`service.XxxService`。

其中，`XxxController`是承接`http`请求的入口。每一个接口方法，映射为一个接口`uri`。

`XXXService`是实现业务逻辑、编写逻辑代码的地方。

因此，基于`Liberg`来开发`Web`应用，开发者只需要定义好实体类、对外提供`API`的接口类，然后编写具体业务逻辑代码即可。其他支撑性的胶水代码由`LibergCoder`插件进行自动的创建和维护。



### 快速开始

1. 在Idea中创建`Spring Initializr`项目，仅仅需要勾选`Spring Web`依赖。

2. 下载本地安装Idea插件[LibergCoder插件，当前最新版本1.3.2](https://gitee.com/liberg-cn/Liberg/blob/master/target/liberg-1.3.2.jar)，安装完成，重启Idea，菜单栏末尾多出一个`LibergCoder`菜单。

3. 下载本项目源代码，本地`Maven install`，将`liberg.jar`包安装到本地Maven仓库中。

4. **打开SpringBoot项目的启动类（带@SpringBootApplication注解），然后点击Idea的LibergCoder菜单，执行`Initialize...`完成项目代码的初始化**。

   **`Initialize`执行完成之后，整个项目会被IDEA重新载入**。

   `Initialize`操作会执行如下动作：

   - 在项目目录下创建`LibergCoder`的配置文件`LibergConfig.properties`。

   - 修改`pom.xml`，增加`mysql-connector-java`和`fastjson`依赖，如果缺失的话。

   - 修改`resources`下的`application.properties`文件，增加一些数据库和`application.name, server.port`等默认配置。

   - 创建`data.dao、data.entity、data.type、service.interfaces、controller.api、misc`等package。

   - 创建`misc.InitializeRunner`类，该类完成数据库的初始化（建库、建表、初始化数据等等）。

   - 创建`misc.ResponseBodyProcessor`类，完成fastjson序列化，并且加入跨域和JSONP的支持。

   - 创建`data.DBConfig`类，用于从`application.properties`文件加载数据库的配置。

   - 创建`data.DBImpl`类，这个类是数据库建表、数据库版本升级，以及数据初始化的入口，由LibergCoder插件维护。

   - 创建`data.DBInitializer`类，开发者在这里增加数据初始化的代码。

   - 创建`data.DBUpgrader`类，由LibergCoder维护的数据库版本自动升级实现类。一般情况下不需要额外关心。需要将多个升级版本合并为一个升级版本时，可以手动编辑删掉一些版本的`upgradeTo`方法，记得一并修改DBImpl中的数据库当前版本。

     `Initialize`一般仅需要执行一次，如果某些支撑文件缺失，可以执行`Initialize`重新创建出来。

     

5. **创建一个entity类，比如data/entity/User.java**

   ```java
   public class User {
       public long id; //实体类要有一个long类型的id作为数据表的自增主键
       @dbmap(isIndex=true) //isIndex为true表示需要给此列创建索引
       public String name;// 映射到varchar(255)
       @dbmap(length=31) // 映射到varchar(31)
       public String password;
       public byte age; // byte映射为TINYINT字段
       public long roleId;// long映射为BIGINT字段
       public long createTime;
       @dbmap(isMap=false)  //isMap为false表示不映射到数据表的字段
       public Role role;
       @dbmap(isMap=false)  //可用于额外填充附加信息返回给客户端
       public UserDetail userDetail;
   }
   ```

   在IDEA中打开`User.java`文件后，执行`LibergCoder--Build entity/interface...`，`LibergCoder`插件会解析此实体类，生成或修改相关的代码文件。比如：

   - 自动创建`data.dao.UserDao`类，用于支持对`user`表的`CRUD`操作。

     ```java
     public class UserDao extends BaseDao<User> {
         public static final String TABLE_NAME = "user";
         public static final Column<String> columnName = new StringColumn("name", "n");
         public static final Column<String> columnPassword = new StringColumn("password", "p");
         public static final Column<Byte> columnAge = new StringColumn("age", "a");
         public static final Column<Long> columnRoleId = new LongColumn("roleId",  "ri");
         public static final Column<Long> columnCreateTime = new LongColumn("createTime", "ct");
         
         // 此处增加自己的业务方法
     }
     ```
     
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

   - 创建`controller.api.UserController`类，用于承接`HTTP`请求。

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



### Liberg+LibergCoder开发体验

重复的事情，无须重复做。”偷懒“是人的天性。

1. Liberg自动初始化Web项目、添加maven依赖，甚至连数据库都提供了默认配置。
2. Liberg由工具维护数据库脚本，包括建库、建表、DDL升级等等。一切变得简单高效，一切尽在java程序员的掌控之中。
3. 基于数据和接口来构建整个系统，有了entity（***保存什么数据***）和interface（***对外提供什么功能***），其他都有了。
4. 基于SpringBoot，遵循SpringMVC规范，天然拥有强大的框架整合能力和开发生态。
5. 基于java接口（interface），快速建立Controller、Service，自动生成接口定义文档（供前端和测试使用）。



如果您也认可`Liberg`这种**零反射ORM+插件生成支撑代码**的开发方式，欢迎加入贡献代码，一起交流进步。

---



### 附录I SQL风格的代码

`SQL`风格的查询、更新代码演示。

- 单条件查询

```java
// XxxDao文件中，单条件查询，支持eq(=)，ne(<>)，gt(>)，ge(>=)，lt(<)，le(<=)，like
// 查询满足条件的单条记录
Xxx record = getEq(columnStatus, 0);
// 查询满足条件的所有记录
List<Xxx> records = getEqs(columnStatus, 0);
```

- 查询单列

```java
// 只查询用户名这一列，按id列降序，返回一个List<String>
return select(columnName)
        .whereGt(columnId, 0)
        .eq(columnNickName, nickName)
        .desc(columnId)
        .all();
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

- 多表join（完善中...）

```java
UserDao userDao = UserDao.self();
RoleDao roleDao = RoleDao.self();
JoinQuery jq = JoinQuery.basedOn(userDao)
    .innerJoin(roleDao).eq(userDao.columnRoleId, roleDao.columnId)
    .where(userDao).eq(userDao.columnName, "张三")
    .asc(userDao.columnId).limit(10);
final JoinResult all = jq.all();
```



### 附录II 相关项目(gitee)

- Liberg项目：[https://gitee.com/liberg-cn/Liberg](https://gitee.com/liberg-cn/liberg-demo)

- liberg-demo:  `Liberg+LibergCoder`的`Spring Boot`演示项目

  https://gitee.com/liberg-cn/liberg-demo
- LibergCoder: Idea插件

  https://gitee.com/liberg-cn/LibergCoder

### 附录III 相关项目(github)

---

- Liberg项目：[https://github.com/liberg-cn/Liberg](https://github.com/liberg-cn/liberg-demo)

- liberg-demo:  `Liberg+LibergCoder`的`Spring Boot`演示项目

  https://github.com/liberg-cn/liberg-demo

- LibergCoder: Idea插件

  https://github.com/liberg-cn/LibergCoder

### 交流

>  Liberg，简单、高效、专注于Web。
>
>  QQ交流群：126193402
>

感谢您的阅读。