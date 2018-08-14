ALTER TABLE smart_app ADD logo_id INT(11) DEFAULT NULL;
ALTER TABLE smart_app ADD launch_url VARCHAR(255) DEFAULT NULL;
ALTER TABLE smart_app ADD logo_uri VARCHAR(255) DEFAULT NULL;
ALTER TABLE smart_app ADD client_name VARCHAR(255) DEFAULT NULL;
ALTER TABLE smart_app ADD fhir_versions VARCHAR(255) DEFAULT NULL;
ALTER TABLE smart_app ADD client_uri VARCHAR(255) DEFAULT NULL;
ALTER TABLE smart_app DROP manifest;

UPDATE smart_app SET logo_uri='https://content.hspconsortium.org/images/bilirubin/logo/bilirubin.png' WHERE client_id='bilirubin_chart';
UPDATE smart_app SET logo_uri='https://content.hspconsortium.org/images/my-web-app/logo/my.png' WHERE client_id='my_web_app';
UPDATE smart_app SET logo_uri='https://content.hspconsortium.org/images/cds-hooks-sandbox/logo/CdsHooks.png' WHERE client_id='48163c5e-88b5-4cb3-92d3-23b800caa927';

UPDATE smart_app SET launch_url='https://bilirubin-risk-chart.hspconsortium.org/launch.html' WHERE client_id='bilirubin_chart';
UPDATE smart_app SET launch_url='http://localhost:8000/fhir-app/launch.html' WHERE client_id='my_web_app';
UPDATE smart_app SET launch_url='http://sandbox.cds-hooks.org/launch.html' WHERE client_id='48163c5e-88b5-4cb3-92d3-23b800caa927';

UPDATE smart_app SET client_name='Bilirubin Risk Chart' WHERE client_id='bilirubin_chart';
UPDATE smart_app SET client_name='My Web App' WHERE client_id='my_web_app';
UPDATE smart_app SET client_name='CDS Hooks Sandbox' WHERE client_id='48163c5e-88b5-4cb3-92d3-23b800caa927';

-- UPDATE smart_app SET sandbox_id='MasterStu3SMART' WHERE sandbox_id='TempStu3SMART';
-- UPDATE smart_app SET sandbox_id='MasterDstu2SMART' WHERE sandbox_id='TempDstu2SMART';
-- UPDATE smart_app SET sandbox_id='MasterEmpty' WHERE sandbox_id='TempEmpty';
-- UPDATE smart_app SET sandbox_id='MasterStu3Synthea' WHERE sandbox_id='TempStu3Synthea';
-- UPDATE smart_app SET sandbox_id='MasterR4SMART' WHERE sandbox_id='TempR4SMART';

DROP FUNCTION IF EXISTS FindCreatedById;
DELIMITER //

CREATE FUNCTION FindCreatedById(i INT)
RETURNS INT(11)
BEGIN
	DECLARE created_by INT(11);
		IF (SELECT created_by_id FROM app WHERE id=(SELECT id FROM app LIMIT i,1)) IS NOT NULL THEN
			SET created_by = (SELECT created_by_id FROM app WHERE id=(SELECT id FROM app LIMIT i,1));
        ELSE
			SET created_by = (SELECT created_by_id FROM sandbox WHERE id=(SELECT sandbox_id FROM app WHERE id=(SELECT id FROM app LIMIT i,1)));
        END IF;
        RETURN created_by;
END; //
DELIMITER ;

DROP FUNCTION IF EXISTS FindCreatedTimeStamp;
DELIMITER -/

CREATE FUNCTION FindCreatedTimeStamp(i INT)
RETURNS DATETIME(3)
BEGIN
	DECLARE created_timestamp_2 DATETIME(3);
		IF (SELECT created_timestamp FROM app WHERE id=(SELECT id FROM app LIMIT i,1)) IS NOT NULL THEN
			SET created_timestamp_2 = (SELECT created_timestamp FROM app WHERE id=(SELECT id FROM app LIMIT i,1));
        ELSE
			SET created_timestamp_2 = (SELECT created_timestamp FROM sandbox WHERE id=(SELECT sandbox_id FROM app WHERE id=(SELECT id FROM app LIMIT i,1)));
    END IF;
    RETURN created_timestamp_2;
END;
-/
DELIMITER ;

DROP PROCEDURE IF EXISTS ROWPERROW;
DELIMITER ;;
CREATE PROCEDURE ROWPERROW()
BEGIN
DECLARE n INT DEFAULT 0;
DECLARE i INT DEFAULT 0;
SELECT COUNT(*) INTO n FROM app;
SET i=0;
WHILE i<n DO
  IF (SELECT auth_database_id FROM auth_client WHERE id=(SELECT auth_client_id FROM app WHERE id=(SELECT id FROM app LIMIT i,1))) IS NOT NULL THEN
    INSERT INTO smart_app (smart_app_id, sandbox_id, manifest_url, client_id, owner_id, created_timestamp, visibility, sample_patients, info, brief_description, author, copy_type,
                                  launch_url, logo_uri, client_name, fhir_versions, logo_id) VALUES (
      UUID(),
      (SELECT sandbox_id FROM sandbox WHERE id=(SELECT sandbox_id FROM app WHERE id=(SELECT id FROM app LIMIT i,1))),
      (SELECT app_manifest_uri FROM app WHERE id=(SELECT id FROM app LIMIT i,1)),
      (SELECT client_id FROM auth_client WHERE id=(SELECT auth_client_id FROM app WHERE id=(SELECT id FROM app LIMIT i,1))),
      FindCreatedById(i),
      FindCreatedTimeStamp(i),
      "PRIVATE",
      (SELECT sample_patients FROM app WHERE id=(SELECT id FROM app LIMIT i,1)),
      null,
      (SELECT brief_description FROM app WHERE id=(SELECT id FROM app LIMIT i,1)),
      null,
      "MASTER",
          (SELECT launch_uri FROM app WHERE id=(SELECT id FROM app LIMIT i,1)),
      (SELECT COALESCE(logo_uri, null) FROM app WHERE id=(SELECT id FROM app LIMIT i,1)),
      (SELECT client_name FROM auth_client WHERE id=(SELECT auth_client_id FROM app WHERE id=(SELECT id FROM app LIMIT i,1))),
          (SELECT fhir_versions FROM app WHERE id=(SELECT id FROM app LIMIT i,1)),
          (SELECT logo_id FROM app WHERE id=(SELECT id FROM app LIMIT i,1))

    );
  END IF;
  SET i = i + 1;
END WHILE;
End;
;;
DELIMITER ;
CALL ROWPERROW();