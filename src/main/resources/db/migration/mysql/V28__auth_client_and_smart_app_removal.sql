DELETE FROM sandbox_activity_log WHERE activity=16;

ALTER TABLE launch_scenario DROP COLUMN smart_app_id;
ALTER TABLE app DROP COLUMN app_manifest_uri;

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

CALL tmp_drop_foreign_key('app', 'FK_lik9ikgidlfr6nfjt3efo1mtr');
CALL tmp_drop_foreign_key('app', 'FKmgfhf5wulfbhdb765y2yvpsjg');
CALL tmp_drop_foreign_key('app', 'app_ibfk_4');
CALL tmp_drop_foreign_key('app', 'smart_app_ibfk_1');

DROP PROCEDURE tmp_drop_foreign_key;

ALTER TABLE app DROP COLUMN auth_client_id;

DROP TABLE auth_client;

DROP TABLE smart_app
