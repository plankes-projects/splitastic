# Splitastic

Splitastic is an open source self hosted chores/expenses splitting app.

Mobile phones are first class citizens in this app. This means you should use this app on a mobile phone for the best experience.

See it in action here: https://splitastic.epicnerf.com/

## Why another chores/expenses splitting app you ask?
There is no free (no ads), open source or self hosted alternative to all those apps out there.
Splitastic is here to change that!

## Getting Started
The project consists of 2 parts:
* Java Spring server which connects to a MySQL database (needs to be MySQL because of raw queries). See server readme for more information.
* TypeScript Vue PWA client. See client readme for more information.

## Development Notes
#### TODOs:
TODOs are tracked on: https://github.com/plankes-projects/splitastic/projects/1
#### New server API endpoints:
API endpoints are implemented with server/specification/src/main/resources/openapi.yml (see https://swagger.io/tools/swagger-editor/). Afterwards client and server parts are generated from this file (see readme in server/client).
#### Fast login
Use the admin token to enable instant login. See server readme for more details.

## Donate
https://epicnerf.com/donate/
