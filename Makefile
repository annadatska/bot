VERSION=$(shell git describe --tags --abbrev=0)-$(shell git rev-parse --short HEAD)
TARGETOS=linux

format:
	gofmt -s -w ./

get:
	go get

lint:
	golint

test:
	go test -v

build: format
	CGO_ENABLED=0 GOOS=${TARGETOS} GOARCH=${shell dpkg --print-architecture} go build -v -o bot -ldflags "-X="github.com/annadatska/bot/cmd.appVersion=${VERSION}