ALTER TABLE app ADD manifest_url VARCHAR(255) DEFAULT NULL;
ALTER TABLE app ADD client_id VARCHAR(255) DEFAULT NULL;
ALTER TABLE app ADD client_name VARCHAR(255) NOT NULL;
ALTER TABLE app ADD client_uri VARCHAR(255) DEFAULT NULL;
ALTER TABLE app ADD copy_type VARCHAR(36) DEFAULT 'MASTER';
ALTER TABLE app ADD custom_app BIT(1) DEFAULT 0;
UPDATE app SET manifest_url=app_manifest_uri;
UPDATE app INNER JOIN auth_client ON app.auth_client_id = auth_client.id SET app.client_id = auth_client.client_id;
UPDATE app INNER JOIN auth_client ON app.auth_client_id = auth_client.id SET app.client_name = auth_client.client_name;
UPDATE app INNER JOIN auth_client ON app.auth_client_id = auth_client.id SET app.custom_app = 1 WHERE auth_client.auth_database_id IS NULL;

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

      SET @SQLStmt = CONCAT('INSERT INTO sandman.app
        (manifest_url, client_id, created_by_id, created_timestamp, visibility, sample_patients, info,
         brief_description, author, copy_type, sandbox_id, launch_uri, logo_uri, client_name)
        VALUES
          (
            "https://bilirubin-risk-chart.hspconsortium.org/.well-known/smart/manifest.json",
            "bilirubin_chart",
            (SELECT created_by_id FROM sandman.sandbox WHERE sandbox_id=\"', current_sandbox_id, '\"),
            NOW(),
            1,
            "Patient?_id=BILIBABY,SMART-1288992",
            "https://IntermountainHealthcare.org",
            "The HSPC Bilirubin Risk Chart is a sample app that demonstrates many of the features of the SMART on FHIR app launch specification and HL7 FHIR standard.",
            "Intermountain Healthcare",
            "REPLICA",
            (SELECT id FROM sandman.sandbox WHERE sandbox_id=\"', current_sandbox_id, '\"),
            "https://bilirubin-risk-chart.hspconsortium.org/launch.html",
           "https://content.hspconsortium.org/images/bilirubin/logo/bilirubin.png",
            "Bilirubin Risk Chart"
          ),
          (
            "https://content.hspconsortium.org/apps/my-web-app/.well-known/smart/manifest.json",
            "my_web_app",
            (SELECT created_by_id FROM sandman.sandbox WHERE sandbox_id=\"', current_sandbox_id, '\"),
            NOW(),
            1,
            null,
            "https://healthservices.atlassian.net/wiki/spaces/HSPC/pages/64159752/For+Developers",
            "Perform a SMART launch at http://localhost:8000/fhir-app/launch.html using the client: my_web_app.",
            "HSPC",
            "REPLICA",
            (SELECT id FROM sandman.sandbox WHERE sandbox_id=\"', current_sandbox_id, '\"),
            "http://localhost:8000/fhir-app/launch.html",
            "https://content.hspconsortium.org/images/my-web-app/logo/my.png",
            "My Web App"
          ),
          (
            "https://content.hspconsortium.org/apps/cds-hooks-sandbox/.well-known/smart/manifest.json",
            "48163c5e-88b5-4cb3-92d3-23b800caa927",
            (SELECT created_by_id FROM sandman.sandbox WHERE sandbox_id=\"', current_sandbox_id, '\"),
            NOW(),
            1,
            null,
            "http://cds-hooks.org/",
            "The CDS Hooks Sandbox is a tool that allows users to simulate the workflow of the CDS Hooks standard.",
            "CDS Hooks",
            "REPLICA",
            (SELECT id FROM sandman.sandbox WHERE sandbox_id=\"', current_sandbox_id, '\"),
            "http://sandbox.cds-hooks.org/launch.html",
            "https://content.hspconsortium.org/images/cds-hooks-sandbox/logo/CdsHooks.png",
            "CDS Hooks Sandbox"
          );'
          );

      PREPARE Stmt FROM @SQLStmt;
      IF current_sandbox_id != 'hspc5' AND  current_sandbox_id != 'hspc6' AND current_sandbox_id != 'hspc7'
        AND current_sandbox_id != 'hspc' AND current_sandbox_id != 'hspc3' AND current_sandbox_id != 'hspc4' AND current_sandbox_id != 'hspc1' THEN
			    EXECUTE Stmt;
			END IF;
	END LOOP testLoop;
END;
;;
DELIMITER ;
CALL AddDefaultAppsToAllSandboxes();

SET @clientId = null;
			SET @clientName = null;
			SET @manifestUrl = null;
SELECT client_id FROM sandman.auth_client WHERE id=(SELECT auth_client_id FROM sandman.app WHERE id=88) INTO @clientId;
UPDATE sandman.app SET client_id=@clientId WHERE id=88;
SELECT client_name FROM sandman.auth_client WHERE id=(SELECT auth_client_id FROM sandman.app WHERE id=88) INTO @clientName;
UPDATE sandman.app SET client_name=@clientName WHERE id=88;
