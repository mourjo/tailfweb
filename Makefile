all: dependencies clean buildjar runjar

buildjar:
	lein do clean, compile, uberjar

runjar:
	java -jar target/tailfweb.jar 8901

clean:
	lein do clean, uberjar

dependencies:
	bash install.sh

run: clean dependencies buildjar runjar

cleanrepl:
	lein do clean, compile, repl

repl:
	lein repl

observe: dependencies buildjar
	java -cp target/tailfweb.jar tailfweb.observer 8901

.PHONY: all
