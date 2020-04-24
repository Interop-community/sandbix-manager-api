SET @user_id := xxxx;

DELETE FROM user_launch WHERE user_id=@user_id;

DELETE FROM launch_scenario WHERE created_by_id=@user_id;

DELETE FROM app WHERE created_by_id=@user_id;

DELETE FROM notification WHERE user_id=@user_id;

DELETE FROM sandbox_activity_log WHERE user_id=@user_id;

DELETE FROM app WHERE sandbox_id IN (SELECT id FROM sandbox WHERE created_by_id=@user_id);

DELETE FROM sandbox_activity_log WHERE sandbox_id IN (SELECT id FROM sandbox WHERE created_by_id=@user_id);

DELETE FROM sandbox_invite WHERE  sandbox_id IN (SELECT id FROM sandbox WHERE created_by_id=@user_id);

DELETE FROM sandbox_user_roles WHERE  sandbox IN (SELECT id FROM sandbox WHERE created_by_id=@user_id);

DELETE FROM user_persona WHERE  sandbox_id IN (SELECT id FROM sandbox WHERE created_by_id=@user_id);

DELETE FROM user_sandbox WHERE  sandbox_id IN (SELECT id FROM sandbox WHERE created_by_id=@user_id);

DELETE FROM cds_service_endpoint WHERE sandbox_id IN (SELECT id FROM sandbox WHERE created_by_id=@user_id);

DELETE FROM fhir_profile WHERE fhir_profile_id IN (SELECT id FROM fhir_profile_detail WHERE created_by_id=@user_id);

DELETE FROM fhir_profile_detail WHERE sandbox_id IN (SELECT id FROM sandbox WHERE created_by_id=@user_id);

DELETE FROM fhir_profile_detail WHERE created_by_id=@user_id;

DELETE FROM sandbox WHERE created_by_id=@user_id;

DELETE FROM sandbox_invite WHERE invited_by_id=@user_id or invitee_id=@user_id;

DELETE FROM system_role WHERE user_id=@user_id;

DELETE FROM user_role WHERE user_id=@user_id;

DELETE FROM user_terms_of_use_acceptance WHERE user_id = @user_id;

DELETE FROM user WHERE id = @user_id;