CREATE TABLE IF NOT EXISTS entries(
  id int(11) NOT NULL AUTO_INCREMENT,
  user_id int(11) NOT NULL,
  title varchar(255) NOT NULL,
  body mediumtext NOT NULL,
  created_at timestamp DEFAULT 0,
  updated_at timestamp,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
--;;
CREATE INDEX entries_user_id on entries(user_id);
