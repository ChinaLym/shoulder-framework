/* 属于特定应用的配置管理，由应用自身后台管理 */
create table shoulder_ext_config_data
(
    `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `biz_id`         VARCHAR(32)        NOT NULL COMMENT '业务唯一标识(不可修改；业务键拼接并哈希)',
    `delete_version` BIGINT unsigned DEFAULT 0 NOT NULL comment '删除标记：0-未删除；否则为删除时间',
    `version`        INT      DEFAULT 0 NOT NULL COMMENT '数据版本号：用于幂等防并发',
    `tenant`         VARCHAR(32)        NOT NULL COMMENT '租户',
    `type`           VARCHAR(64)        NOT NULL COMMENT '配置类型，通常可据此分库表',
    `description` VARCHAR(255) NULL COMMENT '备注:介绍为啥添加这一条',
    `creator`        VARCHAR(64)        NOT NULL COMMENT '创建人编号',
    `create_time`    DATETIME DEFAULT NOW() COMMENT '创建时间',
    `modifier`       VARCHAR(64)        NOT NULL COMMENT '最近修改人编码',
    update_time      DATETIME DEFAULT NOW() COMMENT '最后修改时间',
    `business_value` TEXT               NOT NULL COMMENT '业务数据，json 类型',

    CONSTRAINT config_data_pk
        PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT '配置数据表';

create
    unique index config_data_uni_biz_index
    on shoulder_ext_config_data (biz_id, delete_version, version);
