#!/bin/bash
###############################################
#Run the from the parent Directory
#./utils/gitSecureFile/encrypt.sh
#Otherwise, it will throw error file not found
###############################################
# SET/EXPORT ENV
# $SECRET_PASSPHRASE - Password to encrypt
# $SOURCE - Source file path to encrypt the file. Eg: ./folder/file.txt
# $DESTINATION - File path to create encrypted file Eg: ./folder/file.txt.gpg
###############################################

 gpg --batch --symmetric --cipher-algo AES256 --passphrase="$SECRET_PASSPHRASE" --output $DESTINATION $SOURCE