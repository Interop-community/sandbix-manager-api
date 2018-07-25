ALTER TABLE launch_scenario ADD patient VARCHAR(100) DEFAULT NULL;
ALTER TABLE launch_scenario ADD patient_name VARCHAR(100) DEFAULT NULL;
ALTER TABLE launch_scenario ADD encounter VARCHAR(100) DEFAULT NULL;
ALTER TABLE launch_scenario ADD location VARCHAR(100) DEFAULT NULL;
ALTER TABLE launch_scenario ADD resource VARCHAR(100) DEFAULT NULL;
ALTER TABLE launch_scenario ADD smart_style_url VARCHAR(100) DEFAULT NULL;
ALTER TABLE launch_scenario ADD intent VARCHAR(100) DEFAULT NULL;
ALTER TABLE launch_scenario ADD smart_app_id VARCHAR(36) DEFAULT NULL;
ALTER TABLE launch_scenario ADD title VARCHAR(75) DEFAULT NULL;
ALTER TABLE launch_scenario ADD need_patient_banner VARCHAR(1) DEFAULT NULL;
UPDATE launch_scenario SET need_patient_banner = 'T' WHERE launch_embedded = 0;
UPDATE launch_scenario SET need_patient_banner = 'F' WHERE launch_embedded = 1;

UPDATE launch_scenario
    SET patient = (
        SELECT fhir_id
        FROM patient
        WHERE launch_scenario.patient_id = patient.id
    );
UPDATE launch_scenario
    SET patient_name = (
        SELECT name
        FROM patient
        WHERE launch_scenario.patient_id = patient.id
    );

ALTER TABLE launch_scenario DROP FOREIGN KEY `launch_scenario_ibfk_2`;
ALTER TABLE launch_scenario DROP patient_id;
DROP TABLE IF EXISTS patient;
ALTER TABLE launch_scenario DROP launch_embedded;