# tailfweb

The goal of this project is to implment the following UNIX command:
```
tail -f myfile.txt
```

This is to be done over the web via websockets. That is, connected
clients should get a stream of updates after seeing the current last
10 lines.

This simple implementation assumes the following:
- Lines are only appended to the end of the file, no other
  modification is supported.
- This implementation is tested only on ASCII files.

## Dependencies

Dependencies can be installed via `make dependencies`

[leiningen]: https://github.com/technomancy/leiningen

## Running
To run the server, use `make` at the root of the project -- this will
start the server on `localhost:8901`

## A few words about the implemntation

The implementation is done in `Clojure` with a few simple components
- A thread that polls a file (this thread also maintains a circular
  queue of the last ten lines in the file)
- A thread that maintains a list of connected clients
- The above two processes are joined via a CSP-like `channel`

### Improvements that could be made
- Having a multi-file system where the client can announce the file
  name on which it wants `tail -f` to run
- Having a mechanism to halt the file-poller when there are no
  connected clients
- Using compression to transfer data over the network


### Seeing the action
There is an utility included that writes a set of common words to the
file `myfile.txt` and prints it to stdout allowing the observer to see
the browser tailing the file as well as the expected data from
stdout. To see this in action, try

```
make observe
```


## Project structure
```
.
├── Makefile
├── README.md
├── install.sh
├── myfile.txt
├── project.clj
├── resources
│   ├── log4j.properties
│   ├── public
│   │   └── index.html
│   └── words.txt
└── src
    └── tailfweb
        ├── file_watcher.clj
        ├── handler.clj
        └── observer.clj
```

## License

Copyright © 2018 Mourjo
