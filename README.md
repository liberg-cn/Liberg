# Liberg
    A high-performance and lightweight suites for spring-boot-based web development.

`Liberg`为从零基础小白到中高级`Java Web`开发者提供一站式的极速开发体验和解决方案。

包含`Liberg`库（`Jar`）和`LibergCoder`（`Idea插件Jar`）两部分。

### 3大特点

1. `Liberg`是一个真正实现“**零反射**”的Java `ORM`框架。

   **反射**赋予了Java动态编程的能力。通过反射可以在运行时通过类名称（全限定类名）动态地创建对象、获取对象的属性和方法、为对象赋予新的属性值、调用对象中的方法等等。利用Java的反射机制，动态地创建对象、为对象属性赋值，就可以很容易地实现将数据表中的一条记录转换为一个Java实体类的对象。可以肯定的是，几乎所有`ORM`（Object Relational Mapping）框架就就是这个套路。差别不过是在反射的基础上做了多大程度的优化。

   反射的强大特性在代码自动化和框架场景中大有用武之地，但也有一个缺点：那就是——**慢**。

   利用`LibergCoder`生成代码，`Liberg`真正做到了实体关系映射上的“**零反射**”。因此，可以说`Liberg`是一个极轻量级的`ORM`框架。

2. 极速开发：创建最少的文件、写最少的代码。

   很多需要手动创建的代码、配置文件，`LibergCoder`自动完成创建和维护。在`LibergCoder`插件的加持下，实现了完全自动化的`Spring Boot Web`项目初始化，包括`yml`配置创建、`pom.xml`依赖添加、数据库创建、数据表创建、表结构升级等等。

   `LibergCoder`会创建和维护必要的支撑代码，开发者只写业务：**定义实体类、定义`API`接口、编写业务方法的具体实现**。

   **让开发者创建最少的文件、写最少的代码**。这是`Liberg`项目的不懈追求。

3. 最接近`SQL`的代码风格。这里指数据查询和更新的一些代码。

   先来看一段演示代码：

   ```java
   // UserDao.java
   // 演示查询操作：查询用户名为name，或者密码为xxx并且年龄大于30的一条记录，
   // 返回一个User对象或null
   return select()
           .whereEq(columnName, name)
           .or()
           .eq(columnPassword, "xxx")
           .gt(columnAge, 30)
           .one();
   ```
   
   更多示例，请参见**附录I MySQL风格的代码**。

总之，`Liberg` + `LibergCoder` `==` 零反射的`ORM`框架 `+` 代码自动化工具。



**特别说明：** 由于`Github`在国内访问太慢，相关文档和资源优先在网站[liberg.cn](https://liberg.cn/)进行更新。



### Web系统设计理念

从外部视角来看，**Web系统=数据+功能**。`Liberg`就是围绕“**数据**”和“**接口**”来设计和构架整个Web系统。

**数据**：由`data.entity`包下的实体类进行承载，映射到数据库中的表。entity的一个字段映射为数据库表的一列，通过注解设定为不映射成员除外。

**接口**：由`service.interfaces`包下的接口声明类定义，映射为`controller.api.XxxControllor`和`service.XxxService`。

其中，`XxxController`是承接`http`请求的入口。每一个接口方法，映射为一个接口`uri`。

`XXXService`是实现业务逻辑、编写逻辑代码的地方。

因此，基于`Liberg`来开发`Web`应用，开发者只需要定义好实体类、对外提供`API`的接口类，然后编写具体业务逻辑代码即可。其他支撑性的胶水代码由`LibergCoder`插件进行自动的创建和维护。



### 如何开始

1. 在Idea中创建`Spring Initializr`项目，仅仅需要勾选`Spring Web`依赖。

2. 安装Idea插件[LibergCoder.jar](https://github.com/liberg-cn/LibergCoder/blob/master/LibergCoder.jar)，安装完成，重启Idea，菜单栏末尾多出一个`LibergCoder`菜单。

3. 下载本项目源代码，本地`Maven install`，将`liberg`jar包安装到本地Maven仓库中。也可以在项目中手动引入jar包：[liberg-1.3.0.jar](https://github.com/liberg-cn/Liberg/blob/master/target/liberg-1.3.0.jar)，引入方式见文末备注。

4. **打开SpringBoot项目的启动类（判断标准：带@SpringBootApplication注解），然后执行点击LibergCoder菜单，执行`Initialize...`完成项目代码的初始化**。

   **`Initialize`执行完成之后，整个项目会被IDEA重新载入**。

   `Initialize`操作会执行如下动作：

   - 在项目目录下创建LibergCoder的配置文件`LibergConfig.properties`。

   - 修改`pom.xml`，增加mysql和fastjson依赖，如果缺失的话。

   - 修改`resources`下的`application.properties`文件，增加一些数据库和`application.name`等默认配置。

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
       public long id; //每个实体类都要有一个long类型的id作为数据表的自增主键
       @dbmap(isIndex=true) //isIndex为true表示需要给此列创建索引
       public String name;
       public String password;
       public long roleId;
       public long createTime;
       @dbmap(isMap=false)  //isMap为false表示不映射到数据库的字段
       public Role role;
   }
   ```

   在IDEA中打开`User.java`文件后，执行`LibergCoder--Build entity/interface...`，`LibergCoder`插件会解析此实体类，生成或修改相关的代码文件。比如：

   - 创建`data.dao.UserDao`类，用于支持对user表的CRUD操作。
   - 修改`data.DBImpl`加入user表的建表逻辑。
   - 修改`User.java`文件，给每个字段增加`@JSONField`注解。

6. **创建一个接口类，比如service/interfaces/IUserService.java**

   ```java
   public interface IUserService {
       //接口中的方法须冠以public修饰符，否则当前版本LibergCoder不能识别
       public Response register(String userName, String password) throws OperatorException;
       public Response login(String userName, String password) throws OperatorException;
       public User getByName(String userName) throws OperatorException;
       public boolean nameExists(String userName) throws OperatorException;
   }
   ```

   在IDEA中打开`IUserService.java`文件后，执行`LibergCoder--Build entity/interface...`，LibergCoder插件会解析此interface，生成或修改相关的代码文件。比如：

   - 创建`controller.api.UserController`类，用于承接http请求。
   - 创建`service.UserService`类，这是`IUserService`的实现类，开发者在此编写代码，完成接口需要实现的具体业务逻辑。
   - 在`resources/static/api-doc`目录下创建Markdown格式的接口文档。

7. 运行`SpringBoot`项目，看看发生了什么。



如果遇到了麻烦，请参考示例项目：https://github.com/liberg-cn/liberg-demo



### Liberg+LibergCoder开发体验

重复的事情，无须重复做。

”偷懒“是驱动人类进步的动力？？！。

1. Liberg自动初始化Web项目、添加maven依赖，甚至连数据库都提供了默认配置。
2. Liberg由工具维护数据库脚本，包括建库、建表、DDL升级等等。一切变得简单高效，一切尽在java程序员的掌控之中。
3. 基于数据和接口来构建整个系统，有了entity（*保存什么数据*）和interface（*对外提供什么功能*），其他都有了。
4. 基于SpringBoot，遵循SpringMVC规范，天然拥有强大的框架整合能力和开发生态。
5. 基于java接口（interface），快速建立Controller、Service，自动生成接口定义文档（供前端和测试使用）。



### 备注

> 1. `Liberg`默认依赖`mysql`和`fastjson`。
>
> 2. `Liberg` JAR还未提交Maven中央仓库。目前可以下载最新版[liberg-1.3.0.jar](https://github.com/liberg-cn/Liberg/blob/master/target/liberg-1.3.0.jar)，在项目下新增一个lib目录，放入jar包，然后在`pom.xml`中引入：
>
>    ```xml
>     <dependency>
>        <groupId>cn.liberg</groupId>
>        <artifactId>liberg</artifactId>
>        <version>1.2.0</version>
>        <scope>system</scope>
>        <systemPath>${project.basedir}/lib/liberg-1.2.0.jar</systemPath>
>    </dependency>
>    ```
> 
>    或者如前文所述，下载`Liberg`源码，本地`Maven instal`后直接进行引入。

----

如果您也认可`Liberg`这种**零反射ORM+插件生成支撑代码**的开发方式，欢迎加入贡献代码，一起交流进步。



---



### 附录I MySQL风格的代码

`MySQL`风格的查询、更新代码演示：

```java
// 只查询用户名这一列，按id列降序，返回一个List<String>对象
return select(columnName)
        .whereGt(columnId, 0)
        .eq(columnNickName, nickName)
        .desc(columnId)
        .all();

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

// 演示更新操作：更新用户名、密码、增大年龄，通过id相等条件
update().set(columnName, newName)
        .set(columnPassword, newPassword)
        .increment(columnAge, ageIncrement)
        .whereEq(columnId, id)
        .execute();
```

### 附录II 相关项目

- Liberg项目：[https://github.com/liberg-cn/Liberg](https://github.com/liberg-cn/liberg-demo)

- liberg-demo:  Liberg演示项目

  https://github.com/liberg-cn/liberg-demo

- LibergCoder: Idea插件(已支持idea2020)

  https://github.com/liberg-cn/LibergCoder

### 交流

>  *Liberg，让Web开发更简单！*
>
>  QQ交流群（推荐）：126193402
>

感谢您的阅读。