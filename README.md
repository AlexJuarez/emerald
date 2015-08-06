# Emerald

To build emerald for tomcat deployment run `./lein ring uberwar`

## Configuration

The configuration settings for the project can be found in `profiles.clj`.
On a build the a new file called `.lein-env` is created and needs to be placed with the resulting war.

    eg. ~/tomcat/webapps/www
        |--api#crud.war
        |--.lein-env

When the server is built using `./lein ring uberwar` the settings from :profiles/prod are pulled

| Property | Description |
|:---|:---|
| **dbspec** | should have the correct credentials for the postgres database. for details see [kormasql][1] |
| **jdbc-uri** | is the jdbc uri used by the migrations in emerald, currently migrations are run using `./lein run migrate` however these migrations are only for testing |
| **couchbase** | if false, a in memory store will be used instead of couchbase to manage both sessions, and oauth tokens. Switch to true only if a couchbase instance has been properly configured |
| **couchbase-uri** | the uri for the instance of couchbase, currently set to point at `127.0.0.1:11211` for details see [spyglass][2]|
| **auth** | false disables authentication for the routes and user context, so routes depending on the current user id will not work. Eg. `/campaigns/:id/pin` |

> If the auth flag is enabled, the server expects a header containing `authorization: access_token` or a query parameter of `api_key=access_token`


[1]: http://sqlkorma.com/docs#db
[2]: http://clojurememcached.info/articles/getting_started.html

## OAuth and documentation

    For local development the application can be accessed in browser at localhost:3000/,
    For production the application can be accessed at server-url/api/crud/

At the root of this application exists two areas, `localhost:3000/login` or [`server-url/api/crud/login`][3] to login and access your Application area.

To get to the documentation area you can either view a application and click the take me documentation link or you can browse to `localhost:3000/docs`, [`server-url/api/crud/docs`][4]

[3]: https://thorwhal-dev-api.mixpo.com/api/crud/login
[4]: https://thorwhal-dev-api.mixpo.com/api/crud/docs

## Prerequisites

Lein is included in the repository for easy of use.
To update this script see [Leiningen][5].

[5]: https://github.com/technomancy/leiningen

## Running

To start a dev web server for the application, run:

    ./lein run
