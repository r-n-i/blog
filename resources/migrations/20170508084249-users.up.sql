CREATE TABLE IF NOT EXISTS users(
  id int(11) NOT NULL AUTO_INCREMENT,
  encrypted_password varchar(255) NOT NULL,
  email varchar(255) NOT NULL,
  created_at timestamp DEFAULT 0,
  updated_at datetime,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
--;;
ALTER TABLE users ADD INDEX user_email_idx(email);
