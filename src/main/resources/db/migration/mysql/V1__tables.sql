# Create tables

CREATE TABLE config (
  id          INT(11) NOT NULL AUTO_INCREMENT,
  config_type INT(11)          DEFAULT NULL,
  key_name    VARCHAR(255)     DEFAULT NULL,
  value       VARCHAR(255)     DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE user (
  id                INT(11) NOT NULL AUTO_INCREMENT,
  created_timestamp DATETIME         DEFAULT NULL,
  email             VARCHAR(255)     DEFAULT NULL,
  name              VARCHAR(255)     DEFAULT NULL,
  sbm_user_id       VARCHAR(255)     DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE sandbox (
  id                    INT(11) NOT NULL AUTO_INCREMENT,
  allow_open_access     BIT(1)  NOT NULL,
  created_timestamp     DATETIME         DEFAULT NULL,
  description           VARCHAR(255)     DEFAULT NULL,
  fhir_server_end_point VARCHAR(255)     DEFAULT NULL,
  name                  VARCHAR(255)     DEFAULT NULL,
  sandbox_id            VARCHAR(255)     DEFAULT NULL,
  schema_version        VARCHAR(255)     DEFAULT NULL,
  visibility            INT(11)          DEFAULT NULL,
  created_by_id         INT(11)          DEFAULT NULL,
  PRIMARY KEY (id),
  KEY (created_by_id),
  CONSTRAINT FOREIGN KEY (created_by_id) REFERENCES user (id)
);

CREATE TABLE image (
  id           INT(11) NOT NULL AUTO_INCREMENT,
  bytes        LONGBLOB,
  content_type VARCHAR(255)     DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE auth_client (
  id               INT(11) NOT NULL AUTO_INCREMENT,
  auth_database_id INT(11)          DEFAULT NULL,
  client_id        VARCHAR(255)     DEFAULT NULL,
  client_name      VARCHAR(255)     DEFAULT NULL,
  logo_uri         VARCHAR(255)     DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE app (
  id                INT(11) NOT NULL AUTO_INCREMENT,
  app_manifest_uri  VARCHAR(255)     DEFAULT NULL,
  created_timestamp DATETIME         DEFAULT NULL,
  fhir_versions     VARCHAR(255)     DEFAULT NULL,
  launch_uri        VARCHAR(255)     DEFAULT NULL,
  logo_uri          VARCHAR(255)     DEFAULT NULL,
  sample_patients   VARCHAR(255)     DEFAULT NULL,
  software_id       VARCHAR(255)     DEFAULT NULL,
  visibility        INT(11)          DEFAULT NULL,
  auth_client_id    INT(11)          DEFAULT NULL,
  created_by_id     INT(11)          DEFAULT NULL,
  logo_id           INT(11)          DEFAULT NULL,
  sandbox_id        INT(11)          DEFAULT NULL,
  PRIMARY KEY (id),
  KEY (auth_client_id),
  KEY (created_by_id),
  KEY (logo_id),
  KEY (sandbox_id),
  CONSTRAINT FOREIGN KEY (sandbox_id) REFERENCES sandbox (id),
  CONSTRAINT FOREIGN KEY (logo_id) REFERENCES image (id),
  CONSTRAINT FOREIGN KEY (created_by_id) REFERENCES user (id),
  CONSTRAINT FOREIGN KEY (auth_client_id) REFERENCES auth_client (id)
);

CREATE TABLE context_params (
  id    INT(11) NOT NULL AUTO_INCREMENT,
  name  VARCHAR(255)     DEFAULT NULL,
  value VARCHAR(255)     DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE user_persona (
  id                INT(11) NOT NULL AUTO_INCREMENT,
  created_timestamp DATETIME         DEFAULT NULL,
  fhir_id           VARCHAR(255)     DEFAULT NULL,
  fhir_name         VARCHAR(255)     DEFAULT NULL,
  password          VARCHAR(255)     DEFAULT NULL,
  persona_name      VARCHAR(255)     DEFAULT NULL,
  persona_user_id   VARCHAR(255)     DEFAULT NULL,
  resource          VARCHAR(255)     DEFAULT NULL,
  resource_url      VARCHAR(255)     DEFAULT NULL,
  visibility        INT(11)          DEFAULT NULL,
  created_by_id     INT(11)          DEFAULT NULL,
  sandbox_id        INT(11)          DEFAULT NULL,
  PRIMARY KEY (id),
  KEY (created_by_id),
  KEY (sandbox_id),
  CONSTRAINT FOREIGN KEY (created_by_id) REFERENCES user (id),
  CONSTRAINT FOREIGN KEY (sandbox_id) REFERENCES sandbox (id)
);

CREATE TABLE patient (
  id         INT(11) NOT NULL AUTO_INCREMENT,
  fhir_id    VARCHAR(255)     DEFAULT NULL,
  name       VARCHAR(255)     DEFAULT NULL,
  resource   VARCHAR(255)     DEFAULT NULL,
  sandbox_id INT(11)          DEFAULT NULL,
  PRIMARY KEY (id),
  KEY (sandbox_id),
  CONSTRAINT FOREIGN KEY (sandbox_id) REFERENCES sandbox (id)
);

CREATE TABLE launch_scenario (
  id                INT(11) NOT NULL AUTO_INCREMENT,
  created_timestamp DATETIME         DEFAULT NULL,
  description       VARCHAR(255)     DEFAULT NULL,
  last_launch       DATETIME         DEFAULT NULL,
  visibility        INT(11)          DEFAULT NULL,
  app_id            INT(11)          DEFAULT NULL,
  created_by_id     INT(11)          DEFAULT NULL,
  patient_id        INT(11)          DEFAULT NULL,
  sandbox_id        INT(11)          DEFAULT NULL,
  user_persona_id   INT(11)          DEFAULT NULL,
  PRIMARY KEY (id),
  KEY (app_id),
  KEY (created_by_id),
  KEY (patient_id),
  KEY (sandbox_id),
  KEY (user_persona_id),
  CONSTRAINT FOREIGN KEY (user_persona_id) REFERENCES user_persona (id),
  CONSTRAINT FOREIGN KEY (patient_id) REFERENCES patient (id),
  CONSTRAINT FOREIGN KEY (created_by_id) REFERENCES user (id),
  CONSTRAINT FOREIGN KEY (sandbox_id) REFERENCES sandbox (id),
  CONSTRAINT FOREIGN KEY (app_id) REFERENCES app (id)
);

CREATE TABLE launch_scenario_context_params (
  launch_scenario_id INT(11) NOT NULL,
  context_params_id  INT(11) NOT NULL,
  UNIQUE KEY (context_params_id),
  KEY (launch_scenario_id),
  CONSTRAINT FOREIGN KEY (launch_scenario_id) REFERENCES launch_scenario (id),
  CONSTRAINT FOREIGN KEY (context_params_id) REFERENCES context_params (id)
);

CREATE TABLE sandbox_activity_log (
  id              INT(11) NOT NULL AUTO_INCREMENT,
  activity        INT(11)          DEFAULT NULL,
  additional_info LONGTEXT,
  timestamp       DATETIME         DEFAULT NULL,
  sandbox_id      INT(11)          DEFAULT NULL,
  user_id         INT(11)          DEFAULT NULL,
  PRIMARY KEY (id),
  KEY (sandbox_id),
  KEY (user_id),
  CONSTRAINT FOREIGN KEY (sandbox_id) REFERENCES sandbox (id),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id)
);

CREATE TABLE sandbox_import (
  id               INT(11) NOT NULL AUTO_INCREMENT,
  duration_seconds VARCHAR(255)     DEFAULT NULL,
  failure_count    VARCHAR(255)     DEFAULT NULL,
  import_fhir_url  VARCHAR(255)     DEFAULT NULL,
  success_count    VARCHAR(255)     DEFAULT NULL,
  timestamp        DATETIME         DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE sandbox_imports (
  sandbox_id INT(11) NOT NULL,
  imports_id INT(11) NOT NULL,
  UNIQUE KEY (imports_id),
  KEY (sandbox_id),
  CONSTRAINT FOREIGN KEY (sandbox_id) REFERENCES sandbox (id),
  CONSTRAINT FOREIGN KEY (imports_id) REFERENCES sandbox_import (id)
);

CREATE TABLE sandbox_invite (
  id               INT(11) NOT NULL AUTO_INCREMENT,
  invite_timestamp DATETIME         DEFAULT NULL,
  status           INT(11)          DEFAULT NULL,
  invited_by_id    INT(11)          DEFAULT NULL,
  invitee_id       INT(11)          DEFAULT NULL,
  sandbox_id       INT(11)          DEFAULT NULL,
  PRIMARY KEY (id),
  KEY (invited_by_id),
  KEY (invitee_id),
  KEY (sandbox_id),
  CONSTRAINT FOREIGN KEY (invitee_id) REFERENCES user (id),
  CONSTRAINT FOREIGN KEY (sandbox_id) REFERENCES sandbox (id),
  CONSTRAINT FOREIGN KEY (invited_by_id) REFERENCES user (id)
);

CREATE TABLE user_role (
  id      INT(11) NOT NULL AUTO_INCREMENT,
  role    INT(11)          DEFAULT NULL,
  user_id INT(11)          DEFAULT NULL,
  PRIMARY KEY (id),
  KEY (user_id),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id)
);

CREATE TABLE sandbox_user_roles (
  sandbox    INT(11) NOT NULL,
  user_roles INT(11) NOT NULL,
  UNIQUE KEY (user_roles),
  KEY (sandbox),
  CONSTRAINT FOREIGN KEY (sandbox) REFERENCES sandbox (id),
  CONSTRAINT FOREIGN KEY (user_roles) REFERENCES user_role (id)
);

CREATE TABLE system_role (
  user_id INT(11) NOT NULL,
  role    INT(11) NOT NULL,
  PRIMARY KEY (user_id, role),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id)
);

CREATE TABLE terms_of_use (
  id                INT(11) NOT NULL AUTO_INCREMENT,
  created_timestamp DATETIME         DEFAULT NULL,
  value             LONGTEXT,
  PRIMARY KEY (id)
);

CREATE TABLE terms_of_use_acceptance (
  id                 INT(11) NOT NULL AUTO_INCREMENT,
  accepted_timestamp DATETIME         DEFAULT NULL,
  terms_of_use_id    INT(11)          DEFAULT NULL,
  PRIMARY KEY (id),
  KEY (terms_of_use_id),
  CONSTRAINT FOREIGN KEY (terms_of_use_id) REFERENCES terms_of_use (id)
);

CREATE TABLE user_launch (
  id                 INT(11) NOT NULL AUTO_INCREMENT,
  last_launch        DATETIME         DEFAULT NULL,
  launch_scenario_id INT(11)          DEFAULT NULL,
  user_id            INT(11)          DEFAULT NULL,
  PRIMARY KEY (id),
  KEY (launch_scenario_id),
  KEY (user_id),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id),
  CONSTRAINT FOREIGN KEY (launch_scenario_id) REFERENCES launch_scenario (id)
);

CREATE TABLE user_sandbox (
  user_id    INT(11) NOT NULL,
  sandbox_id INT(11) NOT NULL,
  KEY (sandbox_id),
  KEY (user_id),
  CONSTRAINT FOREIGN KEY (sandbox_id) REFERENCES sandbox (id),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id)
);

CREATE TABLE user_terms_of_use_acceptance (
  user_id                     INT(11) NOT NULL,
  terms_of_use_acceptances_id INT(11) NOT NULL,
  UNIQUE KEY (terms_of_use_acceptances_id),
  KEY (user_id),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id),
  CONSTRAINT FOREIGN KEY (terms_of_use_acceptances_id) REFERENCES terms_of_use_acceptance (id)
);
