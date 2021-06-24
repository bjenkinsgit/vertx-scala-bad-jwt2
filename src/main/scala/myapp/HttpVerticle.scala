package myapp

import org.bouncycastle.jce.provider.BouncyCastleProvider
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.ext.auth.PubSecKeyOptions
import io.vertx.scala.ext.auth.jwt.{JWTAuth, JWTAuthOptions}
import io.vertx.scala.ext.web.{Router, RoutingContext}
import io.vertx.scala.ext.web.handler.JWTAuthHandler
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.{PEMKeyPair, PEMParser}
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter

import java.security.KeyPair

object HttpVerticle {
  if (java.security.Security.getProvider("BC") == null)
    java.security.Security.addProvider(new BouncyCastleProvider())
}

class HttpVerticle extends ScalaVerticle {

  private def sendStatusCode(ctx: RoutingContext, code: Int): Unit = {
    ctx.response.setStatusCode(code).end
  }

  import java.io.IOException
  import java.nio.charset.StandardCharsets
  import java.nio.file.Files
  import java.nio.file.Path
  import java.nio.file.Paths

  object CryptoHelper {
    @throws[IOException]
    def publicKey: String = read("pubkey.pem")

    @throws[IOException]
    def privateKey: String = read("privkey.pem")

    @throws[IOException]
    private def read(file: String) = {
      var path = Paths.get("public-api", file)
      if (!path.toFile.exists) path = Paths.get("..", "public-api", file)
      String.join("\n", Files.readAllLines(path, StandardCharsets.UTF_8))
    }
  }

  override def startFuture(): Future[_] = {

    import java.io.FileReader
    val logger = LoggerFactory.getLogger("HttpVerticle")
    var keypair: KeyPair = null
    val publicKey = CryptoHelper.publicKey
    val privateKey = CryptoHelper.privateKey
    logger.info(s"publicKey=$publicKey\n\n")
    logger.info(s"privateKey=$privateKey\n\n")
    try {
      val r = new FileReader("./public-api/private.pem")

      try {
        keypair = new JcaPEMKeyConverter().getKeyPair(new PEMParser(r).readObject.asInstanceOf[PEMKeyPair])
        //logger.info(s"Successfully DECODED private.pem !\n")
        //logger.info(s"PubKey ->${keypair.getPublic.toString}")
      } catch {
        case e: Exception => logger.error(s"Error DECODING private.pem ->${e.getMessage}")
      }
      if (r != null) r.close()
    } catch {
      case e: Exception => logger.error(s"Error reading private.pem ->${e.getMessage} at dir: ${System.getProperty("user.dir")}")
    }
        val jwtAuth = JWTAuth.create(vertx, JWTAuthOptions().addPubSecKey(PubSecKeyOptions()
          .setAlgorithm("RS256")
          .setPublicKey("-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxPSbCQY5mBKFDIn1kggv\n" +
            "Wb4ChjrctqD4nFnJOJk4mpuZ/u3h2ZgeKJJkJv8+5oFO6vsEwF7/TqKXp0XDp6IH\n" +
            "byaOSWdkl535rCYR5AxDSjwnuSXsSp54pvB+fEEFDPFF81GHixepIbqXCB+BnCTg\n" +
            "N65BqwNn/1Vgqv6+H3nweNlbTv8e/scEgbg6ZYcsnBBB9kYLp69FSwNWpvPmd60e\n" +
            "3DWyIo3WCUmKlQgjHL4PHLKYwwKgOHG/aNl4hN4/wqTixCAHe6KdLnehLn71x+Z0\n" +
            "SyXbWooftefpJP1wMbwlCpH3ikBzVIfHKLWT9QIOVoRgchPU3WAsZv/ePgl5i8Co\n" +
            "qwIDAQAB\n" +
            "-----END PUBLIC KEY-----"))
          .addPubSecKey(PubSecKeyOptions()
          .setAlgorithm("RS256")
          .setSecretKey("-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDE9JsJBjmYEoUM\n" +
            "ifWSCC9ZvgKGOty2oPicWck4mTiam5n+7eHZmB4okmQm/z7mgU7q+wTAXv9Oopen\n" +
            "RcOnogdvJo5JZ2SXnfmsJhHkDENKPCe5JexKnnim8H58QQUM8UXzUYeLF6khupcI\n" +
            "H4GcJOA3rkGrA2f/VWCq/r4fefB42VtO/x7+xwSBuDplhyycEEH2Rgunr0VLA1am\n" +
            "8+Z3rR7cNbIijdYJSYqVCCMcvg8cspjDAqA4cb9o2XiE3j/CpOLEIAd7op0ud6Eu\n" +
            "fvXH5nRLJdtaih+15+kk/XAxvCUKkfeKQHNUh8cotZP1Ag5WhGByE9TdYCxm/94+\n" +
            "CXmLwKirAgMBAAECggEAeQ+M+BgOcK35gAKQoklLqZLEhHNL1SnOhnQd3h84DrhU\n" +
            "CMF5UEFTUEbjLqE3rYGP25mdiw0ZSuFf7B5SrAhJH4YIcZAO4a7ll23zE0SCW+/r\n" +
            "zr9DpX4Q1TP/2yowC4uGHpBfixxpBmVljkWnai20cCU5Ef/O/cAh4hkhDcHrEKwb\n" +
            "m9nymKQt06YnvpCMKoHDdqzfB3eByoAKuGxo/sbi5LDpWalCabcg7w+WKIEU1PHb\n" +
            "Qi+RiDf3TzbQ6TYhAEH2rKM9JHbp02TO/r3QOoqHMITW6FKYvfiVFN+voS5zzAO3\n" +
            "c5X4I+ICNzm+mnt8wElV1B6nO2hFg2PE9uVnlgB2GQKBgQD8xkjNhERaT7f78gBl\n" +
            "ch15DRDH0m1rz84PKRznoPrSEY/HlWddlGkn0sTnbVYKXVTvNytKSmznRZ7fSTJB\n" +
            "2IhQV7+I0jeb7pyLllF5PdSQqKTk6oCeL8h8eDPN7awZ731zff1AGgJ3DJXlRTh/\n" +
            "O6zj9nI8llvGzP30274I2/+cdwKBgQDHd/twbiHZZTDexYewP0ufQDtZP1Nk54fj\n" +
            "EpkEuoTdEPymRoq7xo+Lqj5ewhAtVKQuz6aH4BeEtSCHhxy8OFLDBdoGCEd/WBpD\n" +
            "f+82sfmGk+FxLyYkLxHCxsZdOb93zkUXPCoCrvNRaUFO1qq5Dk8eftGCdC3iETHE\n" +
            "6h5avxHGbQKBgQCLHQVMNhL4MQ9slU8qhZc627n0fxbBUuhw54uE3s+rdQbQLKVq\n" +
            "lxcYV6MOStojciIgVRh6FmPBFEvPTxVdr7G1pdU/k5IPO07kc6H7O9AUnPvDEFwg\n" +
            "suN/vRelqbwhufAs85XBBY99vWtxdpsVSt5nx2YvegCgdIj/jUAU2B7hGQKBgEgV\n" +
            "sCRdaJYr35FiSTsEZMvUZp5GKFka4xzIp8vxq/pIHUXp0FEz3MRYbdnIwBfhssPH\n" +
            "/yKzdUxcOLlBtry+jgo0nyn26/+1Uyh5n3VgtBBSePJyW5JQAFcnhqBCMlOVk5pl\n" +
            "/7igiQYux486PNBLv4QByK0gV0SPejDzeqzIyB+xAoGAe5if7DAAKhH0r2M8vTkm\n" +
            "JvbCFjwuvhjuI+A8AuS8zw634BHne2a1Fkvc8c3d9VDbqsHCtv2tVkxkKXPjVvtB\n" +
            "DtzuwUbp6ebF+jOfPK0LDuJoTdTdiNjIcXJ7iTTI3cXUnUNWWphYnFogzPFq9CyL\n" +
            "0fPinYmDJpkwMYHqQaLGQyg=\n")))
    //    val jwtAuth = JWTAuth.create(vertx, JWTAuthOptions().addPubSecKey(PubSecKeyOptions()
    //      .setAlgorithm("RS256")
    //      .setPublicKey(publicKey))
    //      .addPubSecKey(PubSecKeyOptions()
    //      .setAlgorithm("RS256")
    //      .setSecretKey(privateKey)))
    //
    //    val jwtHandler = JWTAuthHandler.create(jwtAuth)

    //  CheckUser
    def checkUser(ctx: RoutingContext): Unit = {
      val subject = ctx.user.get.principal.getString("sub")
      if (!ctx.pathParam("username").equals(subject)) sendStatusCode(ctx, 403)
      else ctx.next
    }

    //Create a router to answer GET-requests to "/hello" with "world"
    val router = Router.router(vertx)
    val route = router
      .get("/hello")
      //.handler(jwtHandler)
      .handler(_.response().end("world"))

    vertx
      .createHttpServer()
      .requestHandler(router.accept _)
      .listenFuture(8666, "0.0.0.0")
  }
}