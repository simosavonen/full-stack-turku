# full-stack-turku

Code for front- and backend for Full Stack course at Uni. of Turku

Built on top of Shadow-CLJS.

# Backend

```bash
    cd foli-server
    npm install
    shadow-cljs watch app
    # and in another terminal
    node target/main.js
``` 

# Frontend

```bash
    cd foli-client
    npm install
    mkdir target && cp assets/index.html target
    shadow-cljs watch app
    # open localhost:8080 in browser.
```
