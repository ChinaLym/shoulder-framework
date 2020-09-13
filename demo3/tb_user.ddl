/*
SQLyog Professional v12.09 (64 bit) 与 demo 2 中的表一模一样
MySQL - 8.0.19 
*********************************************************************
*/
/*!40101 SET NAMES utf8 */;

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
