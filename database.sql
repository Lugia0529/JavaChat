SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

--
-- Database: `chat`
--

-- --------------------------------------------------------

--
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
CREATE TABLE IF NOT EXISTS `account` (
  `guid` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Global Unique Identifier',
  `username` varchar(50) NOT NULL,
  `password` varchar(50) NOT NULL,
  `title` varchar(200) DEFAULT NULL,
  `psm` varchar(200) DEFAULT NULL,
  `online` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`guid`),
  UNIQUE KEY `username` (`username`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `contact`
--

DROP TABLE IF EXISTS `contact`;
CREATE TABLE IF NOT EXISTS `contact` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `o_guid` int(11) NOT NULL COMMENT 'Owner Global Unique Identifier',
  `c_guid` int(11) NOT NULL COMMENT 'Contact Global Unique Identifier',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `contact_request`
--

DROP TABLE IF EXISTS `contact_request`;
CREATE TABLE IF NOT EXISTS `contact_request` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `o_guid` int(11) NOT NULL COMMENT 'Owner Global Unique Identifier',
  `r_guid` int(11) NOT NULL COMMENT 'Requestor Global Unique Identifier',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8;
