CREATE TABLE IF NOT EXISTS entries(
  id int(11) NOT NULL AUTO_INCREMENT,
  title varchar(255) NOT NULL,
  body mediumtext NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

