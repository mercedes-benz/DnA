--
-- DELETE OLD RECORDS
--
delete from trino_connectors_nsql;
--
-- INSERT Script start
--
insert into trino_connectors_nsql(id,data)
values
      ('Iceberg','{"formats" : ["Parquet","ORC"], "dataTypes" : ["BOOLEAN","INT","BIGINT","REAL","DOUBLE","DECIMAL(6,4)","VARCHAR","VARBINARY","DATE","TIME(6)","TIMESTAMP(6)","TIMESTAMP(6)WITH TIME ZONE","UUID"]}'),
      ('Delta Lake','{"formats" : ["Parquet"], "dataTypes" : ["BOOLEAN","INT","BIGINT","REAL","DOUBLE","DECIMAL(6,4)","VARCHAR","VARBINARY","DATE","TIMESTAMP(3)WITH TIME ZONE"]}');
--
-- INSERT Script end
--