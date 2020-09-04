ALTER TABLE sandman.sandbox ADD creation_status varchar(15);
UPDATE sandman.sandbox SET creation_status = 'CREATED';