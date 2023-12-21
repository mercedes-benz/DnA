UPDATE forecast_nsql
SET data = jsonb_set(
    data,
    '{leanGovernanceFeilds}',
    '{"tags": null, 
    "piiData": false, 
    "archerId": "", 
    "division": "", 
    "decription": "", 
    "department": null,
    "divisionId": "",
    "termsOfUse": false,
    "procedureId": "",
    "subDivision": "",
    "subDivisionId": "",
    "typeOfProject": "",
    "dataClassification": ""}',
    true
)
WHERE data->>'leanGovernanceFeilds' IS NULL