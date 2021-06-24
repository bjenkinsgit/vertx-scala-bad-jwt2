package myapp
import io.vertx.lang.scala.ScalaVerticle
import org.slf4j.LoggerFactory
import scala.concurrent.Future
import scala.util.{Failure, Success}

class Deployer extends ScalaVerticle {
  val logger = LoggerFactory.getLogger("Deployer")

  override def start {
    var delay = 1000
    vertx.setTimer(delay, id => deploy)
  }

  private def deploy {
    vertx.deployVerticleFuture(new HttpVerticle)
      .onComplete {
        case Success(s) => {
          logger.info(s"HttpVerticle has started with id $s")
          //vertx.setTimer(5000, id => undeploy(s))
        }
        case Failure(t) => logger.error(s"HttpVerticle FAILED to start!")
      }
  }

  private def undeploy(s: String): Unit = {
    vertx.undeployFuture(s)
      .onComplete {
        case Success(msg) => logger.info(s"Successfully UN-deployed $s")
        case Failure(t) => logger.error(s"UN-deploy FAILED -> ${t.getMessage}")
      }
  }
}
