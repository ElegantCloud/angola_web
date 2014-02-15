# SQL Manager 2010 for MySQL 4.5.0.9
# ---------------------------------------
# Host     : 122.96.24.173
# Port     : 3306
# Database : angola


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

SET FOREIGN_KEY_CHECKS=0;

DROP DATABASE IF EXISTS `angola`;

CREATE DATABASE `angola`
    CHARACTER SET 'latin1'
    COLLATE 'latin1_swedish_ci';

USE `angola`;

#
# Structure for the `admin_user` table : 
#

CREATE TABLE `admin_user` (
  `username` varchar(10) NOT NULL,
  `password` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#
# Structure for the `charge_money_config` table : 
#

CREATE TABLE `charge_money_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `charge_money` float(9,2) DEFAULT NULL,
  `gift_money` float(9,2) DEFAULT NULL,
  `description` varchar(200) DEFAULT NULL,
  `status` enum('visible','hidden') DEFAULT 'visible',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

#
# Structure for the `common_config` table : 
#

CREATE TABLE `common_config` (
  `attri_key` varchar(30) NOT NULL,
  `attri_value` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`attri_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#
# Structure for the `device_info` table : 
#

CREATE TABLE `device_info` (
  `username` varchar(30) NOT NULL,
  `countrycode` varchar(10) NOT NULL,
  `brand` varchar(20) DEFAULT NULL,
  `model` varchar(20) DEFAULT NULL,
  `release_ver` varchar(20) DEFAULT NULL,
  `sdk` varchar(20) DEFAULT NULL,
  `width` int(11) DEFAULT NULL,
  `height` int(11) DEFAULT NULL,
  PRIMARY KEY (`username`,`countrycode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#
# Structure for the `im_charge_history` table : 
#

CREATE TABLE `im_charge_history` (
  `chargeId` varchar(64) NOT NULL,
  `username` varchar(64) NOT NULL,
  `money` float(9,2) NOT NULL DEFAULT '0.00',
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` enum('fail','success','processing','vos_fail') DEFAULT 'processing' COMMENT 'charge status',
  `countrycode` varchar(10) DEFAULT NULL,
  `contributor` varchar(64) DEFAULT NULL,
  `contributor_country_code` varchar(10) DEFAULT NULL,
  `charge_money_cfg_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`chargeId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#
# Structure for the `im_user` table : 
#

CREATE TABLE `im_user` (
  `username` varchar(30) NOT NULL,
  `password` varchar(32) NOT NULL,
  `userkey` varchar(32) NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `vosphone` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `vosphone_pwd` varchar(10) DEFAULT NULL,
  `status` enum('processing','success','vos_account_error','vos_phone_error','vos_suite_error') NOT NULL DEFAULT 'processing' COMMENT 'account register status',
  `referrer` varchar(30) DEFAULT NULL,
  `referrer_country_code` varchar(10) DEFAULT NULL,
  `countrycode` varchar(10) DEFAULT NULL,
  `bindphone` varchar(30) DEFAULT NULL,
  `bindphone_country_code` varchar(30) DEFAULT NULL,
  `email` varchar(80) DEFAULT NULL,
  `email_status` enum('unverify','verified') DEFAULT 'unverify',
  `random_id` varchar(20) DEFAULT NULL,
  `frozen_money` float(9,2) DEFAULT '0.00',
  PRIMARY KEY (`vosphone`),
  UNIQUE KEY `full_username` (`username`,`countrycode`)
) ENGINE=InnoDB AUTO_INCREMENT=100274 DEFAULT CHARSET=utf8;

#
# Structure for the `invite_reg_link_tag` table : 
#

CREATE TABLE `invite_reg_link_tag` (
  `inviter_id` varchar(20) NOT NULL,
  `country_code` varchar(10) DEFAULT NULL,
  `username` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`inviter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#
# Structure for the `notices` table : 
#

CREATE TABLE `notices` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content` varchar(500) DEFAULT NULL,
  `createtime` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `status` enum('hidden','publish','draft') NOT NULL DEFAULT 'draft',
  `to_user` varchar(30) DEFAULT 'all',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;

#
# Structure for the `reg_activity_config` table : 
#

CREATE TABLE `reg_activity_config` (
  `id` int(11) NOT NULL,
  `start_date` date DEFAULT '0000-00-00',
  `end_date` date DEFAULT '0000-00-00',
  `gift_money` float(9,2) DEFAULT '0.00',
  `status` enum('open','close') NOT NULL DEFAULT 'close',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='config table for regsiter activity';



/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;