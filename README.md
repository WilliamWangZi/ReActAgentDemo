# ReActAgentDemo

这是一个基于Spring Boot框架的示例项目。

## 项目结构

```
ReActAgentDemo/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/reactagentdemo/
│   │   │       ├── ReActAgentDemoApplication.java
│   │   │       └── controller/
│   │   │           └── HelloController.java
│   │   └── resources/
│   │       └── application.properties
├── pom.xml
└── README.md
```

## 技术栈

- Java 17
- Spring Boot 3.1.0
- Maven

## 运行项目

1. 使用Maven构建项目：
   ```
   mvn clean install
   ```

2. 运行应用程序：
   ```
   mvn spring-boot:run
   ```

3. 访问应用：
   打开浏览器访问 http://localhost:8080/hello

## API接口

- `GET /hello` - 返回问候语

## 配置

应用默认运行在端口8080，可以在 `application.properties` 文件中修改。