# As we were facing troubles building the original image we simply copied the changed files and rebuild
FROM dna/kfserving/storage-initializer:v0.6.1

WORKDIR /work

USER 1000

ENTRYPOINT ["/storage-initializer/scripts/initializer-entrypoint"]