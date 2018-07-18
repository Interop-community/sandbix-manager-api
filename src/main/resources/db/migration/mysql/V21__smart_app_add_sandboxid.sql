ALTER TABLE sandbox
  ADD UNIQUE (sandbox_id);

ALTER TABLE smartapp
    ADD (sandbox_id VARCHAR(255) NOT NULL);

DELETE FROM smartapp WHERE id = 'hspc-patient-data-manager';

UPDATE smartapp
SET sandbox_id='hspc5'
WHERE id='hspc-bilirubin-risk-chart';

UPDATE smartapp
SET sandbox_id='hspc5'
WHERE id='hspc-my-web-app';

UPDATE smartapp
SET sandbox_id='hspc5'
WHERE id='cds-hooks-sandbox';

ALTER TABLE smartapp
    ADD FOREIGN KEY fk_smartappsandbox (sandbox_id) REFERENCES sandbox(sandbox_id);
