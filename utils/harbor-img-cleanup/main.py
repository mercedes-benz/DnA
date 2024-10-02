################################################################
################################################################
######      Don't RUN the code without discussing with    ######
######      PALANSA, it will make major deletion of       ######  
######      harbor images, even there is a chance         ######
######      of deleting production images.                ######
################################################################
################################################################
import requests
from requests.auth import HTTPBasicAuth

# Replace these with your actual username and password
username = ''
password = ''

# Base URL for the initial API endpoint
base_url = 'https://registry-emea.app.corpintra.net/api/v2.0/projects/dnaplatform/repositories'

# Initial page number
page = 1
page_size = 100

# List to store the 'name' parameters
names = []

while True:
    # Construct the URL with the current page number
    url = f'{base_url}?page={page}&page_size={page_size}'
    
    # Make the GET request with basic authentication
    response = requests.get(url, auth=HTTPBasicAuth(username, password))
    
    # Check if the request was successful
    if response.status_code == 200:
        data = response.json()
        
        # Check if the response is an empty object
        if not data:
            print(f"No more data found. Stopping at page {page}.")
            break

        # Extract and store the 'name' parameter from each item in the response
        for item in data:
            # Remove 'dnaplatform/' prefix from the name
            name = item['name'].replace('dnaplatform/', '')
            names.append(name)
        
        # Increment the page number for the next request
        page += 1
    else:
        print(f"Failed to retrieve data: {response.status_code}")
        break

# Print the list of names
# print("Names:", names)

# Base URL for the second API endpoint
artifact_base_url = 'https://registry-emea.app.corpintra.net/api/v2.0/projects/dnaplatform/repositories'

# Iterate over each name and make the second API call
for name in names:
    artifact_page = 1
    while name == 'dna-frontend':
        artifact_url = f'{artifact_base_url}/{name}/artifacts?sort=push_time&page={artifact_page}&page_size={page_size}&with_tag=true&with_label=false&with_scan_overview=false&with_sbom_overview=false&with_signature=false&with_immutable_status=false&with_accessory=false'
        
        # Make the GET request with basic authentication
        artifact_response = requests.get(artifact_url, auth=HTTPBasicAuth(username, password))
    

        # Check if the request was successful
        if artifact_response.status_code == 200:
            artifact_data = artifact_response.json()
            
            # Check if the response is an empty object
            if not artifact_data:
                # print(f"No more artifacts found for {name}. Stopping at page {artifact_page}.")
                break
            
            # Print the 'tags.name' for each artifact
            for artifact in artifact_data:
                if 'tags' in artifact and artifact['tags']:
                    for tag in artifact['tags']:
                        tag_name = tag['name']
                        try:
                            if (len(tag_name) == 40 and all(c in '0123456789abcdef' for c in tag_name)):
                                print(f"Tag name for {name}: {tag_name}")
                                # # Construct the URL for the DELETE request
                                ## Uncomment below section after dry run of code
                                ## Caution: Below code will delete the images permanently
                                # delete_url = f'{artifact_base_url}/{name}/artifacts/{tag_name}'
                                # # Make the DELETE request with basic authentication
                                # delete_response = requests.delete(delete_url, auth=HTTPBasicAuth(username, password))
                                # if delete_response.status_code == 200:
                                #     print(f"Successfully deleted artifact with tag {tag_name} for {name}")
                                # else:
                                #     print(f"Failed to delete artifact with tag {tag_name} for {name}: {delete_response.status_code}")
                        except ValueError:
                            print(f"Valueis incorrect for {name}: {tag_name}")
                            # Handle the case where tag_name is not a valid integer
                        
            # Increment the page number for the next request
            artifact_page += 1
        else:
            # print(f"Failed to retrieve artifacts for {name}: {artifact_response.status_code}")
            break