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

DROP TABLE IF EXISTS `tb_project`;

CREATE TABLE `tb_project`
(
    `id`          bigint NOT NULL AUTO_INCREMENT,
    `name`        varchar(32) DEFAULT NULL,
    `crate_time`  datetime    DEFAULT NULL,
    `creator`     bigint      DEFAULT NULL,
    `update_time` datetime    DEFAULT NULL,
    `modifier`    bigint      DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO user_info (id, name, age, email)
VALUES (1, 'shoulder', 8, 'shoulder@shoulder.com'),
       (2, 'jack', 22, 'jack@shoulder.com'),
       (3, 'tom', 29, 'tom@shoulder.com'),
       (4, 'sandy', 21, 'sandy@shoulder.com'),
       (5, 'billie', 34, 'billie@shoulder.com');

