package myapp

import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.DeploymentOptions
import org.slf4j.LoggerFactory

import scala.concurrent.Future

class EmptyVerticle extends ScalaVerticle {
  val logger = LoggerFactory.getLogger("EmptyVerticle")
  override def start {
    logger.info("EmptyVerticle Start")
  }

  override def stop(): Unit = logger.info("Stop")
}
