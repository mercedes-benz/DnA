# latest trino image compatible with latest immuta plugin
ARG baseimage=390
FROM trinodb/trino:$baseimage

ARG IMMUTA_PLUGIN_PATH=immuta-trino-plugin-v2

RUN mkdir /usr/lib/trino/plugin/immuta
RUN chown trino:trino /usr/lib/trino/plugin/immuta
COPY --chown=trino:trino $IMMUTA_PLUGIN_PATH /usr/lib/trino/plugin/immuta
USER 0
RUN mkdir -p /home/trino
RUN chown -R trino:trino /home/trino
USER trino