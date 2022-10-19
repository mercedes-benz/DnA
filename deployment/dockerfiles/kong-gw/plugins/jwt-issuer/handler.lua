local http = require "resty.http"
local utils = require "kong.tools.utils"
 
local TokenHandler = {
    VERSION = "1.0",
    PRIORITY = 1000,
}
 
 
local function introspect_access_token(conf, access_token)
  local httpc = http:new()
--   -- step 1: validate the token
--   local res, err = httpc:request_uri(conf.introspection_endpoint, {
--       method = "POST",
--       ssl_verify = false,
--       headers = {
--           ["Content-Type"] = "application/x-www-form-urlencoded",
--           ["Authorization"] = "Bearer " .. access_token }
--   })
 
--   if not res then
--       kong.log.err("failed to call introspection endpoint: ",err)
--       return kong.response.exit(500)
--   end
--   if res.status ~= 200 then
--       kong.log.err("introspection endpoint responded with status: ",res.status)
--       return kong.response.exit(500)
--   end
 
  -- step 2: validate the customer access rights
--   local res, _ = httpc:request_uri(conf.authorization_endpoint, {
--       method = "POST",
--       ssl_verify = false,
--       body = '{ "custId":"' .. customer_id .. '"}',
--       headers = { ["Content-Type"] = "application/json",
--           ["Authorization"] = "Bearer " .. access_token }
--   })
 
--   if not res then
--     kong.log.err("failed to call authorization endpoint: ",err)
--     return kong.response.exit(500)
--   end
--   if res.status ~= 200 then
--       kong.log.err("authorization endpoint responded with status: ",res.status)
--       return kong.response.exit(500)
--   end
  return true -- all is well
end
 
function TokenHandler:access(conf)
  local access_token = kong.request.get_headers()[conf.token_header]
  kong.log.err("=================Hello inside jwt-issuer plugin=========",access_token);
  if not access_token then
      kong.response.exit(401)  --unauthorized
  end
  -- replace Bearer prefix
  access_token = access_token:sub(8,-1) -- drop "Bearer "
--   local request_path = kong.request.get_path()
--   local values = utils.split(request_path, "")
--   local customer_id = values[3]
 
  --introspect_access_token(conf, access_token)
 
  --kong.service.clear_header(conf.token_header)
end
 
 
return TokenHandler