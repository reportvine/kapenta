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

`http://host:port/api/customer_report?customer_id=1`

This by default will result in an html report being generated. The server currently
supports three output types for a report: PDF, HTML and TEXT

In order to get a PDF report - append `.pdf` to the path before adding the query
parameters or set the `Accept` header to `application/pdf`.

In order to get a Text report - append `.txt` to the path before adding the query
parameters or set the `Accept` header to `text/plain`.

All output is UTF-8 encoded

The other end-point generated allows you to see what parameters are accepted 
by the report.

Invoking 

```bash
$ curl -G http://host:port/api/customer_report/info
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
## TODO

* Don't show SQL errors in the response message
* Add tests for other components
* Fix image resource loading issue in generated reports
* Support basic authentication via `apache-shiro` support or `api-key`
* Support for specifying custom routes in yaml configuration
* Support for limiting output types in the yaml configuration
* Allow users to change the SQL Datasource of a report based on configuration or environment variables, or both

## Authors

* Zikani Nyirenda Mwase

---

Copyright (c) 2016, Credit Data CRB