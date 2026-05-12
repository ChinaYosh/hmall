# Seata Server配置说明

## 问题描述
错误信息：`Failed to store branch xid = ...`
这个错误表明Seata Server无法在数据库中存储分支事务信息。

## 解决方案

### 1. 确保Seata Server的数据库配置正确

Seata Server需要一个数据库来存储事务信息。请检查Seata Server的配置文件（`conf/file.conf`）：

```properties
store {
  mode = "db"
  
  db {
    datasource = "druid"
    dbType = "mysql"
    driverClassName = "com.mysql.cj.jdbc.Driver"
    url = "jdbc:mysql://localhost:33099/seata?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai"
    user = "root"
    password = "123"
    minConn = 5
    maxConn = 30
    globalTable = "global_table"
    branchTable = "branch_table"
    lockTable = "lock_table"
    queryLimit = 100
    maxWait = 5000
  }
}
```

### 2. 创建Seata数据库和表

首先需要在MySQL中创建`seata`数据库，然后执行以下SQL创建必要的表：

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS `seata` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `seata`;

-- 创建全局事务表
CREATE TABLE IF NOT EXISTS `global_table` (
  `xid` VARCHAR(128) NOT NULL,
  `transaction_id` BIGINT NULL,
  `status` TINYINT NOT NULL,
  `application_id` VARCHAR(32) NULL,
  `transaction_service_group` VARCHAR(32) NULL,
  `transaction_name` VARCHAR(128) NULL,
  `timeout` INT NULL,
  `begin_time` BIGINT NULL,
  `application_data` VARCHAR(2000) NULL,
  `gmt_create` DATETIME NULL,
  `gmt_modified` DATETIME NULL,
  PRIMARY KEY (`xid`),
  KEY `idx_status_gmt_modified` (`status` , `gmt_modified`),
  KEY `idx_transaction_id` (`transaction_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 创建分支事务表
CREATE TABLE IF NOT EXISTS `branch_table` (
  `branch_id` BIGINT NOT NULL,
  `xid` VARCHAR(128) NOT NULL,
  `transaction_id` BIGINT NULL,
  `resource_group_id` VARCHAR(32) NULL,
  `resource_id` VARCHAR(256) NULL,
  `branch_type` VARCHAR(8) NULL,
  `status` TINYINT NULL,
  `client_id` VARCHAR(64) NULL,
  `application_data` VARCHAR(2000) NULL,
  `gmt_create` DATETIME(6) NULL,
  `gmt_modified` DATETIME(6) NULL,
  PRIMARY KEY (`branch_id`),
  KEY `idx_xid` (`xid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 创建锁表
CREATE TABLE IF NOT EXISTS `lock_table` (
  `row_key` VARCHAR(128) NOT NULL,
  `xid` VARCHAR(128) NULL,
  `transaction_id` BIGINT NULL,
  `branch_id` BIGINT NOT NULL,
  `resource_id` VARCHAR(256) NULL,
  `table_name` VARCHAR(32) NULL,
  `pk` VARCHAR(36) NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0-待处理,1-已处理',
  `gmt_create` DATETIME NULL,
  `gmt_modified` DATETIME NULL,
  PRIMARY KEY (`row_key`),
  KEY `idx_branch_id` (`branch_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
```

### 3. 在各业务数据库中创建undo_log表

每个业务数据库（hm-cart, hm-item, hm-user, hm-trade, hm-pay）都需要创建undo_log表：

```sql
-- 在每个业务数据库中执行
CREATE TABLE IF NOT EXISTS `undo_log` (
  `branch_id` BIGINT NOT NULL COMMENT 'branch transaction id',
  `xid` VARCHAR(128) NOT NULL COMMENT 'global transaction id',
  `context` VARCHAR(128) NOT NULL COMMENT 'undo_log context, such as serialization',
  `rollback_info` LONGBLOB NOT NULL COMMENT 'rollback info',
  `log_status` INT NOT NULL COMMENT '0:normal status,1:defense status',
  `log_created` DATETIME NOT NULL COMMENT 'create datetime',
  `log_modified` DATETIME NOT NULL COMMENT 'modify datetime',
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT = 'AT transaction undo log';
```

### 4. 检查Seata Server的注册中心配置

确保Seata Server的`conf/registry.conf`配置正确，使用Nacos作为注册中心：

```properties
registry {
  type = "nacos"
  
  nacos {
    application = "seata-server"
    serverAddr = "localhost:8848"
    namespace = ""
    group = "DEFAULT_GROUP"
    cluster = "default"
    username = "nacos"
    password = "nacos"
  }
}

config {
  type = "nacos"
  
  nacos {
    serverAddr = "localhost:8848"
    namespace = ""
    group = "DEFAULT_GROUP"
    username = "nacos"
    password = "nacos"
  }
}
```

### 5. 重启Seata Server

完成以上配置后，重启Seata Server以使配置生效。

### 6. 验证配置

启动后检查Seata Server的日志，确保：
- 成功连接到数据库
- 成功注册到Nacos
- 没有错误信息

## 完整的Nacos配置（shared-seata.yml）

确保在Nacos中的shared-seata.yml配置如下：

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

## 总结

解决Seata事务问题的关键步骤：
1. ✅ 创建Seata数据库和所需的表
2. ✅ 在每个业务数据库中创建undo_log表
3. ✅ 正确配置Seata Server的数据库和注册中心
4. ✅ 在Nacos中配置shared-seata.yml
5. ✅ 重启Seata Server和所有微服务
