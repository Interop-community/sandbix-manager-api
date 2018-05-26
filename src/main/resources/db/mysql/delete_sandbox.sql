SET @sandbox_id := (SELECT id FROM sandbox WHERE name = 'travistest2');

DELETE FROM sandbox_user_roles WHERE sandbox = @sandbox_id;

DELETE FROM user_sandbox WHERE sandbox_id = @sandbox_id;

DELETE FROM sandbox_activity_log WHERE sandbox_id = @sandbox_id;

DELETE FROM launch_scenario WHERE sandbox_id = @sandbox_id;

DELETE FROM patient WHERE sandbox_id = @sandbox_id;

DELETE FROM app WHERE sandbox_id = @sandbox_id;

DELETE FROM user_persona WHERE sandbox_id = @sandbox_id;

DELETE FROM sandbox_invite WHERE sandbox_id = @sandbox_id;

DELETE FROM sandbox WHERE id = @sandbox_id;
