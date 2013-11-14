import dispatch.{Http, url, as}
import java.io.File
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}
import scopt.mutable.OptionParser
import scala.concurrent.ExecutionContext
import scala.language.implicitConversions
import scala.language.reflectiveCalls
import ExecutionContext.Implicits.global

object Config {
  var cleanBoot: Boolean = false
  var setup: Boolean = false
}

object Setup {
  val sbtVersion = "0.13.0"
  val satLaunchAlias = "sbt-launch.jar"
  val sbtLaunchUrl = "http://typesafe.artifactoryonline.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/%s/sbt-launch.jar" format sbtVersion

  val TypesafeRepo = "http://typesafe.artifactoryonline.com/typesafe/ivy-releases"
  val GroupId = "org.scala-sbt"
  val ArtifactId = "sbt-launch"
  val FileName = "sbt-launch.jar"

  implicit def str2paths(a: String) = new {
    def / (b: String) = a + File.separatorChar + b
  }

  def configDir(path: String) = homeDir(".conscript" / path)
  def homeDir(path: String) = new File(System.getProperty("user.home"), path)

  def downloadLauncher: Future[Any] = {
    val req = url(TypesafeRepo / GroupId / ArtifactId / sbtVersion / FileName)
    val jar = configDir("sbt-launch-%s.jar" format sbtVersion)
    Http(req > as.File(jar))
  }
}

object Conscript {
  def main(args: Array[String]) {
    val parser = new OptionParser("cs", "0.1") {
      opt("clean-boot", "clears boot dir", { Config.cleanBoot = true })
      opt("setup", "installs sbt launcher", { Config.setup = true })
    }
    parser.parse(args)

    if (Config.cleanBoot) println(Config)
    if (Config.setup) Setup.downloadLauncher

    val master = new Github("antigremlin", "conscript").lookup

    master onComplete {
      case Success(str) => println(str)
      case Failure(t) => println("%s: %s" format (t.getClass, t.getMessage))
    }
    Await.ready(master, Duration.Inf)
  }
}
