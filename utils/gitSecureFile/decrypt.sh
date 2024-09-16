#!/bin/bash
###############################################
#Run the from the parent Directory
#./utils/gitSecureFile/decrypt.sh
#Otherwise, it will throw error file not found
###############################################
# SET/EXPORT ENV
# $SECRET_PASSPHRASE - Password to decrypt
# $SOURCE - Source encrypted .gpg file path. Eg: ./folder/file.txt.gpg
# $DESTINATION -File path to create decrypted file. Eg: ./folder/file.txt
###############################################
gpg --quiet --batch --yes --decrypt --passphrase="$SECRET_PASSPHRASE" \
--output $DESTINATION $SOURCE