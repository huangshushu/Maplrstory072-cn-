/*
Navicat MySQL Data Transfer

Source Server         : localhost_3306
Source Server Version : 50533
Source Host           : localhost:3306
Source Database       : new113

Target Server Type    : MYSQL
Target Server Version : 50533
File Encoding         : 65001

Date: 2018-01-05 01:23:27
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `wz_customlife`
-- ----------------------------
DROP TABLE IF EXISTS `wz_customlife`;
CREATE TABLE `wz_customlife` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `dataid` int(11) NOT NULL,
  `f` int(11) NOT NULL,
  `hide` tinyint(1) NOT NULL DEFAULT '0',
  `fh` int(11) NOT NULL,
  `type` varchar(1) CHARACTER SET latin1 NOT NULL,
  `cy` int(11) NOT NULL,
  `rx0` int(11) NOT NULL,
  `rx1` int(11) NOT NULL,
  `x` int(11) NOT NULL,
  `y` int(11) NOT NULL,
  `mobtime` int(11) DEFAULT '1000',
  `mid` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of wz_customlife
-- ----------------------------
