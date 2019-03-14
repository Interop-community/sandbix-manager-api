# create the hspc9 sandbox

INSERT INTO sandbox (allow_open_access, created_timestamp, description, name, sandbox_id, api_endpoint_index, created_by_id, fhir_server_end_point, visibility)
VALUES
  ('', '2017-03-09 19:49:09', 'HSPC Development Sandbox 9', 'HSPC Sandbox 9', 'hspc9', '9', 1, null, 1);

INSERT INTO user_sandbox (user_id, sandbox_id)
VALUES
  (1, (SELECT id FROM sandbox WHERE sandbox_id='hspc9')),
  (1, (SELECT id FROM sandbox WHERE sandbox_id='hspc9'));

INSERT INTO user_role (role, user_id)
VALUES
  (0, (SELECT id FROM user WHERE sbm_user_id='admin')),
  (3, (SELECT id FROM user WHERE sbm_user_id='admin')),
  (4, (SELECT id FROM user WHERE sbm_user_id='admin'));

INSERT INTO sandbox_user_roles (sandbox, user_roles)
VALUES
  ((SELECT id FROM sandbox WHERE sandbox_id='hspc9'), (SELECT (MAX(id) - 2) FROM user_role)),
  ((SELECT id FROM sandbox WHERE sandbox_id='hspc9'), (SELECT (MAX(id) - 1) FROM user_role)),
  ((SELECT id FROM sandbox WHERE sandbox_id='hspc9'), (SELECT MAX(id) FROM user_role));
