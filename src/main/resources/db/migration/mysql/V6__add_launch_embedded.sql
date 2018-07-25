# add launch_embedded column

ALTER TABLE sandman.launch_scenario ADD COLUMN launch_embedded BIT(1) NOT NULL;
