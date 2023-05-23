update dataproduct_nsql set data = data #- '{contactInformation,dataTransferDate}';


UPDATE datatransfer_nsql
SET data = (REPLACE(data::TEXT, '"agreementDate"', '"dataTransferDate"'))::JSONB
RETURNING *;