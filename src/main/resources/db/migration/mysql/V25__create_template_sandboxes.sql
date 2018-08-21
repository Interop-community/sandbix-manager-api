# Empty Dstu2 template
INSERT INTO sandbox (allow_open_access, created_timestamp, description, name, sandbox_id, api_endpoint_index, created_by_id, fhir_server_end_point, visibility)
VALUES
  (0, '2018-08-15 16:46:06', 'Template Dstu2 Sandbox with no sample data', 'Master Dstu2 Empty', 'MasterDstu2Empty', '5', 1, null, 1);

INSERT INTO user_sandbox (user_id, sandbox_id)
VALUES
  ((SELECT id FROM user WHERE sbm_user_id='admin'), (SELECT id FROM sandbox WHERE sandbox_id='MasterDstu2Empty'));

INSERT INTO user_role (role, user_id)
VALUES
  (0, (SELECT id FROM user WHERE sbm_user_id='admin')),
  (3, (SELECT id FROM user WHERE sbm_user_id='admin')),
  (4, (SELECT id FROM user WHERE sbm_user_id='admin'));

INSERT INTO sandbox_user_roles (sandbox, user_roles)
VALUES
  ((SELECT id FROM sandbox WHERE sandbox_id='MasterDstu2Empty'), (SELECT (MAX(id) - 2) FROM user_role)),
  ((SELECT id FROM sandbox WHERE sandbox_id='MasterDstu2Empty'), (SELECT (MAX(id) - 1) FROM user_role)),
  ((SELECT id FROM sandbox WHERE sandbox_id='MasterDstu2Empty'), (SELECT MAX(id) FROM user_role));

# Empty Stu3 template
INSERT INTO sandbox (allow_open_access, created_timestamp, description, name, sandbox_id, api_endpoint_index, created_by_id, fhir_server_end_point, visibility)
VALUES
  (0, '2018-08-15 16:46:06', 'Template Stu3 Sandbox with no sample data', 'Master Stu3 Empty', 'MasterStu3Empty', '6', 1, null, 1);

INSERT INTO user_sandbox (user_id, sandbox_id)
VALUES
  ((SELECT id FROM user WHERE sbm_user_id='admin'), (SELECT id FROM sandbox WHERE sandbox_id='MasterStu3Empty'));

INSERT INTO user_role (role, user_id)
VALUES
  (0, (SELECT id FROM user WHERE sbm_user_id='admin')),
  (3, (SELECT id FROM user WHERE sbm_user_id='admin')),
  (4, (SELECT id FROM user WHERE sbm_user_id='admin'));

INSERT INTO sandbox_user_roles (sandbox, user_roles)
VALUES
  ((SELECT id FROM sandbox WHERE sandbox_id='MasterStu3Empty'), (SELECT (MAX(id) - 2) FROM user_role)),
  ((SELECT id FROM sandbox WHERE sandbox_id='MasterStu3Empty'), (SELECT (MAX(id) - 1) FROM user_role)),
  ((SELECT id FROM sandbox WHERE sandbox_id='MasterStu3Empty'), (SELECT MAX(id) FROM user_role));

# Empty R4 template
INSERT INTO sandbox (allow_open_access, created_timestamp, description, name, sandbox_id, api_endpoint_index, created_by_id, fhir_server_end_point, visibility)
VALUES
  (0, '2018-08-15 16:46:06', 'Template R4 Sandbox with no sample data', 'Master R4 Empty', 'MasterR4Empty', '7', 1, null, 1);

INSERT INTO user_sandbox (user_id, sandbox_id)
VALUES
  ((SELECT id FROM user WHERE sbm_user_id='admin'), (SELECT id FROM sandbox WHERE sandbox_id='MasterR4Empty'));

INSERT INTO user_role (role, user_id)
VALUES
  (0, (SELECT id FROM user WHERE sbm_user_id='admin')),
  (3, (SELECT id FROM user WHERE sbm_user_id='admin')),
  (4, (SELECT id FROM user WHERE sbm_user_id='admin'));

INSERT INTO sandbox_user_roles (sandbox, user_roles)
VALUES
  ((SELECT id FROM sandbox WHERE sandbox_id='MasterR4Empty'), (SELECT (MAX(id) - 2) FROM user_role)),
  ((SELECT id FROM sandbox WHERE sandbox_id='MasterR4Empty'), (SELECT (MAX(id) - 1) FROM user_role)),
  ((SELECT id FROM sandbox WHERE sandbox_id='MasterR4Empty'), (SELECT MAX(id) FROM user_role));

# Dstu2 Smart template
INSERT INTO sandbox (allow_open_access, created_timestamp, description, name, sandbox_id, api_endpoint_index, created_by_id, fhir_server_end_point, visibility)
VALUES
  (0, '2018-08-15 16:46:06', 'Template Dstu2 Sandbox with SMART sample data set', 'Master Dstu2 Smart', 'MasterDstu2Smart', '5', 1, null, 1);

INSERT INTO user_sandbox (user_id, sandbox_id)
VALUES
  ((SELECT id FROM user WHERE sbm_user_id='admin'), (SELECT id FROM sandbox WHERE sandbox_id='MasterDstu2Smart'));

INSERT INTO user_role (role, user_id)
VALUES
  (0, (SELECT id FROM user WHERE sbm_user_id='admin')),
  (3, (SELECT id FROM user WHERE sbm_user_id='admin')),
  (4, (SELECT id FROM user WHERE sbm_user_id='admin'));

INSERT INTO sandbox_user_roles (sandbox, user_roles)
VALUES
  ((SELECT id FROM sandbox WHERE sandbox_id='MasterDstu2Smart'), (SELECT (MAX(id) - 2) FROM user_role)),
  ((SELECT id FROM sandbox WHERE sandbox_id='MasterDstu2Smart'), (SELECT (MAX(id) - 1) FROM user_role)),
  ((SELECT id FROM sandbox WHERE sandbox_id='MasterDstu2Smart'), (SELECT MAX(id) FROM user_role));

# Stu3 Smart template
INSERT INTO sandbox (allow_open_access, created_timestamp, description, name, sandbox_id, api_endpoint_index, created_by_id, fhir_server_end_point, visibility)
VALUES
  (0, '2018-08-15 16:46:06', 'Template Stu3 Sandbox with SMART sample data set', 'Master Stu3 Smart', 'MasterStu3Smart', '6', 1, null, 1);

INSERT INTO user_sandbox (user_id, sandbox_id)
VALUES
  ((SELECT id FROM user WHERE sbm_user_id='admin'), (SELECT id FROM sandbox WHERE sandbox_id='MasterStu3Smart'));

INSERT INTO user_role (role, user_id)
VALUES
  (0, (SELECT id FROM user WHERE sbm_user_id='admin')),
  (3, (SELECT id FROM user WHERE sbm_user_id='admin')),
  (4, (SELECT id FROM user WHERE sbm_user_id='admin'));

INSERT INTO sandbox_user_roles (sandbox, user_roles)
VALUES
  ((SELECT id FROM sandbox WHERE sandbox_id='MasterStu3Smart'), (SELECT (MAX(id) - 2) FROM user_role)),
  ((SELECT id FROM sandbox WHERE sandbox_id='MasterStu3Smart'), (SELECT (MAX(id) - 1) FROM user_role)),
  ((SELECT id FROM sandbox WHERE sandbox_id='MasterStu3Smart'), (SELECT MAX(id) FROM user_role));

# R4 Smart template
INSERT INTO sandbox (allow_open_access, created_timestamp, description, name, sandbox_id, api_endpoint_index, created_by_id, fhir_server_end_point, visibility)
VALUES
  (0, '2018-08-15 16:46:06', 'Template R4 Sandbox with SMART sample data set', 'Master R4 Smart', 'MasterR4Smart', '7', 1, null, 1);

INSERT INTO user_sandbox (user_id, sandbox_id)
VALUES
  ((SELECT id FROM user WHERE sbm_user_id='admin'), (SELECT id FROM sandbox WHERE sandbox_id='MasterR4Smart'));

INSERT INTO user_role (role, user_id)
VALUES
  (0, (SELECT id FROM user WHERE sbm_user_id='admin')),
  (3, (SELECT id FROM user WHERE sbm_user_id='admin')),
  (4, (SELECT id FROM user WHERE sbm_user_id='admin'));

INSERT INTO sandbox_user_roles (sandbox, user_roles)
VALUES
  ((SELECT id FROM sandbox WHERE sandbox_id='MasterR4Smart'), (SELECT (MAX(id) - 2) FROM user_role)),
  ((SELECT id FROM sandbox WHERE sandbox_id='MasterR4Smart'), (SELECT (MAX(id) - 1) FROM user_role)),
  ((SELECT id FROM sandbox WHERE sandbox_id='MasterR4Smart'), (SELECT MAX(id) FROM user_role));