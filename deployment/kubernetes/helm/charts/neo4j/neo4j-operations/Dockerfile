FROM golang:1.22-alpine
RUN apk --no-cache add curl \
    && addgroup --gid 7474 --system neo4j \
    && adduser --uid 7474 --system --no-create-home --home "/go" --ingroup neo4j neo4j
WORKDIR neo4j-operations
COPY go.mod go.mod
COPY go.sum go.sum
COPY *.go ./

#RUN go get -v -t ./... \
RUN go mod download && go mod verify \
    && go build -v -o /go/neo4j_operations_linux \
    && chown -R neo4j:neo4j /go && chmod -R 777 /go
CMD ["/go/neo4j_operations_linux"]
