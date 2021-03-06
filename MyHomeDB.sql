-- --------------------------------------------------------
-- 主机:                           y23141394i.imwork.net
-- 服务器版本:                        10.3.21-MariaDB - Source distribution
-- 服务器操作系统:                      Linux
-- HeidiSQL 版本:                  10.2.0.5646
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


-- 导出 myhome 的数据库结构
CREATE DATABASE IF NOT EXISTS `myhome` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `myhome`;

-- 导出  表 myhome.tl_boolean 结构
CREATE TABLE IF NOT EXISTS `tl_boolean` (
  `CN_ID` varchar(32) NOT NULL,
  `CN_NAME` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`CN_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='部位类型,记录是|否';


-- 正在导出表  myhome.tl_boolean 的数据：~2 rows (大约)
/*!40000 ALTER TABLE `tl_boolean` DISABLE KEYS */;
INSERT INTO `tl_boolean` (`CN_ID`, `CN_NAME`) VALUES
	('no', 'no'),
	('yes', 'yes');
/*!40000 ALTER TABLE `tl_boolean` ENABLE KEYS */;


-- 导出  表 myhome.tn_debit_and_credit 结构
CREATE TABLE IF NOT EXISTS `tn_debit_and_credit` (
  `CN_ID` varchar(32) NOT NULL,
  `CR_IN_FLAG` varchar(32) DEFAULT NULL COMMENT '是为借入,否为借出',
  `CN_AMOUNT` decimal(10,2) DEFAULT NULL COMMENT '借贷的金额',
  `CN_DESCRIPTION` varchar(1000) DEFAULT NULL COMMENT '本次借贷的描述信息',
  `CN_CREATE_DATE` bigint(20) DEFAULT NULL COMMENT '创建日期',
  PRIMARY KEY (`CN_ID`),
  KEY `FK_TN_DEBIT_AND_CREDIT_R_TL_BOOLEAN_ON_CR_IN_FLAG` (`CR_IN_FLAG`),
  CONSTRAINT `FK_TN_DEBIT_AND_CREDIT_R_TL_BOOLEAN_ON_CR_IN_FLAG` FOREIGN KEY (`CR_IN_FLAG`) REFERENCES `tl_boolean` (`CN_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='借贷表,记录借出和借入的账目';

-- 数据导出被取消选择。

-- 导出  表 myhome.tn_expenditure 结构
CREATE TABLE IF NOT EXISTS `tn_expenditure` (
  `CN_ID` varchar(32) NOT NULL,
  `CR_EXPENDITURE_TYPE_ID` varchar(32) DEFAULT NULL COMMENT '收入类型',
  `CN_AMOUNT` decimal(10,2) DEFAULT NULL COMMENT '金额',
  `CR_PLAN_FLAG` varchar(32) DEFAULT NULL COMMENT '是为计划支出,否为实际支出',
  `CN_CREATE_DATE` bigint(20) DEFAULT NULL COMMENT '创建日期',
  `CN_DESCRIPTION` varchar(1000) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`CN_ID`),
  KEY `FK_TN_EXPENDITURE_R_TN_EXPENDITURE_TYPE` (`CR_EXPENDITURE_TYPE_ID`),
  KEY `FK_TN_EXPENDITURE_R_TL_BOOLEAN_ON_CR_PLAN_FLAG` (`CR_PLAN_FLAG`),
  CONSTRAINT `FK_TN_EXPENDITURE_R_TL_BOOLEAN_ON_CR_PLAN_FLAG` FOREIGN KEY (`CR_PLAN_FLAG`) REFERENCES `tl_boolean` (`CN_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_TN_EXPENDITURE_R_TN_EXPENDITURE_TYPE` FOREIGN KEY (`CR_EXPENDITURE_TYPE_ID`) REFERENCES `tn_expenditure_type` (`CN_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='支出表,记录支出信息.';

-- 数据导出被取消选择。

-- 导出  表 myhome.tn_expenditure_type 结构
CREATE TABLE IF NOT EXISTS `tn_expenditure_type` (
  `CN_ID` varchar(32) NOT NULL,
  `CN_NAME` varchar(10) DEFAULT NULL,
  `CR_PERIODIC_FLAG` varchar(32) DEFAULT NULL COMMENT '是否为周期性支出',
  `CN_EFFECTIVE_DAY` bigint(20) DEFAULT NULL COMMENT '如果是周期性,保存每月的生效日1~31',
  `CN_AMOUNT` decimal(10,2) DEFAULT NULL COMMENT '金额',
  `CN_START_DATE` bigint(20) DEFAULT NULL COMMENT '如果是周期性,保存开始周期开始日期',
  `CN_END_DATE` bigint(20) DEFAULT NULL COMMENT '如果是周期性,保存开始周期结束日期',
  PRIMARY KEY (`CN_ID`),
  KEY `FK_TN_EXPENDITURE_TYPE_R_BOOLEAN_ON_CR_PERIODIC_FLAG` (`CR_PERIODIC_FLAG`),
  CONSTRAINT `FK_TN_EXPENDITURE_TYPE_R_BOOLEAN_ON_CR_PERIODIC_FLAG` FOREIGN KEY (`CR_PERIODIC_FLAG`) REFERENCES `tl_boolean` (`CN_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='支出类型';

-- 数据导出被取消选择。

-- 导出  表 myhome.tn_housing_fund_withdrawal 结构
CREATE TABLE IF NOT EXISTS `tn_housing_fund_withdrawal` (
  `CN_ID` varchar(32) NOT NULL,
  `CN_CREATE_DATE` bigint(20) NOT NULL COMMENT '提醒日期',
  `CN_AMOUNT` decimal(10,2) NOT NULL COMMENT '提现金额',
  `CN_DESCRIPTION` varchar(1000) NOT NULL COMMENT '备注',
  PRIMARY KEY (`CN_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='公积金提取表';

-- 数据导出被取消选择。

-- 导出  表 myhome.tn_income 结构
CREATE TABLE IF NOT EXISTS `tn_income` (
  `CN_ID` varchar(32) NOT NULL,
  `CR_INCOME_TYPE_ID` varchar(32) DEFAULT NULL COMMENT '收入类型',
  `CN_AMOUNT` decimal(10,2) DEFAULT NULL COMMENT '金额',
  `CR_PLAN_FLAG` varchar(32) DEFAULT NULL COMMENT '是为计划收入,否为实际收入',
  `CN_CREATE_DATE` bigint(20) DEFAULT NULL COMMENT '创建日期',
  `CN_DESCRIPTION` varchar(1000) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`CN_ID`),
  KEY `FK_TN_INCOME_R_TN_INCOME_TYPE_ON_CR_INCOME_TYPE_ID` (`CR_INCOME_TYPE_ID`),
  KEY `FK_TN_INCOME_R_TL_BOOLEAN_ON_CR_PLAN_FLAG` (`CR_PLAN_FLAG`),
  CONSTRAINT `FK_TN_INCOME_R_TL_BOOLEAN_ON_CR_PLAN_FLAG` FOREIGN KEY (`CR_PLAN_FLAG`) REFERENCES `tl_boolean` (`CN_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_TN_INCOME_R_TN_INCOME_TYPE_ON_CR_INCOME_TYPE_ID` FOREIGN KEY (`CR_INCOME_TYPE_ID`) REFERENCES `tn_income_type` (`CN_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='收入表,记录收入信息.';

-- 数据导出被取消选择。

-- 导出  表 myhome.tn_income_type 结构
CREATE TABLE IF NOT EXISTS `tn_income_type` (
  `CN_ID` varchar(32) NOT NULL,
  `CN_NAME` varchar(10) DEFAULT NULL,
  `CR_PERIODIC_FLAG` varchar(32) DEFAULT NULL COMMENT '是否为周期性收入',
  `CN_EFFECTIVE_DAY` bigint(20) DEFAULT NULL COMMENT '如果是周期性,保存每月的生效日1~31',
  `CN_AMOUNT` decimal(10,2) DEFAULT NULL COMMENT '金额',
  `CN_FROZEN_FLAG` varchar(32) DEFAULT NULL COMMENT '是否冻结资金,如公积金,定期存折等.',
  PRIMARY KEY (`CN_ID`),
  KEY `FK_TN_INCOME_TYPE_R_BOOLEAN_ON_CR_PERIODIC_FLAG` (`CR_PERIODIC_FLAG`),
  KEY `FK_TN_INCOME_TYPE_R_BOOLEAN_ON_CR_FROZEN_FLAG` (`CN_FROZEN_FLAG`),
  CONSTRAINT `FK_TN_INCOME_TYPE_R_BOOLEAN_ON_CR_FROZEN_FLAG` FOREIGN KEY (`CN_FROZEN_FLAG`) REFERENCES `tl_boolean` (`CN_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_TN_INCOME_TYPE_R_BOOLEAN_ON_CR_PERIODIC_FLAG` FOREIGN KEY (`CR_PERIODIC_FLAG`) REFERENCES `tl_boolean` (`CN_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='收入类型';

-- 数据导出被取消选择。

-- 导出  表 myhome.tn_user 结构
CREATE TABLE IF NOT EXISTS `tn_user` (
  `CN_ID` varchar(32) NOT NULL,
  `CN_NAME` varchar(32) NOT NULL,
  `CR_ACTIVE_FLAG` varchar(32) NOT NULL,
  `CN_LOGIN_NAME` varchar(32) NOT NULL,
  `CN_PASSWORD` varchar(32) NOT NULL,
  `CN_ICO` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`CN_ID`),
  KEY `FK_TN_USER_R_TL_BOOLEAN_ON_CR_ACTIVE_FLAG` (`CR_ACTIVE_FLAG`),
  CONSTRAINT `FK_TN_USER_R_TL_BOOLEAN_ON_CR_ACTIVE_FLAG` FOREIGN KEY (`CR_ACTIVE_FLAG`) REFERENCES `tl_boolean` (`CN_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户表';

-- 数据导出被取消选择。

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
