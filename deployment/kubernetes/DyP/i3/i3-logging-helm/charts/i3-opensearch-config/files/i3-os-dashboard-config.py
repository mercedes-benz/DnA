import base64
import json
import os
import time
import urllib.request
import urllib.parse
import io
import http.client
import http.client
import mimetypes
from codecs import encode
from urllib.parse import urlparse

def encode_basic_auth(username, password):
    credentials = f"{username}:{password}"
    encoded_credentials = base64.b64encode(credentials.encode('ascii')).decode('ascii')
    return f"Basic {encoded_credentials}"

def make_post_request(url, data, headers, valid_response_codes, fail_ok=False):
    # data = urllib.parse.urlencode(data)
    if data:
        data = data.encode()
    while True:
        try:
            if data:
                req = urllib.request.Request(url=url, data=data, headers=headers, method='POST')
                with urllib.request.urlopen(req) as response:
                    if response.status in valid_response_codes:
                        print(f"{url} successfull")
                        return response.read().decode('utf-8')
            else:
                req = urllib.request.Request(url=url, headers=headers, method='POST')
                with urllib.request.urlopen(req) as response:
                    if response.status in valid_response_codes:
                        print(f"{url} successfull")
                        return response.read().decode('utf-8')
        except Exception as e:
            print(f"{url} failed with {e}. Retrying with data={data} and headers={headers}")
            if fail_ok:
                return None
            time.sleep(2)

def multi_part_post(url, path, file_name, headers, valid_response_codes, fail_ok=False):
    while True:
        try:
            parsed_url = urlparse(url)
            print(f"parsed_url: {parsed_url.netloc}, port: {parsed_url.port}, path:{path}")     
            conn = None
            url_port = parsed_url.netloc.split(":")
            if parsed_url.scheme == "http":   
                conn = http.client.HTTPConnection(f"{url_port[0]}", f"{parsed_url.port}")
            elif parsed_url.scheme == "https":
                conn = http.client.HTTPSConnection(f"{url_port[0]}", f"{parsed_url.port}")
            else:
                raise NotImplementedError("Scheme should be http/https")
            dataList = []
            boundary = 'wL36Yn8afVp8Ag7AmP8qZ0SA4n1v9T'
            dataList.append(encode('--' + boundary))
            dataList.append(encode('Content-Disposition: form-data; name=file; filename={0}'.format(f'{file_name}')))
            fileType = mimetypes.guess_type(f'{file_name}')[0] or 'application/octet-stream'
            dataList.append(encode('Content-Type: {}'.format(fileType)))
            dataList.append(encode(''))

            with open(f'{file_name}', 'rb') as f:
                dataList.append(f.read())
                dataList.append(encode('--'+boundary+'--'))
                dataList.append(encode(''))
                body = b'\r\n'.join(dataList)
                payload = body
                headers_to_add = {
                'Content-type': 'multipart/form-data; boundary={}'.format(boundary) 
                }
                merged_headers =  headers | headers_to_add
                conn.request("POST", f"{path}", payload, merged_headers)
                res = conn.getresponse()
                data = res.read()                
                print(data.decode("utf-8"))
                if res.status in valid_response_codes:
                    return data.decode("utf-8")
                time.sleep(2)
        except Exception as e:
            print(f"{url} failed with {e}. Retrying with headers={merged_headers}")
            if fail_ok:
                return None
            time.sleep(2)

def make_put_request(url, data, headers, valid_response_codes):
    if data:
        data = data.encode('ascii')
    while True:
        try:
            req = urllib.request.Request(url=url, data=data, headers=headers, method='PUT')
            with urllib.request.urlopen(req) as response:
                if response.status in valid_response_codes:
                    print(f"{url} successfull")
                    return response.read().decode('utf-8')
        except urllib.error.HTTPError as e:
            print(e.code)
            print(e.read())  
            print(f"{url} failed with {e}. Retrying with data={data} and headers={headers}")
            time.sleep(2)
        except Exception as e:
            print(f"{url} failed with {e}. Retrying with data={data} and headers={headers}")
            time.sleep(2)

def make_get_request(url, headers, valid_response_codes):
    while True:
        try:
            req = urllib.request.Request(url=url, headers=headers, method='GET')
            with urllib.request.urlopen(req) as response:
                if response.status in valid_response_codes:
                    print(f"{url} successfull")
                    return response.read().decode('utf-8')
        except Exception as e:
            print(f"{url} failed with {e}. Retrying with headers={headers}")
            time.sleep(2)

# Function to list files in folders
def list_files(dir):
    r = []
    for root, dirs, files in os.walk(dir):
        for name in files:
            r.append(os.path.join(root, name))
    return r

# Function to read data from a file
def read_from_file(file_path):
    with open(file_path, 'r') as file:
        return file.read().strip()
    
# Read configuration and env
opensearch_dashboard_urls = os.environ['OPENSEARCH_DASHBOARD_HOSTS']
api_path = "/_plugins/_ism/policies/policy_1" 
saved_objects_path =  os.environ['SAVED_OBJECT_API_PATH']
saved_object_enabled = os.environ['SAVED_OBJECT_API_ENABLED']
saved_object_api_params = os.environ.get('SAVED_OBJECT_API_PARAMS', "")
i3_ism = os.environ['I3_ISM']
os_dashboard_version = os.environ['OPENSEARCH_DASHBOARD_VERSION']
username = os.environ['OPENSEARCH_USERNAME']
password = os.environ['OPENSEARCH_PASSWORD']

auth_header = encode_basic_auth(username, password)

print(f"opensearch_dashboard_urls: {opensearch_dashboard_urls}")
print(f"saved_objects_path: {saved_objects_path}")
print(f"saved_object_enabled: {saved_object_enabled}")
print(f"saved_object_api_params: {saved_object_api_params}")
print(f"i3_ism: {i3_ism}")
print(f"os_dashboard_version: {os_dashboard_version}")


def wait_for_os_dashboard_to_be_ready(dashboard_url):
    url = f"{dashboard_url}/api/status"
    headers = {'Authorization': auth_header}
    make_get_request(url, headers, [200])

ism_data = {
        "policy": {
        "description": "default workflow",
        "default_state": "hot",
        "schema_version": 1,
        "states": [
            {
            "name": "hot",
            "actions": [],
            "transitions": [
                {
                "state_name": "warm",
                "conditions": {
                    "min_index_age": "1d"
                }
                }
            ]
            },
            {
            "name": "warm",
            "actions": [],
            "transitions": [
                {
                "state_name": "delete",
                "conditions": {
                    "min_index_age": "7d"
                }
                },
                {
                "state_name": "delete",
                "conditions": {
                    "min_size": "20gb"
                }
                }
            ]
            },
            {
            "name": "delete",
            "actions": [
                {
                "delete": {}
                }
            ]
            }
        ],
        "ism_template": {
            "index_patterns": ["kubernetes_*"],
            "priority": 1
        }
        }
    }

for dashboard_url in opensearch_dashboard_urls.strip().split(','):
    # wait for dashboards
    wait_for_os_dashboard_to_be_ready(dashboard_url)

    # Creating Index Pattern
    headers = {'content-type': 'application/json', 'osd-version': os_dashboard_version, 'Authorization': auth_header}
    data = '{"attributes": {"title": "kubernetes_*","timeFieldName": "@flb-timestamp"}}'
    response = make_post_request(
        f"{dashboard_url}/api/saved_objects/index-pattern/kubernetes?overwrite=true", data, headers, [200], True
    ) 

    # Creating Application Logs Filter
    headers = {'content-type': 'application/json', 'osd-version': os_dashboard_version, 'Authorization': auth_header}
    data = None
    response = make_post_request(
        f"{dashboard_url}/api/saved_objects/search/application-logs?overwrite=true",data, headers, [200] , True     
    ) 

    # Creating Platform Kubernetes API Audit Logs Filter
    headers = {'content-type': 'application/json', 'osd-version': os_dashboard_version, 'Authorization': auth_header}
    data = None
    response = make_post_request(
        f"{dashboard_url}/api/saved_objects/search/platform-k8s-audit-logs?overwrite=true", data, headers, [200], True
    ) 

    # Creating Platform Kubernetes Nodes Kubelet Logs Filter
    headers = {'content-type': 'application/json', 'osd-version': os_dashboard_version, 'Authorization': auth_header}
    data = None
    response = make_post_request(
        f"{dashboard_url}/api/saved_objects/search/platform-kubelet-logs?overwrite=true", data, headers, [200], True
    ) 


    # ISM
    if i3_ism == 'true':
        # check for ism
        headers = {'content-type': 'application/json', 'osd-version': os_dashboard_version, 'Authorization': auth_header}
        response = make_get_request(
            f"{dashboard_url}/api/ism/policies/kubernetes", headers, [200]
        )
        print(f'ISM response {response}')
        data = json.loads(response)
        if "error" in data.keys():
            if data["error"].startswith("[index_not_found_exception]"):
                headers = {'content-type': 'application/json', 'osd-version': os_dashboard_version, 'Authorization': auth_header}
                response = make_put_request(
                    f"{dashboard_url}/api/ism/policies/kubernetes", json.dumps(ism_data), headers, [200]
                ) 
                print(f'ISM Policy response {response}')
                # Assigning kubernetes index management policy for kubernetes_ index
                headers = {'content-type': 'application/json', 'osd-version': os_dashboard_version, 'Authorization': auth_header}
                data = '{"indices": ["kubernetes_*"],"policyId": "kubernetes"}'
                response = make_post_request(
                    f"{dashboard_url}/api/ism/applyPolicy", data, headers, [200]
                ) 
                print(f'ISM Policy Apply response {response}')
     
    # Saved object API
    if saved_object_enabled == "true":
        # first install saved objects bundled with chart
        for file_name in list_files("/etc/savedobjects"):
            print(f"file_name {file_name}")
            headers = {'osd-version': os_dashboard_version, 'Authorization': auth_header}
            response = multi_part_post(url=dashboard_url, path=f"{saved_objects_path}{saved_object_api_params}", file_name=file_name, headers=headers, valid_response_codes=[200])
            print(f'Saved Object response {response}')
        # install additional configmap files
        for file_name in list_files("/etc/additionalcms"):
            print(f"file_name additionalcms {file_name}")
            headers = {'osd-version': os_dashboard_version, 'Authorization': auth_header}
            response = multi_part_post(url=dashboard_url, path=f"{saved_objects_path}{saved_object_api_params}", file_name=file_name, headers=headers, valid_response_codes=[200])
            print(f'Saved Object response {response}')

exit(0)
        