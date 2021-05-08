/*
SQLyog Professional v12.09 (64 bit)
MySQL - 8.0.19 : Database - demo_shoulder
*********************************************************************
*/


/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`demo_shoulder` /*!40100 DEFAULT CHARACTER SET utf8 */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `demo_shoulder`;

/*Table structure for table `tb_project` */

DROP TABLE IF EXISTS `tb_project`;

CREATE TABLE `tb_project` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(32) DEFAULT NULL,
  `crate_time` datetime DEFAULT NULL,
  `creator` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `modifier` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `tb_project` */


/*Table structure for table `log_server` */

CREATE TABLE `log_server` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `command_id` BIGINT NOT NULL COMMENT '服务器远程日志ID，linux为历史命令分配的递增序号',
  `user_name` varchar(128) NOT NULL COMMENT '执行命令使用用户的名称（可分组）',
  `login_ip` varchar(48) NOT NULL COMMENT '登录终端IP（可分组）',
  `command` varchar(1024) NOT NULL COMMENT '执行的命令',
  `login_time` timestamp NOT NULL COMMENT '用户登录时间（可分组）',
  `operation_time` timestamp NOT NULL COMMENT '操作时间',
  `operation_localtime` timestamp NOT NULL COMMENT '执行命令时，服务器本地时间，是否记录采集时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='服务器shell/bash命令日志';

CREATE TABLE `system_lock` (
  `resource` varchar(64) NOT NULL COMMENT '锁定的资源，组件标识:模块标识:资源/操作标识',
  `owner` varchar(64) NOT NULL COMMENT '持有者，可通过该值解析持有应用 / 机器 / 线程 等',
  `token` varchar(64) NOT NULL COMMENT '令牌，用于操作锁（获取、解锁、修改）在达到 ttl 之前，必须通过该令牌，才能对锁进行操作',
  `version` INT NOT NULL DEFAULT '0' COMMENT '版本号',
  `lock_time` datetime NOT NULL COMMENT '上锁时间',
  `release_time` datetime NOT NULL COMMENT '超时自动释放时间',
  `description` varchar(128) NOT NULL DEFAULT '' COMMENT '备注：描述这个锁的目的',
  PRIMARY KEY (`resource`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='全局锁';

CREATE TABLE `crypt_info` (
  `component_id` varchar(32) NOT NULL COMMENT '应用标识',
  `header` varchar(32) NOT NULL DEFAULT '' COMMENT '密文前缀标识，算法标识',
  `data_key` varchar(64) NOT NULL COMMENT '数据密钥（密文）',
  `root_key_part` varchar(64) DEFAULT NULL COMMENT '根密钥部件',
  `vector` varchar(64) DEFAULT NULL COMMENT '初始偏移向量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`component_id`,`header`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='加密元信息';

/*Data for the table `crypt_info` */

/*Table structure for table `batch_record` */

CREATE TABLE `batch_record` (
  `id` varchar(48) NOT NULL COMMENT '主键',
  `data_type` varchar(64) NOT NULL COMMENT '导入数据类型，建议可翻译。对应 导入数据库表名 / 领域对象名称，如用户、人员、订单',
  `operation` varchar(64) COMMENT '业务操作类型，如校验、同步、导入、更新，可空',
  `total_num` INT NOT NULL COMMENT '导入总条数',
  `success_num` INT NOT NULL COMMENT '成功条数',
  `fail_num` INT NOT NULL COMMENT '失败条数',
  `creator` BIGINT NOT NULL COMMENT '执行导入的用户',
  `create_time` datetime NOT NULL COMMENT '导入时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='导入记录';

/*Data for the table `batch_record` */

/*Table structure for table `batch_record_detail` */

CREATE TABLE `batch_record_detail` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `record_id` varchar(48) NOT NULL COMMENT '导入记录表id',
  `row_num` INT NOT NULL COMMENT '导入行号',
  `operation` varchar(64) NOT NULL COMMENT '业务操作类型，如校验、同步、导入、更新',
  `status` INT NOT NULL COMMENT '结果 0 导入成功 1 校验失败、2 重复跳过、3 重复更新、4 导入失败',
  `fail_reason` varchar(1024) DEFAULT NULL COMMENT '失败原因，推荐支持多语言',
  `source` text COMMENT '导入的原数据',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='导入记录详情';

/*Data for the table `batch_record_detail` */

/*Table structure for table `log_operation` */

CREATE TABLE `log_operation` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                 `component_id` varchar(32) NOT NULL COMMENT '组件id',
                                 `version` varchar(64) DEFAULT NULL COMMENT '组件版本',
                                 `instance_id` varchar(64) DEFAULT NULL COMMENT '操作服务器节点标识（支持集群时用于定位具体哪台服务器执行）',
                                 `user_id` BIGINT NOT NULL COMMENT '用户标识',
                                 `user_name` varchar(64) DEFAULT NULL COMMENT '用户名',
                                 `user_real_name` varchar(128) DEFAULT NULL COMMENT '用户真实姓名',
                                 `user_org_id` BIGINT DEFAULT NULL COMMENT '用户组标识',
                                 `user_org_name` varchar(64) DEFAULT NULL COMMENT '用户组名',
                                 `terminal_type` INT NOT NULL COMMENT '终端类型。0:服务内部定时任务等触发；1:浏览器；2:客户端；3:移动App；4:小程序。推荐前端支持多语言',
                                 `terminal_address` varchar(64) DEFAULT NULL COMMENT '操作者所在终端地址，如 IPv4(15) IPv6(46)',
                                 `terminal_id` varchar(64) DEFAULT NULL COMMENT '操作者所在终端标识，如PC的 MAC；手机的 IMSI、IMEI、ESN、MEID；甚至持久化的 UUID',
                                 `terminal_info` varchar(255) DEFAULT NULL COMMENT '操作者所在终端信息，如操作系统类型、浏览器、版本号等',
                                 `object_type` varchar(128) DEFAULT NULL COMMENT '操作对象类型，通常展示，推荐支持多语言',
                                 `object_id` varchar(128) DEFAULT NULL COMMENT '操作对象id',
                                 `object_name` varchar(255) DEFAULT NULL COMMENT '操作对象名称',
                                 `operation_param` text COMMENT '触发该操作的参数',
                                 `operation` varchar(255) NOT NULL COMMENT '操作动作，通常展示，推荐支持多语言',
                                 `detail` varchar(4096) DEFAULT NULL COMMENT '操作详情。详细的描述用户的操作内容、json对象等',
                                 `detail_key` varchar(128) DEFAULT NULL COMMENT '操作详情对应的多语言key',
                                 `detail_item` varchar(255) DEFAULT NULL COMMENT '填充 detail_i18n_key 对应的多语言翻译。数组类型',
                                 `result` INT NOT NULL COMMENT '操作结果,0成功；1失败；2部分成功，通常展示，推荐支持多语言',
                                 `error_code` varchar(32) DEFAULT NULL COMMENT '错误码',
                                 `operation_time` timestamp NOT NULL COMMENT '操作触发时间，注意采集完成后替换为日志服务所在服务器时间',
                                 `end_time` timestamp NULL DEFAULT NULL COMMENT '操作结束时间',
                                 `duration` BIGINT DEFAULT NULL COMMENT '操作持续时间，冗余字段，单位 ms',
                                 `trace_id` varchar(64) DEFAULT NULL COMMENT '调用链id',
                                 `relation_id`     varchar(64) DEFAULT NULL COMMENT '关联的调用链id/业务id',
                                 `tenant_code`     varchar(20) DEFAULT '' COMMENT '租户编码',
                                 `insert_time`     timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '数据入库时间',
                                 `extended_field0` varchar(1024) DEFAULT NULL,
                                 `extended_field1` varchar(1024) DEFAULT NULL,
                                 `extended_field2` varchar(1024) DEFAULT NULL,
                                 `extended_field3` varchar(1024) DEFAULT NULL,
                                 `extended_field4` varchar(1024) DEFAULT NULL,
                                 PRIMARY KEY (`id`),
                                 KEY               `idx_trace_id` (`trace_id`),
                                 KEY               `idx_operation_time` (`operation_time`),
                                 KEY               `idx_user_id` (`user_id`),
                                 KEY               `idx_terminal_address` (`terminal_address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='业务日志';

/*Data for the table `log_operation` */


/*Table structure for table `user` */

DROP TABLE IF EXISTS `user`;

create table demo_shoulder.tb_user
(
    id          bigint                              not null comment '主键ID'
        primary key,
    username    varchar(32) null comment '用户名',
    phone_num   varchar(32) null comment '手机号',
    password    varchar(128) null comment '密码（密文）',
    name        varchar(32) null comment '姓名',
    age         int null comment '年龄',
    email       varchar(64) null comment '邮箱',
    create_time timestamp default CURRENT_TIMESTAMP not null,
    update_time timestamp null
);

/*Data for the table `user` 其中密码是由 BCEncryptor 加密的，其明文均为 shoulder */

INSERT INTO user (id, username, PASSWORD, NAME, age, email)
VALUES (1, 'shoulder', '$2a$10$tNmATC/VgsmFXEgCm2vIX.ndFlVGS/VKff.4L.9UGkW/JegEC4NXu', 'Shoulder', 8,
        'shoulder@shoulder.com'),
       (2, 'jack', '$2a$10$krRf9r03W2hbyDeX64f5Nud0zyjf5Q7af3yTRPQU6aF98l/T5A6Zi', 'Jack', 20, 'jack@shoulder.com'),
       (3, 'tom', '$2a$10$zMbaVJ3qfEZAPenBgjTg/u2zfw96Bb3iLbJclJ63cs9EFA95Q1Eta', 'Tom', 28, 'tom@shoulder.com'),
       (4, 'sandy', '$2a$10$QAWTiks6bBNUUuWlwCLDwet4.9SGVGNWafbjZkspRq/H3eX/ouorq', 'Sandy', 21, 'sandy@shoulder.com'),
       (5, 'billie', '$2a$10$4PMa6nS8oiNoGXvbTzH/PeTzBQlUMmnWrnmMGzHj5nCWc/OOme04e', 'Billie', 34,
        'billie@shoulder.com');

