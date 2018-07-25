# create the hspc5 sandbox

INSERT INTO sandbox (allow_open_access, created_timestamp, description, name, sandbox_id, api_endpoint_index, created_by_id, fhir_server_end_point, visibility)
VALUES
  ('', '2017-03-15 16:46:06', 'HSPC Development Sandbox 5', 'HSPC Sandbox 5', 'hspc5', '5', 1, null, 1);

INSERT INTO user_sandbox (user_id, sandbox_id)
VALUES
  (1, (SELECT id FROM sandbox WHERE sandbox_id='hspc5')),
  (1, (SELECT id FROM sandbox WHERE sandbox_id='hspc5'));

INSERT INTO user_role (role, user_id)
VALUES
  (0, (SELECT id FROM user WHERE sbm_user_id='admin')),
  (3, (SELECT id FROM user WHERE sbm_user_id='admin')),
  (4, (SELECT id FROM user WHERE sbm_user_id='admin'));

INSERT INTO sandbox_user_roles (sandbox, user_roles)
VALUES
  ((SELECT id FROM sandbox WHERE sandbox_id='hspc5'), (SELECT (MAX(id) - 2) FROM user_role)),
  ((SELECT id FROM sandbox WHERE sandbox_id='hspc5'), (SELECT (MAX(id) - 1) FROM user_role)),
  ((SELECT id FROM sandbox WHERE sandbox_id='hspc5'), (SELECT MAX(id) FROM user_role));
