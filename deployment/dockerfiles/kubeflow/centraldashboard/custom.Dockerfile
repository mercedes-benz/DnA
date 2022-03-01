# As we were facing troubles building the original image we simply copied the changed files and rebuild
FROM dna/j1r0q0g6/notebooks/central-dashboard:v1.4

COPY manage-users-view.pug public/components/manage-users-view.pug
COPY api_workgroup.ts app/api_workgroup.ts

RUN npm --production=false install

RUN npm rebuild && \
    if [ "$(uname -m)" = "aarch64" ]; then \
        export CFLAGS=-Wno-error && \
        export CXXFLAGS=-Wno-error && \
        npm install; \
    else \
        npm install; \
    fi && \
    npm run build && \
    npm prune --production

EXPOSE 8082
ENTRYPOINT ["npm", "start"]

