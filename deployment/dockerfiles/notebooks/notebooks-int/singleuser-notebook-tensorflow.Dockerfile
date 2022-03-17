FROM dna/int/pyspark-notebook:1.0-default

USER root

RUN PYCURL_SSL_LIBRARY=openssl pip3 install tensorflow==2.3.0

USER ${NB_UID}
WORKDIR "${HOME}"
