FROM openjdk:11-slim

ARG HADOOP_VERSION=3.2.3

RUN apt-get update && apt-get install -y curl postgresql-client wget procps --no-install-recommends && \
        rm -rf /var/lib/apt/lists/*

# Download and extract the Hadoop binary package.
RUN curl https://downloads.apache.org/hadoop/common/hadoop-$HADOOP_VERSION/hadoop-$HADOOP_VERSION.tar.gz \
        | tar xvz -C /opt/  \
        && ln -s /opt/hadoop-$HADOOP_VERSION /opt/hadoop \
        && rm -r /opt/hadoop/share/doc

# Add S3a jars to the classpath using this hack.
RUN ln -s /opt/hadoop/share/hadoop/tools/lib/hadoop-aws* /opt/hadoop/share/hadoop/common/lib/ && \
    ln -s /opt/hadoop/share/hadoop/tools/lib/aws-java-sdk* /opt/hadoop/share/hadoop/common/lib/

# Set necessary environment variables.
ENV HADOOP_HOME="/opt/hadoop"
ENV PATH="/opt/spark/bin:/opt/hadoop/bin:${PATH}"

# Download and install the standalone metastore binary.
RUN curl https://repo1.maven.org/maven2/org/apache/hive/hive-standalone-metastore/3.1.2/hive-standalone-metastore-3.1.2-bin.tar.gz \
        | tar xvz -C /opt/ \
        && ln -s /opt/apache-hive-metastore-3.1.2-bin /opt/hive-metastore

#install minio client
RUN wget https://dl.min.io/client/mc/release/darwin-amd64/mc
RUN chmod +x mc

ARG POSTGRESQL_JDBC_VERSION=42.2.24

# Install PostgreSQL JDBC driver
RUN curl -fSL https://jdbc.postgresql.org/download/postgresql-$POSTGRESQL_JDBC_VERSION.jar -o /opt/hive-metastore/lib/postgresql-jdbc.jar

RUN rm /opt/hive-metastore/lib/guava-19.0.jar && \
        ls -lah /opt/hadoop/share/hadoop/common/lib/ && \
        cp /opt/hadoop/share/hadoop/common/lib/guava-27.0-jre.jar /opt/hive-metastore/lib/ && \
        cp /opt/hadoop/share/hadoop/tools/lib/hadoop-aws-$HADOOP_VERSION.jar /opt/hive-metastore/lib/ && \
        cp /opt/hadoop/share/hadoop/tools/lib/aws-java-sdk-bundle-* /opt/hive-metastore/lib/