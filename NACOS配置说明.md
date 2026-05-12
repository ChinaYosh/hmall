# Nacos共享配置说明

## 问题描述
项目重构后，所有共享配置（数据库、日志、Swagger、Seata）都已移至Nacos配置中心。但服务启动失败是因为Nacos中缺少这些共享配置。

## 需要在Nacos中创建的配置文件

### 1. shared-jdbc.yml (必须配置)
```yaml
spring:
  datasource:
    url: jdbc:mysql://${hm.db.host}:${hm.db.port:33099}/${hm.db.database}?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&serverTimezone=Asia/Shanghai
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: ${hm.db.username:root}
    password: ${hm.db.pw}

mybatis-plus:
  configuration:
    default-enum-type-handler: com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler
  global-config:
    db-config:
      update-strategy: not_null
      id-type: auto
```

### 2. shared-log.yml (必须配置)
```yaml
logging:
  level:
    com.hmall: debug
  pattern:
    dateformat: HH:mm:ss:SSS
  file:
    path: "logs/${spring.application.name}"
```

### 3. shared-swagger.yml (必须配置)
```yaml
knife4j:
  enable: true
  openapi:
    title: "${hm.swagger.title}"
    description: "${hm.swagger.title}"
    email: zhanghuyi@itcast.cn
    concat: 虎哥
    url: https://www.itcast.cn
    version: v1.0.0
    group:
      default:
        group-name: default
        api-rule: package
        api-rule-resources:
          - "${hm.swagger.package}"
```

### 4. shared-seata.yml (必须配置)
```yaml
seata:
  tx-service-group: hmall
  service:
    vgroup-mapping:
      hmall: default
      default_tx_group: default
    grouplist:
      default: localhost:8091
  registry:
    type: nacos
    nacos:
      server-addr: localhost:8848
      namespace: ""
      group: DEFAULT_GROUP
      application: seata-server
      username: nacos
      password: nacos
  data-source-proxy-mode: AT
```

## 如何在Nacos中配置

1. 访问Nacos控制台：http://localhost:8848/nacos
2. 默认用户名/密码：nacos/nacos
3. 在配置管理 -> 配置列表中，点击"+"创建配置
4. 依次创建以上4个配置文件（Data ID分别为：shared-jdbc.yml、shared-log.yml、shared-swagger.yml、shared-seata.yml）
5. Group使用默认的：DEFAULT_GROUP
6. 配置格式选择：YAML

## 本地开发环境配置
各服务已包含以下配置：
- `application.yml`：基础配置，包含服务端口、hm.db配置等
- `application-dev.yml`：开发环境配置（hm.db.host=mysql, pw=123）
- `application-local.yml`：本地环境配置（需要您自己创建）

您也可以修改`application-dev.yml`中的数据库连接信息，将host改为localhost：
```yaml
hm:
  db:
    host: localhost
    pw: 123
```
