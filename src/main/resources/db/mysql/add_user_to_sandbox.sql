select * from sandbox where name='something';

select * from user where email='travis@interopion.com';

insert into sandbox_invite (invite_timestamp, status, invited_by_id, invitee_id, sandbox_id)
values (now(), 0, 5, 5, 986);
