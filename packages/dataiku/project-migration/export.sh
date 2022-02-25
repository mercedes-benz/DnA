#!/bin/bash
#Run as Dataiku User
whoami
dss_migration_directory='/data/migration'
dss_data_directory='/data/dataiku'
project_list=''
azure_storage_conn_string=''


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
if [ -z "$project_list" ]
then
      echo "variable \$project_list is unset"
      exit 1
fi

mkdir $dss_migration_directory
echo "=============================================="
echo "Copying the Project list csv..."
echo "=============================================="
cp $project_list $dss_migration_directory/project-list.csv

sed -i 's/\r//g' $dss_migration_directory/project-list.csv
INPUT=$dss_migration_directory/project-list.csv
OLDIFS=$IFS
IFS=','
[ ! -f $INPUT ] && { echo "$INPUT file not found"; exit 99; }
while read shortid projectKey
do
	echo "ShortId : $shortid"
      echo "=============================================="
	echo "Exporting Project : $projectKey..."
      echo "=============================================="
      $dss_data_directory/bin/dsscli project-export $projectKey $dss_migration_directory/$projectKey.zip --uploads --managed-fs --managed-folders --input-managed-folders --input-datasets --all-datasets  --analysis-models --saved-models
      sleep 1
done < $INPUT
IFS=$OLDIFS
echo "=============================================="
echo "Uploading projects in Azure Storage Container"
echo "=============================================="
az storage fs directory upload -f dssmigration -s $dss_migration_directory  --connection-string $azure_storage_conn_string --recursive

rm -r $dss_migration_directory