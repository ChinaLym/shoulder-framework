/*Table structure for table `crypt_info` 加密部件表，可以由每个应用自身维护，也可统一管理 */

CREATE TABLE `crypt_info`
(
    `app_id`        VARCHAR(32) NOT NULL COMMENT '应用标识',
    `header`        VARCHAR(32) NOT NULL DEFAULT '' COMMENT '密文前缀/算法标识/版本标志',
    `data_key`      VARCHAR(64) NOT NULL COMMENT '数据密钥（密文）',
    `root_key_part` VARCHAR(64)          DEFAULT NULL COMMENT '根密钥部件',
    `vector`        VARCHAR(64)          DEFAULT NULL COMMENT '初始偏移向量',
    `create_time`   DATETIME             DEFAULT NOW() COMMENT '创建时间',
    PRIMARY KEY (`app_id`, `header`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='加密元信息';
