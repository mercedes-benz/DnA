import json
import requests


# Define paths to configuration files
opensearch_url_file = '/etc/config/opensearchUrl'


# Function to read data from a file
def read_from_file(file_path):
    with open(file_path, 'r') as file:
        return file.read().strip()
    
# Read configuration and secrets
opensearch_url = read_from_file(opensearch_url_file)
api_path = "/api/anomaly_detectors/detectors" 


data = {
        "name": "i3-http-response-codes",
        "description": "This anomaly detector detectes http response codes , aggregates them by count and visualize anomalies",
        "timeField": "@flb-timestamp",
        "indices": [
            "kubernetes_cluster-*"
        ],
        "detectionInterval": {
        "period": {
            "interval": 10,
            "unit": "Minutes"
        }
        },
        "windowDelay": {
            "period": {
                "interval": 1,
                "unit": "Minutes"
            }
        },
        "shingleSize": 8,
        "schemaVersion": 0,                  
        "primaryTerm": 1,
        "seqNo": 4,
        "filterQuery": {
            "bool": {
                "filter": [
                    {
                        "query_string": {
                            "query": "*\"status\": 400*",
                            "default_field": "log",
                            "fields": [],
                            "type": "best_fields",
                            "default_operator": "or",
                            "max_determinized_states": 10000,
                            "enable_position_increments": "true",
                            "fuzziness": "AUTO",
                            "fuzzy_prefix_length": 0,
                            "fuzzy_max_expansions": 50,
                            "phrase_slop": 0,
                            "escape": "false",
                            "auto_generate_synonyms_phrase_query": "true",
                            "fuzzy_transpositions": "true",
                            "boost": 1
                        }
                    },
                    {
                        "term": {
                            "kubernetes.container_image.keyword": {
                                "value": "registry-emea.app.corpintra.net/i3-releases/auth-service",
                                "boost": 1
                            }
                        }
                    }
                ],
                "adjust_pure_negative": "true",
                "boost": 1
            }
        },
        "featureAttributes": [
        {
            "featureId": "E8uKPowBbFiBbKt2rt8b",
            "featureName": "httpstatus",
            "featureEnabled": "true",
            "aggregationQuery": {
                "aggs": {
                    "sum": {
                        "field": "log.status"
                    }
                }
            }
        }
    ],
        "result_index" : "opensearch-ad-plugin-result-i3-access"
    }

headers={'content-type': 'application/json', 'osd-xsrf': 'osd-fetch'}

response = requests.post(
    f"{opensearch_url}{api_path}",
    headers=headers,
    data=data,
)
 
# Check the response
if response.status_code == 200:
    print("Data imported successfully.")
    exit(0)
else:
    print(f"Failed to import data: {response.text}")
    exit(-1)