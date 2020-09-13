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

/*Table structure for table `tb_user` */

DROP TABLE IF EXISTS `tb_user`;

create table `tb_user` (
	`id` bigint (20),
	`username` varchar (128),
	`password` varchar (512),
	`name` varchar (128),
	`age` int (11),
	`email` varchar (256)
);

/*Data for the table `tb_user` 其中密码是由 BCEncryptor 加密的，其明文均为 shoulder */

INSERT INTO tb_user (id, username, PASSWORD, NAME, age, email) VALUES
(1, 'shoulder', '$2a$10$tNmATC/VgsmFXEgCm2vIX.ndFlVGS/VKff.4L.9UGkW/JegEC4NXu', 'Shoulder', 8, 'shoulder@shoulder.com'),
(2, 'jack', '$2a$10$krRf9r03W2hbyDeX64f5Nud0zyjf5Q7af3yTRPQU6aF98l/T5A6Zi', 'Jack', 20, 'jack@shoulder.com'),
(3, 'tom', '$2a$10$zMbaVJ3qfEZAPenBgjTg/u2zfw96Bb3iLbJclJ63cs9EFA95Q1Eta', 'Tom', 28, 'tom@shoulder.com'),
(4, 'sandy', '$2a$10$QAWTiks6bBNUUuWlwCLDwet4.9SGVGNWafbjZkspRq/H3eX/ouorq', 'Sandy', 21, 'sandy@shoulder.com'),
(5, 'billie', '$2a$10$4PMa6nS8oiNoGXvbTzH/PeTzBQlUMmnWrnmMGzHj5nCWc/OOme04e', 'Billie', 34, 'billie@shoulder.com');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
