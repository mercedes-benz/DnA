FROM golang:1.19-alpine

WORKDIR /app

COPY go.mod ./
COPY go.sum ./
RUN go mod download

COPY *.go ./

RUN go build -o /vault-unseal

# EXPOSE 8080
USER 1000

CMD [ "/vault-unseal" ]