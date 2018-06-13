DROP PROCEDURE run_as_prepared_query;

DELIMITER //
CREATE PROCEDURE run_as_prepared_query
  (IN qry varchar(4000))
  BEGIN
    SET @SQLStmt = qry;
    PREPARE Stmt FROM @SQLStmt;
    EXECUTE Stmt;
    DEALLOCATE PREPARE Stmt;

  END //
DELIMITER ;


DROP PROCEDURE delete_sandbox_proc;

DELIMITER //
CREATE PROCEDURE delete_sandbox_proc
(IN delete_sandbox_id int(11))
BEGIN

  START TRANSACTION;

  SELECT DATABASE();

  SELECT CONCAT('Deleting sandbox: ', delete_sandbox_id, '...') as status;

  CALL run_as_prepared_query(CONCAT('DELETE FROM sandbox_user_roles WHERE sandbox = ', delete_sandbox_id));

  CALL run_as_prepared_query(CONCAT('DELETE FROM user_sandbox WHERE sandbox_id = ', delete_sandbox_id));

  CALL run_as_prepared_query(CONCAT('DELETE FROM sandbox_activity_log WHERE sandbox_id = ', delete_sandbox_id));

  CALL run_as_prepared_query(CONCAT('DELETE FROM launch_scenario WHERE sandbox_id = ', delete_sandbox_id));

  CALL run_as_prepared_query(CONCAT('DELETE FROM patient WHERE sandbox_id = ', delete_sandbox_id));

  CALL run_as_prepared_query(CONCAT('DELETE FROM app WHERE sandbox_id = ', delete_sandbox_id));

  CALL run_as_prepared_query(CONCAT('DELETE FROM user_persona WHERE sandbox_id = ', delete_sandbox_id));

  CALL run_as_prepared_query(CONCAT('DELETE FROM sandbox_invite WHERE sandbox_id = ', delete_sandbox_id));

  CALL run_as_prepared_query(CONCAT('DELETE FROM sandbox WHERE id = ', delete_sandbox_id));

  SELECT concat('Deleting sandbox: ', delete_sandbox_id, ' complete');

  COMMIT;

END //
DELIMITER ;


DROP PROCEDURE DO_delete_sandbox_proc;

DELIMITER //
CREATE PROCEDURE DO_delete_sandbox_proc
(IN my_api_endpoint_index VARCHAR(255))
BEGIN
  DECLARE done BOOLEAN DEFAULT FALSE;
  DECLARE current_sandbox_id int(11);
  DECLARE cur CURSOR FOR SELECT id FROM sandbox WHERE api_endpoint_index = my_api_endpoint_index;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

  OPEN cur;

  testLoop: LOOP
    FETCH cur INTO current_sandbox_id;
    IF done THEN
      LEAVE testLoop;
    END IF;
    CALL delete_sandbox_proc(current_sandbox_id);
  END LOOP testLoop;

  CLOSE cur;
END //
DELIMITER ;

CALL DO_delete_sandbox_proc(2);