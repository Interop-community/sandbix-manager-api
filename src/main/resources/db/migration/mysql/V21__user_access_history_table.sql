CREATE TABLE user_access_history (
  id                    INT(11)       NOT NULL AUTO_INCREMENT,
  access_timestamp DATETIME      DEFAULT NULL,
  user_id               INT(11)       DEFAULT NULL,
  sandbox_id            INT(11)       NOT NULL,
  PRIMARY KEY (id),
  KEY (sandbox_id),
  KEY (user_id),
  CONSTRAINT FOREIGN KEY (sandbox_id) REFERENCES sandbox (id),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id)

)