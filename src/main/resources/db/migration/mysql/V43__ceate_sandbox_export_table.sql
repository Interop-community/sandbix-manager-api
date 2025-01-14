CREATE TABLE sandbox_export (
  id               INT(11) NOT NULL AUTO_INCREMENT,
  sandbox_id       INT(11)          DEFAULT NULL,
  created_time     DATETIME         DEFAULT NULL,
  user_id          VARCHAR(255)     DEFAULT NULL,
  token            TEXT             DEFAULT NULL,
  server           VARCHAR(255)     DEFAULT NULL,
  status           VARCHAR(255)     DEFAULT NULL,
  reason           TEXT            DEFAULT NULL,
  completed_time   DATETIME         DEFAULT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (sandbox_id) REFERENCES sandbox (id)
);