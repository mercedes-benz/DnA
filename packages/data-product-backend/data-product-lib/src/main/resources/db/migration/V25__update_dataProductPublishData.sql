update dataproduct_nsql set data = jsonb_set(data,'{publish}', 'false');

UPDATE dataproduct_nsql set data = data #- '{howToAccessText}';