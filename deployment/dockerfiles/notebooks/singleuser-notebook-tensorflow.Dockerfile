ARG defaultimage=pyspark-notebook-default:latest
FROM $defaultimage

USER root

RUN PYCURL_SSL_LIBRARY=openssl pip3 install tensorflow==2.3.0

USER ${NB_UID}
WORKDIR "${HOME}"
