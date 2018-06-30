CREATE TABLE news_item (
  id                INT(11) NOT NULL AUTO_INCREMENT,
  created_timestamp timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
  title             VARCHAR(255)     DEFAULT NULL,
  description       VARCHAR(255)     DEFAULT NULL,
  link              VARCHAR(255)     DEFAULT NULL,
  expiration_date   DATE             DEFAULT NULL,
  type              VARCHAR(20)      DEFAULT NULL,
  active            INT(1) default '1' null,
  PRIMARY KEY (id)
);