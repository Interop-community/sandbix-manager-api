# create the admin user

INSERT INTO user (created_timestamp, sbm_user_id, name)
VALUES ('2017-03-15 16:40:08', 'admin', 'Admin User');

INSERT INTO system_role (user_id, role)
VALUES
  ((SELECT id FROM user WHERE sbm_user_id='admin'), 0),
  ((SELECT id FROM user WHERE sbm_user_id='admin'), 2);
