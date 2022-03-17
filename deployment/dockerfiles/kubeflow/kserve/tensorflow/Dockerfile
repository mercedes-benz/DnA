FROM tensorflow/serving:1.14.0

RUN chmod 777 /usr/bin/tf_serving_entrypoint.sh

USER 1000

ENTRYPOINT ["/usr/bin/tf_serving_entrypoint.sh"]