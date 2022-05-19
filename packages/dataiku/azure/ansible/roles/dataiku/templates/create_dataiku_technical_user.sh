#!/bin/bash

USER_ISOLATION_GROUP_NAME="{{dss_user_isolation_group}}"

if [ -z "$1" ]
then
      echo "Missing user name"
      exit 1;
fi

USERNAME="$1"

if [[ ! "$USERNAME" =~ ^([a-zA-Z0-9_-]+)$ ]]; then
  echo "Invalid user name"
  exit 1;
fi

# In case of re-creating the project with the same name as deleted project,
id -u $USERNAME >/dev/null 2>&1 || adduser --disabled-password --gecos "" $USERNAME
usermod -a -G $USER_ISOLATION_GROUP_NAME $USERNAME
