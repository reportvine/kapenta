Spark Pentaho API
=

Generate restful apis for your pentaho reports automatically from
a YAML configuration file.

Let's assume you have designed a report called `customer_report` in Pentaho Report Designer
and you want to make this report available via an *API*.

You can create this configuration  and save it in a yaml file called `configuration.yml`

```yaml
# configuration.yml
host: localhost
port: 4567
apiRoot: /api
reports:
  - name: Customer Report
    version: "1.0.0"
    path: "./customer_report.prpt"
    parameters:
      - name: customer_id
        type: java.lang.Long
        required: true
        default: 0
```

Running the following command will start a webserver at port 4567

```bash
$ java -jar spark-pentaho-report.jar configuration.yml
```

The API created will have two end-points, one for generating your report.
Example to generate a report for customer with id #1 we'd have:

`http://localhost:4567/api/customer_report?customer_id=1`

This by default will result in an html report being generated. The server currently
supports three output types for a report: PDF, HTML and TEXT

In order to get a PDF report - append `.pdf` to the path before adding the query
parameters or set the `Accept` header to `application/pdf`.

For example the same request above can be made a pdf by performing the request in a browser:

`http://localhost:4567/api/customer_report.pdf?customer_id=1`

In order to get a Text report - append `.txt` to the path before adding the query
parameters or set the `Accept` header to `text/plain`.

All output is UTF-8 encoded

The other end-point generated allows you to see what parameters are accepted 
by the report.

Invoking 

```bash
$ curl -G http://localhost:4567/api/customer_report/info
```

In this case will give you 

```json
{
  "reportName" : "Customer Report",
  "version" : "1.0.0",
  "parameters": [
    { "name": "customer_id", "type": "Long", "default" : 0, "required": true }
  ]
}
```

## Advanced Configuration

## Server Configuration

You can configure the server to bind to a different ip address than `localhost` and
different port than the default `4567`.

For example

```yaml
host: 192.168.1.45
port: 8172
```

## Report Backups

If you want to be able to store a backup of the reports clients/users have generated via the API
you can do so via the following configuration.

The files are saved with the output type that's sent to the requesting clients and are prepended
with the timestamp they are generated at using `System.currentTimeMillis()`.

For example a generated text report for `hello report` would have a backup with name `1483718274331-helloreport.txt`

> **NOTE**: Set the directory to a directory that the user running the process has write permissions to.

```yaml
# Configuration for backup of generated reports
backup:
  # Where to store the generated reports
  directory: /var/log
  # If the directory should have subdirectories for each day
  rollingBackup: true
```

### Rolling Backups

> NOTE: Currently rolling backups are not supported so all files will be in one directory

Rolling backups creates a directory per day and stores the reports in the directory
with the date they were generated. The directories are named in `YYYY-MM-DD` format.

For example: `2017-01-01`

### Configuring Basic Authentication

In order to add some level of security to the API you can configure HTTP Basic Authentication via
the configuration file.

#### Single User authentication

```yaml
basicAuth:
  user:
    username: zikani
    password: zikani123
```

#### Multiple Users authentication

In order to allow multiple usernames and passwords to authenticate to the API you can use
the `users` key in the basicAuth configuration

```yaml
basicAuth:
  users:
    - username: zikani
      password: zikani123
    - username: john
      password: john123
```

## TODO

* Add tests for other components
* Fix image resource loading issue in generated reports
* Allow users to change the SQL Datasource of a report based on configuration or environment variables, or both

## Authors

* Zikani Nyirenda Mwase

---

Copyright (c) 2016, Credit Data CRB