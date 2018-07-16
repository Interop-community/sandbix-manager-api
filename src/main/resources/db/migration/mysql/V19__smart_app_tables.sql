CREATE TABLE smart_app (
  id                VARCHAR(36)     NOT NULL,
  name              VARCHAR(100)    NOT NULL,
  manifest_url      VARCHAR(500)    NOT NULL,
  manifest          VARCHAR(4000)   DEFAULT NULL,
  client_id         VARCHAR(50)     DEFAULT NULL,
  owner_id          INT(11)         NOT NULL,
  created_timestamp DATETIME(3)     NOT NULL,
  visibility        VARCHAR(20)     NOT NULL DEFAULT 'PRIVATE',
  sample_patients   VARCHAR(2000)   DEFAULT NULL,
  info              VARCHAR(4000)   DEFAULT NULL,
  brief_description VARCHAR(4000)   DEFAULT NULL,
  author            VARCHAR(100)    DEFAULT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (owner_id) REFERENCES user (id)
);

CREATE INDEX smartapp_owner_idx
  ON smart_app (owner_id);
