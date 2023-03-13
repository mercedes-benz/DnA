--
-- UPDATE Script start
--
UPDATE datatransfer_nsql SET data = replace(data::text, 'N.A', 'No')::jsonb
where (jsonb_extract_path_text(data,'providerInformation','transnationalDataTransfer','insiderInformation'))
IN('N.A') ;

UPDATE datatransfer_nsql SET data = replace(data::text, 'Yes', 'Yes, insider information')::jsonb
where (jsonb_extract_path_text(data,'providerInformation','transnationalDataTransfer','insiderInformation'))
IN('Yes') ;

UPDATE dataproduct_nsql SET data = replace(data::text, 'N.A', 'No')::jsonb
where (jsonb_extract_path_text(data,'transnationalDataTransfer','insiderInformation'))
IN('N.A');

UPDATE dataproduct_nsql SET data = replace(data::text, 'Yes', 'Yes, insider information')::jsonb
where (jsonb_extract_path_text(data,'transnationalDataTransfer','insiderInformation'))
IN('Yes');

--
-- UPDATE Script end
--