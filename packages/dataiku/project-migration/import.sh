#!/bin/bash
#Run as Dataiku User
whoami
dss_migration_directory='/data/migration'
dss_migration_directory_backup='/data/migration/backup' 
dss_data_directory='/data/dataiku'
azure_storage_conn_string=''
api_url=''
api_key=''

if [ -z "$dss_migration_directory" ]
then
      echo "variable \$dss_migration_directory is unset"
      exit 1
fi
if [ -z "$dss_data_directory" ]
then
      echo "variable \$dss_data_directory is unset"
      exit 1
fi
if [ -z "$azure_storage_conn_string" ]
then
      echo "variable \$azure_storage_conn_string is unset"
      exit 1
fi
if [ -z "$api_url" ]
then
      echo "variable \$api_url is unset"
      exit 1
fi
if [ -z "$api_key" ]
then
      echo "variable \$api_key is unset"
      exit 1
fi


mkdir $dss_migration_directory
mkdir $dss_migration_directory_backup
echo "=============================================="
echo "Downloading Project List CSV..."
echo "=============================================="
az storage blob download --account-name azureaccount --container-name containername --file $dss_migration_directory/project-list.csv --name migration/project-list.csv --connection-string "$azure_storage_conn_string"

sed -i 's/\r//g' $dss_migration_directory/project-list.csv
INPUT=$dss_migration_directory/project-list.csv
OLDIFS=$IFS
IFS=','
[ ! -f $INPUT ] && { echo "$INPUT file not found"; exit 99; }
while read shortid projectKey
do
      newProjectKey=""
      projectName=""
      case $projectKey in
            *"PRACTICEPLAYGROUND_"*)
                  newProjectKey="PLAYGROUND_"${shortid^^}
                  projectName="Playground";;
            *"PLAYGROUND_"*)
                  newProjectKey="PLAYGROUND_"${shortid^^}
                  projectName="Playground";;
            *"DATAWRANGLING_"*)
                  newProjectKey="FOUNDATIONS_"${shortid^^}
                  projectName="Data Wrangling & Exploration – Live Session";;
            *"CODING_"*)
                  newProjectKey="DSMOD1_"${shortid^^}
                  projectName="Coding – Live Session";;
            *"DATAWRANGLINGII_"*)
                  newProjectKey="DSMOD2_"${shortid^^}
                  projectName="Data Wrangling II – Live Session";;
            *"DATAEXPLORATIONIISOLUTION_"*)
                  newProjectKey="DSMOD3_"${shortid^^}
                  projectName="Data Exploration II – Live Session";;
            *"MACHINELEARNING_"*)
                  newProjectKey="DSMOD4_"${shortid^^}
                  projectName="Machine Learning I – Live Session";;
            *"MACHINELEARNINGII_"*)
                  newProjectKey="DSMOD5_"${shortid^^}
                  projectName="Machine Learning II – Live Session";;
            *)
                  echo "INVALID FILE";;
      esac

      echo "==================================="
      echo "Downloading $projectKey folder..."
      echo "==================================="
      az storage blob download --account-name mbceudnadsdevsa --container-name dssmigration --file $dss_migration_directory/$projectKey.zip --name migration/$projectKey.zip --connection-string "$azure_storage_conn_string"

      echo "==================================="
      echo "Exporting Project $newProjectKey..."
      echo "==================================="
      $dss_data_directory/bin/dsscli project-export $newProjectKey $dss_migration_directory_backup/$newProjectKey.zip --uploads --managed-fs --managed-folders --input-managed-folders --input-datasets --all-datasets  --analysis-models --saved-models
      
      echo "=============================================="
      echo "Getting Project $newProjectKey permissions..."
      echo "=============================================="
      api_permission_payload=$(curl -k -s --user $api_key: -H "Content-Type: application/json" -X GET $api_url/$newProjectKey/permissions)
      echo "Permission Payload $api_permission_payload"

      echo "======================================="
      echo "Deleting the $newProjectKey Project..."
      echo "======================================="
      $dss_data_directory/bin/dsscli project-delete $newProjectKey

      echo "==================================="
      echo "Importing $newProjectKey Project..."
      echo "==================================="
      $dss_data_directory/bin/dsscli project-import $dss_migration_directory/$projectKey.zip --project-key $newProjectKey

      echo "=============================================="
      echo "Updating Project Permission $newProjectKey..."
      echo "=============================================="
      curl -k -s --user $api_key: -H "Content-Type: application/json" -X PUT $api_url/$newProjectKey/permissions --data "$api_permission_payload" -i

      echo "============================================"
      echo "Getting Project $newProjectKey metadata..."
      echo "============================================"
      api_metadata_payload=$(curl -k -s --user $api_key: -H "Content-Type: application/json" -X GET $api_url/$newProjectKey/metadata)
      api_metadata_payload=$(jq ".label = \"$projectName\"" <<<"$api_metadata_payload")
      echo "MetaData PayLoad $api_metadata_payload"
      echo "=================================================================="
      echo "Updating Project Metadata $newProjectKey, name $projectName..."
      echo "=================================================================="
      curl -k -s --user $api_key: -H "Content-Type: application/json" -X PUT $api_url/$newProjectKey/metadata --data "$api_metadata_payload" -i


      echo "============================================"
      echo "Compressing Image size for $newProjectKey..."
      echo "============================================"
      find $dss_data_directory/config/projects/$newProjectKey/pictures/project-original.png | xargs -L1 -I {} convert -resize 20% {} {}

done < $INPUT
IFS=$OLDIFS
echo "=============================================="
echo "Backup projects in Azure Storage Container"
echo "=============================================="
az storage fs directory upload -f backup -s $dss_migration_directory_backup  --connection-string $azure_storage_conn_string --recursive

rm -r $dss_migration_directory