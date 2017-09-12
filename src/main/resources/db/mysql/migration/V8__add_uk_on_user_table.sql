# add unique index on email

ALTER TABLE sandman.user ADD UNIQUE INDEX sandman_user_idx (email);