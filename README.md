# ![Emerald](http://obsidian.mixpo.com/images/emerald-iconx32.png)Emerald

To build emerald for tomcat deployment run `./lein ring uberwar`
## Initial Setup

##### 1. Build the latest mixpo.com bowser database

- Pull the latest mixpo.com source code and open the db directory

        ./postgresql-install dev # installs the latest postgres and memory settings
        ./db-update data # builds bowser database

##### 2. Install a couchbase instance locally
- Install the couchbase community edition for your OS. Start the couchbase daemon
- Open your web browser to localhost:8091 and set up your username and password. The default settings should be fine (uncheck the replication checkbox), just keep clicking 'next'. **TODO: Add a configuration script for this step**

##### 3. Point mixpo_server.identity to your couchbase instance
- Edit your ~/mixpo_server.identity file and add the line

        couchbase_server_uri=127.0.0.1:11211

## Quickstart

To start a dev web server for the application, run:

    ./lein run

## Configuration

The configuration settings for the project can be found in `profiles.clj`.
On a build the a new file called `.lein-env` is created and needs to be renamed and copyed to `~/tomcat/conf/emerald.config`.

Additionally the built war needs to be copied to

    eg. ~/tomcat/webapps/www
        |--api#crud.war

When the server is built using `./lein ring uberwar` the settings from :profiles/prod are pulled

| Property | Description |
|:---|:---|
| **dbspec** | should have the correct credentials for the postgres database. for details see [kormasql][1] |
| **jdbc-uri** | is the jdbc uri used by the migrations in emerald, currently migrations are run using `./lein run migrate` however these migrations are only for testing |
| **couchbase** | if false, a in memory store will be used instead of couchbase to manage both sessions, and oauth tokens. Switch to true only if a couchbase instance has been properly configured |
| **auth** | false disables authentication for the routes and user context, so routes depending on the current user id will not work. Eg. `/campaigns/:id/pin` |
| **log-path** | set to the path of the log relative to `~/tomcat` for production or `~/` for dev |

> If the auth flag is enabled, the server expects a header containing `authorization: access_token` or a query parameter of `api_key=access_token` or a cookie with the name `access_token`

###Mixpo Identity File

Additionally the couchbase server uri is pulled from `mixpo_server.identity` file, the path is determined by the bash variable `$MIXPO_IDENTITY`, or a default location of `~/mixpo_server.identity`

| Property | Description |
|:---|:---|
| **couchbase_server_uri** | the uri for the instance of couchbase, currently set to point at `127.0.0.1:11211` for details see [spyglass][2]|

    eg. couchbase_server_uri=127.0.0.1:11211




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
