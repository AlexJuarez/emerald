<div class="bs-callout bs-callout-danger">

### Database Configuration is Required

Before continuing please follow the steps below to configure your database connection and run the necessary migrations.

* Create the database for your application.
* Update the connection URL in the `profiles.clj` file with your database name and login.
* Run `lein run migrate` in the root of the project to create the tables.
* Restart the application.

</div>

### Managing Your Middleware

Request middleware functions are located under the `emerald.middleware` namespace.
A request logging helper called `log-request` is defined below:

```clojure
(defn log-request [handler]
  (fn [req]
    (timbre/debug req)
    (handler req)))
```

This namespace also defines two vectors for organizing the middleware called `development-middleware` and `production-middleware`.
Any middleware that you only wish to run in development mode, such as `log-request`, should be added to the first vector.

### Here are some links to get started

1. [HTML templating](http://www.luminusweb.net/docs/html_templating.md)
2. [Accessing the database](http://www.luminusweb.net/docs/database.md)
3. [Serving static resources](http://www.luminusweb.net/docs/static_resources.md)
4. [Setting response types](http://www.luminusweb.net/docs/responses.md)
5. [Defining routes](http://www.luminusweb.net/docs/routes.md)
6. [Adding middleware](http://www.luminusweb.net/docs/middleware.md)
7. [Sessions and cookies](http://www.luminusweb.net/docs/sessions_cookies.md)
8. [Security](http://www.luminusweb.net/docs/security.md)
9. [Deploying the application](http://www.luminusweb.net/docs/deployment.md)
