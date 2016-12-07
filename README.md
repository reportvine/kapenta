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

## Authors

* Zikani Nyirenda Mwase

---

Copyright (c) 2016, Credit Data CRB