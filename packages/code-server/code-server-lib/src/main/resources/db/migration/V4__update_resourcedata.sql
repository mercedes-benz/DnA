--
-- UPDATE Script start
--
update workspace_nsql
set data = jsonb_set(data , '{projectDetails,recipeDetails,resource}', '"2Gi,1000Mi,500m,2000Mi,1000m"');
--
-- UPDATE Script end
--