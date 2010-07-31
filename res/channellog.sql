-- phpMyAdmin SQL Dump
-- version 3.2.0.1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Jul 30, 2010 at 07:55 PM
-- Server version: 5.1.36
-- PHP Version: 5.3.0

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

--
-- Database: `channellog`
--
CREATE DATABASE `channellog` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `channellog`;

-- --------------------------------------------------------

--
-- Table structure for table `channel`
--

DROP TABLE IF EXISTS `channel`;
CREATE TABLE IF NOT EXISTS `channel` (
  `pk_ChannelID` int(11) NOT NULL AUTO_INCREMENT,
  `vcServer` varchar(256) CHARACTER SET latin1 NOT NULL,
  `vcChannel` varchar(512) CHARACTER SET latin1 NOT NULL,
  PRIMARY KEY (`pk_ChannelID`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=2 ;


-- --------------------------------------------------------

--
-- Table structure for table `messages`
--

DROP TABLE IF EXISTS `messages`;
CREATE TABLE IF NOT EXISTS `messages` (
  `pk_MessageID` bigint(20) NOT NULL AUTO_INCREMENT,
  `fk_ChannelID` int(11) NOT NULL,
  `tsMsgTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `intMsgType` int(11) NOT NULL,
  `vcUsername` varchar(255) NOT NULL,
  `vcMessage` text CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`pk_MessageID`),
  KEY `vcUsername` (`vcUsername`),
  KEY `fk_ChannelID` (`fk_ChannelID`),
  FULLTEXT KEY `vcMessage` (`vcMessage`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=17 ;
