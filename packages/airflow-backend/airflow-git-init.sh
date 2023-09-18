#!/bin/sh

echo "Trying to clone Airflow Dag Repo..."
cd /git
export GIT_ASKPASS=/tmp/git-askpass-helper.sh


# Define the directory path
dir_path="/git/airflow-user-dags"

# Check if the directory exists
if [ -d "$dir_path" ]; then
    echo "Directory $dir_path exists."
    cd "$dir_path"

    # Check if it's a Git repository
    if [ -d ".git" ]; then
        echo "It's a Git repository. Checking out 'dev' branch."
        git checkout dev
		exit 0
    else
        echo "It's not a Git repository."
		cd /git
		git clone $AIRFLOW_GIT_URI
		cd /git/airflow-user-dags
		git checkout $GIT_BRANCH
		exit 0 
    fi
else
    echo "Directory $dir_path does not exist."
    echo "Cloning 'airflow-user-dags' repository."
	git clone $AIRFLOW_GIT_URI 
	cd /git/airflow-user-dags
	git checkout $GIT_BRANCH
	exit 0 
fi

