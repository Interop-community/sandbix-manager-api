# create the snapshot statistics

CREATE TABLE statistics (
  id                            INT(11) NOT NULL AUTO_INCREMENT,
  created_timestamp             TIMESTAMP NOT NULL,
  full_sandbox_count            VARCHAR (11),
  full_dstu2_count              VARCHAR (11),
  full_stu3_count               VARCHAR (11),
  full_r4_count                 VARCHAR (11),
  active_sandboxes_in_interval    VARCHAR (11),
  new_sandboxes_in_interval       VARCHAR (11),
  dstu2_sandboxes_in_interval     VARCHAR (11),
  stu3_sandboxes_in_interval      VARCHAR (11),
  r4_sandboxes_in_interval        VARCHAR (11),
  full_user_count               VARCHAR (11),
  active_user_in_interval       VARCHAR (11),
  new_users_in_interval          VARCHAR (11),
  fhir_transactions               varchar (11),
  PRIMARY KEY (id)
)

