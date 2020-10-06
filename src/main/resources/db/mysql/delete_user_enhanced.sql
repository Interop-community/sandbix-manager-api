SET @user_id := xxxx;

DELETE FROM sandman.user_launch where user_id=@user_id;

DELETE FROM sandman.launch_scenario Where created_by_id=@user_id;

DELETE FROM sandman.app where created_by_id=@user_id;

DELETE FROM sandman.notification where user_id=@user_id;

DELETE FROM sandman.sandbox_activity_log where user_id=@user_id;

-- there could be issue with deleting the sandbox

DELETE FROM sandman.app where sandbox_id in (SELECT id FROM sandman.sandbox where created_by_id=@user_id);

DELETE FROM sandman.sandbox_activity_log where sandbox_id in (SELECT id FROM sandman.sandbox where created_by_id=@user_id);

DELETE FROM sandman.sandbox_invite where  sandbox_id in (SELECT id FROM sandman.sandbox where created_by_id=@user_id);

DELETE FROM sandman.sandbox_user_roles where  sandbox in (SELECT id FROM sandman.sandbox where created_by_id=@user_id);

DELETE FROM sandman.user_persona where  sandbox_id in (SELECT id FROM sandman.sandbox where created_by_id=@user_id);

DELETE FROM sandman.user_sandbox where  sandbox_id in (SELECT id FROM sandman.sandbox where created_by_id=@user_id);

DELETE FROM sandman.cds_service_endpoint WHERE sandbox_id IN (SELECT id FROM sandman.sandbox where created_by_id=@user_id);

DELETE FROM sandman.fhir_profile WHERE fhir_profile_id IN (SELECT id FROM sandman.fhir_profile_detail where created_by_id=@user_id);

DELETE FROM sandman.fhir_profile_detail WHERE sandbox_id IN (SELECT id FROM sandman.sandbox where created_by_id=@user_id);

DELETE FROM sandman.fhir_profile_detail WHERE created_by_id=@user_id;

DELETE FROM sandman.sandbox where created_by_id=@user_id;

DELETE FROM sandman.sandbox_invite where invited_by_id=@user_id or invitee_id=@user_id;

DELETE FROM sandman.system_role where user_id=@user_id;

DELETE FROM sandman.user_role where user_id=@user_id;

DELETE FROM sandman.user where id = @user_id;
