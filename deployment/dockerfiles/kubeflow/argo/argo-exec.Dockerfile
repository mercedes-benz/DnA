FROM dna/ml-pipeline/argoexec:v3.1.14-patch-license-compliance

USER 0 

RUN chmod a=rwx,u+t /tmp
RUN chown -R 8737:8737 /tmp

RUN mkdir /marshal
RUN chmod a=rwx,u+t /marshal
RUN chown -R 8737:8737 /marshal

USER 8737

ENTRYPOINT [ "argo" ]