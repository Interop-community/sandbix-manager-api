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

DROP PROCEDURE IF EXISTS tmp_drop_foreign_key;

DELIMITER $$

CREATE PROCEDURE tmp_drop_foreign_key(IN tableName VARCHAR(64), IN constraintName VARCHAR(64))
BEGIN
    IF EXISTS(
        SELECT * FROM information_schema.table_constraints
        WHERE
            table_schema    = DATABASE()     AND
            table_name      = tableName      AND
            constraint_name = constraintName AND
            constraint_type = 'FOREIGN KEY')
    THEN
        SET @query = CONCAT('ALTER TABLE ', tableName, ' DROP FOREIGN KEY ', constraintName, ';');
        PREPARE stmt FROM @query;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;

/* ========= Modify - Begin. ========= */
CALL tmp_drop_foreign_key('launch_scenario', 'FK_4duagy85r364powdn2nu84w8a');
CALL tmp_drop_foreign_key('launch_scenario', 'FKf2n0w2ou34ddpwuyi88c92yyv');
CALL tmp_drop_foreign_key('launch_scenario', 'launch_scenario_ibfk_2');
/* ========= Modify - End. =========== */

DROP PROCEDURE tmp_drop_foreign_key;

ALTER TABLE launch_scenario DROP patient_id;
ALTER TABLE launch_scenario DROP launch_embedded;
DROP TABLE patient;