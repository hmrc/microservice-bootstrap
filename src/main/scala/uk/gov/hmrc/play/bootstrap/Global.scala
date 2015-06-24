package uk.gov.hmrc.play.bootstrap

import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import play.api._
import play.api.mvc._
import uk.gov.hmrc.play.audit.filters.{AuditFilter => AuditFiltering}
import uk.gov.hmrc.play.audit.http.config.{ErrorAuditingSettings, LoadAuditingConfig}
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector => Auditing}
import uk.gov.hmrc.play.config.{AppName, ControllerConfig, RunMode}
import uk.gov.hmrc.play.filters._
import uk.gov.hmrc.play.graphite.GraphiteConfig
import uk.gov.hmrc.play.http.logging.filters.LoggingFilter
import uk.gov.hmrc.play.http.ws.{WSDelete, WSGet, WSPatch, WSPost, WSPut}

object WSHttp extends WSGet with WSPut with WSPost with WSDelete with WSPatch with AppName with RunMode {
  override val auditConnector = AuditConnector
}

object AuditConnector extends Auditing with AppName with RunMode {
  override lazy val auditingConfig = LoadAuditingConfig(s"$env.auditing")
}


object DefaultFilters {

  object AuditFilter extends AuditFiltering with AppName {
    override val auditConnector = AuditConnector
    override def controllerNeedsAuditing(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsAuditing
  }

  object MicroserviceLoggingFilter extends LoggingFilter {
    override def controllerNeedsLogging(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsLogging
  }

  object ControllerConfiguration extends ControllerConfig {
    lazy val controllerConfigs = Play.current.configuration.underlying.as[Config]("controllers")
  }

  def apply(): Seq[EssentialFilter] = Seq(
    AuditFilter,
    MicroserviceLoggingFilter,
    NoCacheFilter,
    RecoveryFilter)
}

abstract class DefaultGlobal(filters: Seq[EssentialFilter]) extends WithFilters(filters: _*)  with GraphiteConfig with AppName {

  override def onStart(app: Application) {
    Logger.info(s"Starting microservice : $appName : in mode : ${app.mode}")
    super.onStart(app)
  }

  // Play 2.0 doesn't support trailing slash: http://play.lighthouseapp.com/projects/82401/tickets/98
  override def onRouteRequest(request: RequestHeader) = super.onRouteRequest(request).orElse {
    Some(request.path).filter(_.endsWith("/")).flatMap(p => super.onRouteRequest(request.copy(path = p.dropRight(1))))
  }
}

abstract class RestGlobal extends DefaultGlobal(DefaultFilters()) with JsonErrorHandling with ErrorAuditingSettings

object RestGlobal extends RestGlobal with RunMode {
  override val auditConnector = AuditConnector

  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig(s"$env.microservice.metrics")
}
