CREATE TABLE fhir_transaction (
  id                    INT(11)       NOT NULL AUTO_INCREMENT,
  transaction_timestamp DATETIME      DEFAULT NULL,
  performed_by_id       INT(11)       DEFAULT NULL,
  sandbox_id            INT(11)       NOT NULL,
  url                   VARCHAR(255)  DEFAULT NULL,
  fhir_resource         VARCHAR(255)  DEFAULT NULL,
  domain                VARCHAR(255)  DEFAULT NULL,
  ip_address            VARCHAR(255)  DEFAULT NULL,
  method                VARCHAR(11)   DEFAULT NULL,
  response_code         INT(11)       DEFAULT NULL,
  secured               TINYINT(1)    DEFAULT NULL,
  payer_user_id         INT(11)       DEFAULT NULL,
  PRIMARY KEY (id)
)