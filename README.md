Spark Pentaho API
=

Generate RESTful APIs for your Pentaho Reports automatically from
a YAML configuration file.

## Basic Usage

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
    path: /customer_report
    file: ./customer_report.prpt
    parameters:
      - name: customer_id
        type: java.lang.Long
        required: true
        default: 0
```

Running the following command will start a webserver at port 4567

```bash
$ bin/spr serve configuration.yml
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

**All output is UTF-8 encoded**

An end-point is generated that allows you to see what parameters are accepted 
by the report.

For example, you could run the following request: 

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

## Generating Configuration

Let's say you have too many reports you want to expose via an API or you're just too busy to write up the YAML
configuration by hand you can **automagically generate** the YAML configuration using the same binary!

Let's assume you have reports in a directory called `/var/pentaho_reports`. You can use the following
command to generate a YAML file called `my_api.yml` that contains basic configuration for the reports
in that directory.

> NOTE: The directory must contain valid Pentaho report templates but may also contain other files. The
> generator only picks up the Pentaho files

```sh
$ bin/spr generate /var/pentaho_reports my_api.yml
```

You can then use the generated YAML file to run your API and you didn't have to write anything!

```sh
$ bin/spr serve my_api.yml
```

The generated configuration file does not configure backup and authentication - so if you
need those features you have to add them in yourself. See the *Advanced Configuration* section, below.

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
    username: foo
    password: foo123
```

#### Multiple Users authentication

In order to allow multiple usernames and passwords to authenticate to the API you can use
the `users` key in the basicAuth configuration

```yaml
basicAuth:
  users:
    - username: foo
      password: foo123
    - username: john
      password: john123
```

## Building

You will need a minimum of [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 
and [Maven 3](https://maven.apache.org/) to build the project.

The project uses the [assembly](https://maven.apache.org/plugins/maven-assembly-plugin/usage.html) 
to create both a zip and tar archive
that contains the compiled application, all it's dependencies and the executable 
scripts; `bin/spr.bat` file for Windows and a bash (`bin/spr`)
file for Linux.

```sh
$ git clone https://github.com/creditdatamw/spark-pentaho-report.git
$ cd spark-pentaho-report
$ mvn clean install
$ mvn assembly:assembly
```

The archives will be created in the `target` directory.

## Note on MySQL database drivers

The project does not ship with the database driver for MySQL so you will have problems
if your reports connect to a MySQL database. You can fix this by adding the MySQL 
dependency to the dependencies section in the `pom` file on your own fork of the repo.

Alternatively, assuming you have a tarball build of `spark-pentaho-report` you can add 
the MySQL jar file in the `lib` directory - this should work.

> NOTE: We will not accept pull requests to add MySQL as a dependency in the pom.

You may add something like:

```xml
<dependency>
	<groupId>mysql</groupId>
	<artifactId>mysql-connector-java</artifactId>
	<version>5.1.42</version>
</dependency>
```

## CONTRIBUTING

See the [`CONTRIBUTING.md`](CONTRIBUTING.md) file for more information.



This is not an official Credit Data CRB Ltd product  - just code that 
happens to be owned by Credit Data CRB Ltd.

---

Copyright (c) 2017, Credit Data CRB Ltd