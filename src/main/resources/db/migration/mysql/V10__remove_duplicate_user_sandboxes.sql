# correct a bug in V4 and V5 where two user_sandbox exist for each starter sandbox
DELETE FROM user_sandbox
WHERE user_id=(SELECT id FROM user WHERE sbm_user_id='admin')
  AND sandbox_id=(SELECT id FROM sandbox WHERE sandbox_id='hspc1');

INSERT INTO user_sandbox (user_id, sandbox_id)
VALUES
  ((SELECT id FROM user WHERE sbm_user_id='admin'), (SELECT id FROM sandbox WHERE sandbox_id='hspc1'));

DELETE FROM user_sandbox
WHERE user_id=(SELECT id FROM user WHERE sbm_user_id='admin')
      AND sandbox_id=(SELECT id FROM sandbox WHERE sandbox_id='hspc4');

INSERT INTO user_sandbox (user_id, sandbox_id)
VALUES
  (1, (SELECT id FROM sandbox WHERE sandbox_id='hspc4'));
