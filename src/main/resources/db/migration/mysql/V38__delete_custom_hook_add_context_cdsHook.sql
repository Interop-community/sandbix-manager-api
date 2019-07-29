# Delete the custom hook table and just add context column in the cds_hook table instead.
# Having a context column signifies that it is a custom hook


DROP TABLE custom_hook;
ALTER TABLE cds_hook ADD context JSON DEFAULT NULL;
ALTER TABLE cds_hook modify description LONGTEXT DEFAULT NULL;
ALTER TABLE cds_service_endpoint modify description LONGTEXT DEFAULT NULL;