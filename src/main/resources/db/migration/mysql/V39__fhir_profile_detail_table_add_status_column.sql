ALTER TABLE sandman.fhir_profile_detail ADD status varchar(15);
UPDATE sandman.fhir_profile_detail SET status = 'CREATED';