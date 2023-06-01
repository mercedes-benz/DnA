drop procedure if exists reportNsql_Pl_convertKpiNameToObject();

create or replace procedure reportNsql_Pl_convertKpiNameToObject()
Language plpgsql
as $$
DECLARE
    item RECORD;
begin 
	for item in 
		 SELECT index - 1,
			('{kpis,' || index - 1 || ',name}')::TEXT[] AS path,
           id as identifier
			, (data->'kpis'->>(index-1)::int)::json->'name' AS nameValue
    		FROM report_nsql,
         	jsonb_array_elements(data -> 'kpis') WITH ORDINALITY arr(item, index)
			where data -> 'kpis' <> 'null'
	loop
		UPDATE report_nsql i
		SET data = jsonb_set(data, item.path, jsonb_build_object('name',item.nameValue,'classification',null)) where i.id = item.identifier;
	end loop;
end;$$;

call reportNsql_Pl_convertKpiNameToObject();
