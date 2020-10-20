# Liberg
    A high-performance and lightweight suites for spring-boot-based web development.



Liberg为从零基础小白到中高级Java Web开发者提供一站式的极速解决方案。由Liberg库（jar）和LibergCoder插件（Idea版）构成。

> Liberg库，提供了一个极轻量级的`ORM`（Object Relational Mapping）、以及一些Web项目支撑代码。 
>
> 为什么如此轻量级？
>
> 是因为借助LibergCoder自动生成代码，实现了**非反射的ORM**。



**特别说明：**由于Github在国内访问太慢，相关文档和资源优先在网站[liberg.cn](http://liberg.cn/)进行更新。



### Web系统设计理念

Liberg围绕“**数据**”和“**接口**”来设计和开发整个Web系统。

**数据**：由`data.entity`包下的实体类进行承载，映射到数据库中的表。entity的一个字段映射为数据库表的一列。

**接口**：由`service.interfaces`包下的接口声明类定义，映射为controller.api.XxxControllor，而XxxController是承接http请求的入口。每一个接口方法，映射为一个接口uri。



### 如何开始

1. 在Idea中创建`Spring Initializr`项目，仅仅需要勾选Spring Web依赖。

2. 安装Idea插件[LibergCoder.jar](https://github.com/liberg-cn/LibergCoder/blob/master/LibergCoder.jar)，安装完成，重启Idea，菜单栏末尾多出一个LibergCoder菜单。

3. 项目中手动引入jar包：[liberg-1.2.0.jar](https://github.com/liberg-cn/Liberg/blob/master/target/liberg-1.2.0.jar)，引入方式见文末备注。

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
       public long id; //每个实体类都要有一个long类型的id作为数据表的主键
       @dbmap(isIndex=true) //isIndex为true表示需要给此列创建索引
       public String name;
       public String password;
       public long roleId;
       public long createTime;
       @dbmap(isMap=false)  //isMap为false表示不映射到数据库的字段
       public Role role;
   }
   ```

   在IDEA中打开`User.java`文件后，执行`LibergCoder--Build entity/interface...`，LibergCoder插件会解析此实体类，生成或修改相关的代码文件。比如：

   - 创建`data.dao.UserDao`类，用于支持对user表的CRUD操作。
   - 修改`data.DBImpl`加入user表的建表逻辑。
   - 修改`User.java`文件，给每个字段增加@JSONField注解。

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
   - 创建`service.UserService`类，这是IUserService的实现类，开发者在此编写代码，完成接口需要实现的具体业务逻辑。
   - 在`resources/static/api-doc`目录下创建Markdown格式的接口文档。

7. 运行SpringBoot项目，看看发生了什么。



如果遇到了麻烦，请参考示例项目：https://github.com/liberg-cn/liberg-demo



### 使用Liberg+LibergCoder的开发体验

重复的事情，无须重复做。

”偷懒“是程序员的美德，Liberg或许能帮助您把这种“美德”尽情发挥。

1. Liberg自动初始化Web项目、添加maven依赖，甚至连数据库都提供了默认配置。
2. Liberg由工具维护数据库脚本，包括建库、建表、DDL升级等等。一切变得简单高效，一切尽在java程序员的掌控之中。
3. 基于数据和接口来构建整个系统，有了entity（*我要保存什么数据*）和interface（*我要对外提供什么功能*），其他都有了。
4. 基于SpringBoot，遵循SpringMVC规范，天然拥有强大的框架整合能力和开发生态。
5. 基于java接口（interface），快速建立Controller、Service，自动生成接口定义文档（供前端和测试使用）。



> 备注：
>
> 1. Liberg默认依赖mysql和fastjson。
>
> 2. Liberg JAR还未提交Maven中央仓库。目前可以下载[liberg-1.2.0.jar](https://github.com/liberg-cn/Liberg/blob/master/target/liberg-1.2.0.jar)，在项目下新增一个lib目录，放入jar包，然后在pom.xml中引入：
>
>    ```xml
>    <dependency>
>        <groupId>cn.liberg</groupId>
>        <artifactId>liberg</artifactId>
>        <version>1.2.0</version>
>        <scope>system</scope>
>        <systemPath>${project.basedir}/lib/liberg-1.2.0.jar</systemPath>
>    </dependency>
>    ```
>
>    或者下载Liberg源码，本地`Maven instal`后直接进行引入。

----

如果您也认可Liberg这种**非反射ORM+插件生成支撑代码**的开发方式，欢迎加入贡献代码，一起交流进步。



### 相关项目

- Liberg项目：[https://github.com/liberg-cn/Liberg](https://github.com/liberg-cn/liberg-demo)

- liberg-demo:  Liberg演示项目

  https://github.com/liberg-cn/liberg-demo

- LibergCoder: Idea插件(已支持idea2020)

  https://github.com/liberg-cn/LibergCoder



>  *Liberg，让Web开发更简单！*
>
>  QQ交流群（推荐）：126193402
>



感谢您的阅读。