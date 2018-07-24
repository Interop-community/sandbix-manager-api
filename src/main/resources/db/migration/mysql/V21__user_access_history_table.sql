CREATE TABLE user_access_history (
  id                    INT(11)       NOT NULL AUTO_INCREMENT,
  access_timestamp      DATETIME      DEFAULT NULL,
  sbm_user_id               VARCHAR(255)  NOT NULL,
  sandbox_id            VARCHAR(255)  NOT NULL,
  PRIMARY KEY (id)
)