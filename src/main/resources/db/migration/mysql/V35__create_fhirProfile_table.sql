#create fhir_profile table
CREATE TABLE fhir_profile_detail (
  id                    INT(11) NOT NULL AUTO_INCREMENT,
  profile_name          VARCHAR(255)     DEFAULT NULL,
  profile_id            VARCHAR(255)     DEFAULT NULL,
  sandbox_id            INT(11)          DEFAULT NULL,
  created_by_id         INT(11)          DEFAULT NULL,
  created_timestamp     DATETIME         DEFAULT NULL,
  visibility            INT(11)          DEFAULT NULL,
  PRIMARY KEY (id),
  KEY (created_by_id),
  KEY (sandbox_id),
  CONSTRAINT FOREIGN KEY (created_by_id) REFERENCES user (id),
  CONSTRAINT FOREIGN KEY (sandbox_id) REFERENCES sandbox (id)
);

CREATE TABLE fhir_profile (
  id                    INT(11) NOT NULL AUTO_INCREMENT,
  fhir_profile_id       INT(11)          DEFAULT NULL,
  full_url              VARCHAR(255)     DEFAULT NULL,
  relative_url          VARCHAR(255)     DEFAULT NULL,
  profile_type          VARCHAR(255)     DEFAULT NULL,
  PRIMARY KEY (id),
  KEY (fhir_profile_id),
  CONSTRAINT FOREIGN KEY (fhir_profile_id) REFERENCES fhir_profile_detail (id)
);