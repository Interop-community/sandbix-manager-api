# create the snapshot statistics

CREATE TABLE statistics (
  id                                  INT(11) NOT NULL AUTO_INCREMENT,
  created_timestamp                   TIMESTAMP NOT NULL,
  total_sandboxes_count               VARCHAR (11),
  total_dstu2_sandboxes_count         VARCHAR (11),
  total_stu3_sandboxes_count          VARCHAR (11),
  total_r4_sandboxes_count            VARCHAR (11),
  total_users_count                   VARCHAR (11),
  active_sandboxes_in_interval        VARCHAR (11),
  active_dstu2_sandboxes_in_interval  VARCHAR (11),
  active_stu3_sandboxes_in_interval   VARCHAR (11),
  active_r4_sandboxes_in_interval     VARCHAR (11),
  active_users_in_interval            VARCHAR (11),
  new_sandboxes_in_interval           VARCHAR (11),
  new_users_in_interval               VARCHAR (11),
  new_dstu2_sandboxes_in_interval     VARCHAR (11),
  new_stu3_sandboxes_in_interval      VARCHAR (11),
  new_r4_sandboxes_in_interval        VARCHAR (11),
  fhir_transactions                   VARCHAR (11),
  PRIMARY KEY (id)
)

