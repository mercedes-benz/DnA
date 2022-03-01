FROM docker.io/bitnami/mysql:8.0.28-debian-10-r0

USER root

# Install vim & curl
RUN apt-get update
RUN apt-get install vim -y
RUN apt-get install curl -y


# Scripts
RUN mkdir -p /srv/jobs
COPY backup_script.sh /srv/jobs/
RUN chmod 774 /srv/jobs/backup_script.sh

RUN chmod a=rwx,u+t /srv/jobs
RUN chown -R 1001:1001 /srv/jobs

# Backup Folder
RUN mkdir -p /var/backup/mysql

RUN chmod a=rwx,u+t /var/backup/mysql
RUN chown -R 1001:1001 /var/backup/mysql

USER 1001
# overwrite entrypoint
ENTRYPOINT ["/usr/bin/env"]
