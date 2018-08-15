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

UPDATE smart_app SET sandbox_id='MasterStu3SMART' WHERE sandbox_id='TempStu3SMART';
UPDATE smart_app SET sandbox_id='MasterDstu2SMART' WHERE sandbox_id='TempDstu2SMART';
UPDATE smart_app SET sandbox_id='MasterEmpty' WHERE sandbox_id='TempEmpty';
UPDATE smart_app SET sandbox_id='MasterStu3Synthea' WHERE sandbox_id='TempStu3Synthea';
UPDATE smart_app SET sandbox_id='MasterR4SMART' WHERE sandbox_id='TempR4SMART';


DROP PROCEDURE IF EXISTS app_to_smart_app;
DELIMITER ;;
CREATE PROCEDURE app_to_smart_app()
BEGIN
DECLARE n INT DEFAULT 0;
DECLARE i INT DEFAULT 0;
DECLARE created_timestamp_2 DATETIME(3);
DECLARE created_by INT(11);
SELECT COUNT(*) INTO n FROM app;
SET i=0;
WHILE i<n DO
  IF (SELECT auth_database_id FROM auth_client WHERE id=(SELECT auth_client_id FROM app WHERE id=(SELECT id FROM app LIMIT i,1))) IS NOT NULL THEN

		IF (SELECT created_timestamp FROM app WHERE id=(SELECT id FROM app LIMIT i,1)) IS NOT NULL THEN
			SET created_timestamp_2 = (SELECT created_timestamp FROM app WHERE id=(SELECT id FROM app LIMIT i,1));
    ELSE
			SET created_timestamp_2 = (SELECT created_timestamp FROM sandbox WHERE id=(SELECT sandbox_id FROM app WHERE id=(SELECT id FROM app LIMIT i,1)));
    END IF;

		IF (SELECT created_by_id FROM app WHERE id=(SELECT id FROM app LIMIT i,1)) IS NOT NULL THEN
			SET created_by = (SELECT created_by_id FROM app WHERE id=(SELECT id FROM app LIMIT i,1));
    ELSE
			SET created_by = (SELECT created_by_id FROM sandbox WHERE id=(SELECT sandbox_id FROM app WHERE id=(SELECT id FROM app LIMIT i,1)));
    END IF;

    INSERT INTO smart_app (smart_app_id, sandbox_id, manifest_url, client_id, owner_id, created_timestamp, visibility, sample_patients, info, brief_description, author, copy_type,
                                  launch_url, logo_uri, client_name, fhir_versions, logo_id) VALUES (
      UUID(),
      (SELECT sandbox_id FROM sandbox WHERE id=(SELECT sandbox_id FROM app WHERE id=(SELECT id FROM app LIMIT i,1))),
      (SELECT app_manifest_uri FROM app WHERE id=(SELECT id FROM app LIMIT i,1)),
      (SELECT client_id FROM auth_client WHERE id=(SELECT auth_client_id FROM app WHERE id=(SELECT id FROM app LIMIT i,1))),
      created_by,
      created_timestamp_2,
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
CALL app_to_smart_app();

DROP PROCEDURE IF EXISTS AddDefaultAppsToAllSandboxes;
DELIMITER ;;
CREATE PROCEDURE AddDefaultAppsToAllSandboxes()
BEGIN
	DECLARE done BOOLEAN DEFAULT FALSE;

	DECLARE current_sandbox_id VARCHAR(255);
  DECLARE cur CURSOR FOR SELECT sandbox_id FROM sandbox;
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

	OPEN cur;
	testLoop: LOOP

		FETCH cur INTO current_sandbox_id;
			IF done THEN
				LEAVE testLoop;
			END IF;

      SET @SQLStmt = CONCAT('INSERT INTO sandman.smart_app
        (smart_app_id, manifest_url, client_id, owner_id, created_timestamp, visibility, sample_patients, info,
         brief_description, author, copy_type, sandbox_id, launch_url, logo_uri, client_name)
        VALUES
          (
            "hspc-bilirubin-risk-chart",
            "https://bilirubin-risk-chart.hspconsortium.org/.well-known/smart/manifest.json",
            "bilirubin_chart",
            (SELECT created_by_id
             FROM sandman.sandbox
             WHERE sandbox_id=\"', current_sandbox_id, '\"),
            NOW(),
            "PUBLIC",
            "Patient?_id=BILIBABY,SMART-1288992",
            "https://IntermountainHealthcare.org",
            "The HSPC Bilirubin Risk Chart is a sample app that demonstrates many of the features of the SMART on FHIR app launch specification and HL7 FHIR standard.",
            "Intermountain Healthcare",
            "REPLICA",\"',
            current_sandbox_id, '\",
            "https://bilirubin-risk-chart.hspconsortium.org/launch.html",
           "https://content.hspconsortium.org/images/bilirubin/logo/bilirubin.png",
            "Bilirubin Risk Chart"
          ),
          (
            "hspc-my-web-app",
            "https://content.hspconsortium.org/apps/my-web-app/.well-known/smart/manifest.json",
            "my_web_app",
            (SELECT created_by_id
             FROM sandman.sandbox
             WHERE sandbox_id=\"', current_sandbox_id, '\"),
            NOW(),
            "PUBLIC",
            "",
            "https://healthservices.atlassian.net/wiki/spaces/HSPC/pages/64159752/For+Developers",
            "Perform a SMART launch at http://localhost:8000/fhir-app/launch.html using the client: my_web_app.",
            "HSPC",
            "REPLICA",\"',
            current_sandbox_id, '\",
            "http://localhost:8000/fhir-app/launch.html",
            "https://content.hspconsortium.org/images/my-web-app/logo/my.png",
            "My Web App"
          ),
          (
            "cds-hooks-sandbox",
            "https://content.hspconsortium.org/apps/cds-hooks-sandbox/.well-known/smart/manifest.json",
            "48163c5e-88b5-4cb3-92d3-23b800caa927",
            (SELECT created_by_id
             FROM sandman.sandbox
             WHERE sandbox_id=\"', current_sandbox_id, '\"),
            NOW(),
            "PUBLIC",
            "",
            "http://cds-hooks.org/",
            "The CDS Hooks Sandbox is a tool that allows users to simulate the workflow of the CDS Hooks standard.",
            "CDS Hooks",
            "REPLICA",\"',
            current_sandbox_id, '\",
            "http://sandbox.cds-hooks.org/launch.html",
            "https://content.hspconsortium.org/images/cds-hooks-sandbox/logo/CdsHooks.png",
            "CDS Hooks Sandbox"
          );'
          );

      PREPARE Stmt FROM @SQLStmt;
      IF current_sandbox_id != 'hspc5' AND  current_sandbox_id != 'hspc6' AND current_sandbox_id != 'hspc7'
        AND current_sandbox_id != 'hspc' AND current_sandbox_id != 'hspc3' AND current_sandbox_id != 'hspc4' AND current_sandbox_id != 'hspc1'
        AND current_sandbox_id != 'MasterDstu2SMART' AND current_sandbox_id != 'MasterEmpty' AND current_sandbox_id != 'MasterR4SMART'
        AND current_sandbox_id != 'MasterStu3SMART' AND current_sandbox_id != 'MasterStu3Synthea' THEN
			  EXECUTE Stmt;
			END IF;
	END LOOP testLoop;
END;
;;
DELIMITER ;
CALL AddDefaultAppsToAllSandboxes();