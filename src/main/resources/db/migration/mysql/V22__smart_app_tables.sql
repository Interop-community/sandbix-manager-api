CREATE TABLE smart_app (
  smart_app_id      VARCHAR(36)     NOT NULL,
  sandbox_id        VARCHAR(255)    NOT NULL,
  manifest_url      VARCHAR(500)    DEFAULT NULL,
  manifest          VARCHAR(4000)   DEFAULT NULL,
  client_id         VARCHAR(50)     DEFAULT NULL,
  owner_id          INT(11)         NOT NULL,
  created_timestamp DATETIME(3)     NOT NULL,
  visibility        VARCHAR(20)     NOT NULL DEFAULT 'PRIVATE',
  sample_patients   VARCHAR(2000)   DEFAULT NULL,
  info              VARCHAR(4000)   DEFAULT NULL,
  brief_description VARCHAR(4000)   DEFAULT NULL,
  author            VARCHAR(100)    DEFAULT NULL,
  copy_type         VARCHAR(36)     DEFAULT 'REPLICA',
  PRIMARY KEY (smart_app_id, sandbox_id),
  CONSTRAINT FOREIGN KEY (owner_id) REFERENCES user (id)
);



CREATE INDEX smartapp_owner_idx
  ON smart_app (owner_id);
