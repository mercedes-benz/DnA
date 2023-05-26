drop procedure if exists pr_collaborator_sql_addingOwnersAsAdminCollabs();

create or replace procedure pr_collaborator_sql_addingOwnersAsAdminCollabs()
Language plpgsql
as $$
DECLARE
    item RECORD;
	tempuser RECORD;
begin 
	for item in 
		select id as pkidentifier, created_by as owner from dataiku_sql 
	loop
		select givenname , surname into tempuser from userprivilege_sql i where i.userid = item.owner;
		insert into collaborator_sql values ( gen_random_uuid(), item.pkidentifier, tempuser.givenname, 'administrator',
												  tempuser.surname, item.owner );
	end loop;
end;$$;

call pr_collaborator_sql_addingOwnersAsAdminCollabs();