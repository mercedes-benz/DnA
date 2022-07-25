FROM openjdk:8-slim
LABEL hive-metastore-version="3.0.0"
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
ARG HADOOP_VERSION=3.2.0
RUN apt-get update && \
apt-get install -y curl postgresql-client --no-install-recommends && \
apt-get clean && \
rm -rf /var/lib/apt/lists/*

# Set Hadoop/HiveMetastore variables and Classpath
ENV HADOOP_HOME="/opt/hadoop"
ENV METASTORE_HOME="/opt/hive-metastore"
ENV HADOOP_CLASSPATH="${HADOOP_HOME}/share/hadoop/tools/lib/*:${METASTORE_HOME}/lib"
ENV PATH="${HADOOP_HOME}/bin:${METASTORE_HOME}/lib/:${HADOOP_HOME}/share/hadoop/tools/lib/:/opt/postgresql-jdbc.jar:${PATH}"
RUN groupadd hive --gid 1000
RUN useradd hive --uid 1000 --gid 1000

# Download and extract the Hadoop binary package.
RUN curl -s https://archive.apache.org/dist/hadoop/core/hadoop-$HADOOP_VERSION/hadoop-$HADOOP_VERSION.tar.gz \                                 
| tar xz -C /opt/ \
&& ln -s ${HADOOP_HOME}-$HADOOP_VERSION ${HADOOP_HOME} \
&& rm -r ${HADOOP_HOME}/share/doc

# Add S3a jars to the Hadoop classpath
RUN cp ${HADOOP_HOME}/share/hadoop/tools/lib/hadoop-aws* ${HADOOP_HOME}/share/hadoop/common/lib/ && \
cp ${HADOOP_HOME}/share/hadoop/tools/lib/aws-java-sdk* ${HADOOP_HOME}/share/hadoop/common/lib/

# Download and install the standalone metastore binary
# (Standalone Metastore is available after 3.0.0 version)

RUN curl -s http://apache.uvigo.es/hive/hive-standalone-metastore-3.0.0/hive-standalone-metastore-3.0.0-bin.tar.gz \                           
| tar xz -C /opt/ \
&& ln -s /opt/apache-hive-metastore-3.0.0-bin ${METASTORE_HOME}

# Add jars to the Hive Metastore classpath
RUN cp ${HADOOP_HOME}/share/hadoop/tools/lib/hadoop-aws* ${METASTORE_HOME}/lib/ && \
cp ${HADOOP_HOME}/share/hadoop/tools/lib/aws-java-sdk* ${METASTORE_HOME}/lib/

# Download and install the postgres connector used by HiveMetastore
RUN curl -s https://jdbc.postgresql.org/download/postgresql-42.2.24.jar -o postgresql-42.2.24.jar && mv postgresql-42.2.24.jar /opt/postgresql-jdbc.jar
RUN cp /opt/postgresql-jdbc.jar ${HADOOP_HOME}/share/hadoop/common/lib/ && chmod -R 775 ${HADOOP_HOME}/share/hadoop/common/lib/*
RUN cp /opt/postgresql-jdbc.jar ${METASTORE_HOME}/lib/ && chmod -R 775 ${METASTORE_HOME}/lib/*
COPY entrypoint.sh ${METASTORE_HOME}/bin/
RUN chmod 775 ${METASTORE_HOME}/bin/entrypoint.sh

#RUN chown -R hive:hive ${HADOOP_HOME} ${METASTORE_HOME} /opt/postgresql-jdbc.jar
RUN chown -R hive:hive /opt

# Metastore URI Port
EXPOSE 9083
WORKDIR ${METASTORE_HOME}
USER hive

# Apply Hive custom configurations and start Metastore Service through entrypoint.sh file
# We need to use the exec form to avoid running our command in a subshell and omitting signals,
# thus being unable to shut down gracefully:
# https://docs.docker.com/engine/reference/builder/#entrypoint                                                                                 
# Also we need to use relative path, because the exec form does not invoke a command shell,
# thus normal shell processing does not happen:
# https://docs.docker.com/engine/reference/builder/#exec-form-entrypoint-example                                                               

CMD ["bin/entrypoint.sh"]