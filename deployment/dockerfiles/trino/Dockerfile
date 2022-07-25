# latest trino image compatible with latest immuta plugin
FROM trinodb/trino:377

ARG IMMUTA_PLUGIN_PATH

#RUN mkdir /usr/lib/trino/plugin/immuta
#RUN chown trino:trino /usr/lib/trino/plugin/immuta
COPY --chown=trino:trino $IMMUTA_PLUGIN_PATH /usr/lib/trino/plugin/immuta



