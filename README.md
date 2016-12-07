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

`http://host:port/api/customer_report?customer_id=`

The other end-point generated allows you to see what parameters are accepted 
by the report.

Invoking 

```bash
$ curl -G http://host:port/api/customer_report/params
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

* Add parameter end-point/route generator
* Don't show SQL errors in the response message
* Add tests for other components
* Fix image resource loading issue in generated reports
* Support basic authentication via `apache-shiro` support or `api-key`
* Add extension support i.e. a user should be able to generate a pdf report by appending `.pdf` to the route
* Support for specifying custom routes in yaml configuration
* Support for specifying output types in the yaml configuration
* Allow users to change the SQL Datasource of a report based on configuration or environment variables, or both
* Build artifact via maven-assembly plugin

## Authors

* Zikani Nyirenda Mwase

---

Copyright (c) 2016, Credit Data CRB