# Create tables for CDS-Service Endpoint, CDS-Hook, Joint table, LaunchScenarioCdsServiceEndpoint
CREATE TABLE cds_hook (
  id                    INT(11) NOT NULL AUTO_INCREMENT,
  logo_id               INT(11)          DEFAULT NULL,
  logo_uri              VARCHAR(255)     DEFAULT NULL,
  hook                  VARCHAR(255)     DEFAULT NULL,
  title                 VARCHAR(255)     DEFAULT NULL,
  description           VARCHAR(255)     DEFAULT NULL,
  hook_id                VARCHAR(255)     DEFAULT NULL,
  prefetch              VARCHAR(255)     DEFAULT NULL,
  PRIMARY KEY (id),
  KEY (logo_id),
  CONSTRAINT FOREIGN KEY (logo_id) REFERENCES image (id)
);

CREATE TABLE cds_service_endpoint (
  id                    INT(11) NOT NULL AUTO_INCREMENT,
  title                 VARCHAR(255)     DEFAULT NULL,
  description           VARCHAR(255)     DEFAULT NULL,
  url                   VARCHAR(255)     DEFAULT NULL,
  cds_hook_id           INT(11)          DEFAULT NULL,
  created_by_id         INT(11)          DEFAULT NULL,
  created_timestamp     DATETIME         DEFAULT NULL,
  sandbox_id            INT(11)          DEFAULT NULL,
  visibility            INT(11)          DEFAULT NULL,
  PRIMARY KEY (id),
  KEY (cds_hook_id),
  KEY (created_by_id),
  KEY (sandbox_id),
  CONSTRAINT FOREIGN KEY (cds_hook_id) REFERENCES cds_hook (id),
  CONSTRAINT FOREIGN KEY (created_by_id) REFERENCES user (id),
  CONSTRAINT FOREIGN KEY (sandbox_id) REFERENCES sandbox (id)
);

CREATE TABLE cds_service_endpoint_cds_hooks (
  cds_service_endpoint_id INT(11)       NOT NULL,
  cds_hooks_id             INT(11)       NOT NULL,
  UNIQUE KEY (cds_hooks_id),
  KEY (cds_service_endpoint_id),
  CONSTRAINT FOREIGN KEY (cds_hooks_id) REFERENCES cds_hook (id),
  CONSTRAINT FOREIGN KEY (cds_service_endpoint_id) REFERENCES cds_service_endpoint (id)
);

CREATE TABLE launch_scenario_cds_service_endpoint (
  id                    INT(11) NOT NULL AUTO_INCREMENT,
  description           VARCHAR(255)     DEFAULT NULL,
  user_persona_id       INT(11)          DEFAULT NULL,
  cds_service_endpoint_id INT(11)        DEFAULT NULL,
  cds_hook_id           INT(11)          DEFAULT NULL,
  context               VARCHAR(255)            DEFAULT NULL,
  last_launch           DATETIME         DEFAULT NULL,
  created_by_id         INT(11)          DEFAULT NULL,
  sandbox_id            INT(11)          DEFAULT NULL,
  PRIMARY KEY (id),
  KEY (cds_service_endpoint_id),
  KEY (created_by_id),
  KEY (sandbox_id),
  KEY (user_persona_id),
  CONSTRAINT FOREIGN KEY (cds_service_endpoint_id) REFERENCES cds_service_endpoint (id),
  CONSTRAINT FOREIGN KEY (created_by_id) REFERENCES user (id),
  CONSTRAINT FOREIGN KEY (sandbox_id) REFERENCES sandbox (id),
  CONSTRAINT FOREIGN KEY (user_persona_id) REFERENCES user_persona (id)
);

CREATE TABLE user_launch_cds_service_endpoint (
  id                 INT(11) NOT NULL AUTO_INCREMENT,
  last_launch        DATETIME         DEFAULT NULL,
  launch_scenario_cds_service_endpoint_id INT(11)          DEFAULT NULL,
  user_id            INT(11)          DEFAULT NULL,
  PRIMARY KEY (id),
  KEY (launch_scenario_cds_service_endpoint_id),
  KEY (user_id),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id),
  CONSTRAINT FOREIGN KEY (launch_scenario_cds_service_endpoint_id) REFERENCES launch_scenario_cds_service_endpoint (id)
);
