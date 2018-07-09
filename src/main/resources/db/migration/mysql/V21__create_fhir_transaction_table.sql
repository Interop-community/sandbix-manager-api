CREATE TABLE fhir_transaction (
  id                    INT(11)       NOT NULL AUTO_INCREMENT,
  transaction_timestamp DATETIME      DEFAULT NULL,
  user_id               INT(11)       DEFAULT NULL,
  sandbox_id            INT(11)       NOT NULL,
  url                   VARCHAR(255)  DEFAULT NULL,
  fhir_resource         VARCHAR(255)  DEFAULT NULL,
  domain                VARCHAR(255)  DEFAULT NULL,
  ip_address            VARCHAR(255)  DEFAULT NULL,
  method                VARCHAR(11)   DEFAULT NULL,
  response_code         INT(11)       DEFAULT NULL,
  secured               TINYINT(1)    DEFAULT NULL,
  PRIMARY KEY (id),
  KEY (sandbox_id),
  KEY (user_id),
  CONSTRAINT FOREIGN KEY (sandbox_id) REFERENCES sandbox (id),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id)

)