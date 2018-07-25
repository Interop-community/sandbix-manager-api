# create the hspc1 sandbox

INSERT INTO sandbox (allow_open_access, created_timestamp, description, name, sandbox_id, schema_version, created_by_id, fhir_server_end_point, visibility)
VALUES
  ('', '2017-03-15 16:46:06', 'HSPC Development Sandbox v1', 'HSPC Sandbox v1', 'hspc1', '1', 1, null, 1);

INSERT INTO user_sandbox (user_id, sandbox_id)
VALUES
  (1, (SELECT id FROM sandbox WHERE sandbox_id='hspc1')),
  (1, (SELECT id FROM sandbox WHERE sandbox_id='hspc1'));

INSERT INTO user_role (id, role, user_id)
VALUES
  (1, 0, (SELECT id FROM user WHERE sbm_user_id='admin')),
  (2, 3, (SELECT id FROM user WHERE sbm_user_id='admin')),
  (3, 4, (SELECT id FROM user WHERE sbm_user_id='admin'));

INSERT INTO sandbox_user_roles (sandbox, user_roles)
VALUES
  ((SELECT id FROM sandbox WHERE sandbox_id='hspc1'), 1),
  ((SELECT id FROM sandbox WHERE sandbox_id='hspc1'), 2),
  ((SELECT id FROM sandbox WHERE sandbox_id='hspc1'), 3);
