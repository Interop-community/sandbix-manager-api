# Create table for Custom Hook
CREATE TABLE custom_hook (
  id                    INT(11) NOT NULL AUTO_INCREMENT,
  hook             VARCHAR(255)     DEFAULT NULL,
  context               JSON             DEFAULT NULL,
  created_by_id         INT(11)          DEFAULT NULL,
  created_timestamp     DATETIME         DEFAULT NULL,
  sandbox_id            INT(11)          DEFAULT NULL,
  visibility            INT(11)          DEFAULT NULL,
  PRIMARY KEY (id),
  KEY (created_by_id),
  KEY (sandbox_id),
  CONSTRAINT FOREIGN KEY (created_by_id) REFERENCES user (id),
  CONSTRAINT FOREIGN KEY (sandbox_id) REFERENCES sandbox (id)

);
