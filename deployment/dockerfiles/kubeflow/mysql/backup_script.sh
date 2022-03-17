#!/bin/bash

############# SET VARIABLES #############

# Env Variables

NOW="$(date +"%Y-%m-%d")"
STARTTIME=$(date +"%s")

############# BUILD ENVIRONMENT #############
# Check if temp Backup Directory is empty
mkdir -p $BACKUPDIR

if [ "$(ls -A $BACKUPDIR)" ]; then
    echo "Take action $BACKUPDIR is not Empty"
    rm -f $BACKUPDIR/*.gz
    rm -f $BACKUPDIR/*.mysql
else
    echo "$BACKUPDIR is Empty"
fi

############# BACKUP SQL DATABASES #############
mysqldump -u$USER -p$PASS -h $HOST --single-transaction --all-databases > "$BACKUPDIR/all_databases.sql";

############# ZIP BACKUP #############
cd $BACKUPDIR
tar -zcvf backup-${NOW}.tar.gz *.sql

