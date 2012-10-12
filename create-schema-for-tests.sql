USE test;

-- create tables used in tests
CREATE TABLE `test_apples` (
  `apple_id` int(11) NOT NULL AUTO_INCREMENT,
  `nickname` varchar(30) NOT NULL DEFAULT '',
  PRIMARY KEY (`apple_id`),
  UNIQUE KEY `nickname` (`nickname`)
) ENGINE=InnoDB AUTO_INCREMENT=162 DEFAULT CHARSET=latin1;

CREATE TABLE `test_oranges` (
  `orange_id` int(11) NOT NULL AUTO_INCREMENT,
  `nickname` varchar(30) NOT NULL DEFAULT '',
  PRIMARY KEY (`orange_id`),
  UNIQUE KEY `nickname` (`nickname`)
) ENGINE=InnoDB AUTO_INCREMENT=162 DEFAULT CHARSET=latin1;

-- create slaveuser that has only read-only permissions
CREATE USER 'readonlyuser'@'localhost' IDENTIFIED BY 'test123';
GRANT SELECT ON test.* TO 'readonlyuser'@'localhost';