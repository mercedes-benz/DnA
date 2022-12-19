UPDATE dataproduct_nsql
SET data = data - 'dataProductId' || jsonb_build_object('dataTransferId', data->'dataProductId')
WHERE data ? 'dataProductId'
returning *;

UPDATE dataproduct_nsql
SET data = data - 'dataProductName' || jsonb_build_object('dataTransferName', data->'dataProductName')
WHERE data ? 'dataProductName'
returning *;

CREATE TABLE datatransfer_nsql AS TABLE dataproduct_nsql;

DROP TABLE dataproduct_nsql;

ALTER SEQUENCE dataproduct_seq RENAME TO datatransfer_seq;

