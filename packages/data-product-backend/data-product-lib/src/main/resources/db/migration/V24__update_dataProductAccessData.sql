drop procedure if exists testDp_Pl_migrateKafkaAndOneApiValues();

create or replace procedure testDp_Pl_migrateKafkaAndOneApiValues()
Language plpgsql
as $$
DECLARE
    item RECORD;
begin 
	for item in 
		select ('{access}')::TEXT[] AS kafkaPath,
		('{access,' || 'oneApi}')::TEXT[] AS oneApiPath,
        id as identifier, 
		data->'kafka' AS kafkaValue,
		data->'oneApi' AS oneApiValue		   
    	FROM dataproduct_nsql         	

	loop
		UPDATE dataproduct_nsql i
		SET data = jsonb_set(data, item.kafkaPath, jsonb_build_object('kafka',item.kafkaValue)) where i.id = item.identifier;
		
		UPDATE dataproduct_nsql i
		SET data = jsonb_set(data, item.oneApiPath, item.oneApiValue) where i.id = item.identifier;			
	end loop;
	UPDATE dataproduct_nsql
	set data = data #- '{kafka}';
	UPDATE dataproduct_nsql
	set data = data #- '{oneApi}';
end;$$;
call testDp_Pl_migrateKafkaAndOneApiValues();
