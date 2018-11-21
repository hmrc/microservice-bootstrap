microservice-bootstrap
======================

[![Build Status](https://travis-ci.org/hmrc/microservice-bootstrap.svg)](https://travis-ci.org/hmrc/microservice-bootstrap) [ ![Download](https://api.bintray.com/packages/hmrc/releases/microservice-bootstrap/images/download.svg) ](https://bintray.com/hmrc/releases/microservice-bootstrap/_latestVersion)

## This library is in maintanance mode. We recommend migrating to bootstrap-play-{25,26}.

This library implements a basic Play Global object and related functionality for frontend applications.

### Creating a Global object for your microservice application

Simply create an object extending `DefaultMicroserviceGlobal`. That will provide you with the common filters and error handling.
You can also override `microserviceFilters` attribute if you need to alter the default set of filters.

### Metrics plugin

To enable the Metrics plugin in your application, add this line to your `play.plugins` file:

```scala
1:com.kenshoo.play.metrics.MetricsPlugin
```

and a block similar to this in your Play application config file:

```scala
metrics {
    name = ${appName}
    rateUnit = SECONDS
    durationUnit = SECONDS
    showSamples = true
    jvm = true
    enabled = true
}
```

You can also enable the plugin's admin servlet by adding this line to your `routes` file:

```scala
GET     /admin/metrics          com.kenshoo.play.metrics.MetricsController.metrics
```

and configure the controller in your application conf file:

```scala
controllers {
    com.kenshoo.play.metrics.MetricsController {
        needsAuth = false
        needsLogging = false
        needsAuditing = false
    }
}
```

#### Publishing metrics to Graphite

To enable Graphite publisher in your application, add a block like this to your application `conf` file:

```scala
microservice {
    metrics {
        graphite {
            host = graphite
            port = 2003
            prefix = play.${appName}.
            enabled = true
        }
    }
```

and point your Global object (inherited from `DefaultMicroserviceGlobal` ) to it

```scala
override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig("microservice.metrics")
```

### Installing

Add the following to your SBT build:
```scala
resolvers += Resolver.bintrayRepo("hmrc", "releases")

libraryDependencies += "uk.gov.hmrc" %% "microservice-bootstrap" % "[INSERT-VERSION]"
```

## License ##

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
