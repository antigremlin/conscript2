import dispatch.{Http, Req, as, host}
import net.liftweb.json._
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global

class Github(owner: String, repo: String) {

  val base: Req = host("api.github.com").secure / "repos" / owner / repo
  
  val repoObj = json(base)
  def ref(br: String) = json(base / "git" / "refs" / "heads" / br)
  def tree(hash: String) = json(base / "git" / "trees" / hash <<? Map("recursive" -> "1"))

  val ScriptRegex = "^src/main/conscript/([^/]+)/launchconfig$".r
  val TestRegex = "^src/main".r

  def json(req: Req): Future[JValue] = {
    val str = Http(req OK as.String)
    for (json <- str) yield parse(json)
  }

  def masterBranch(repo: Future[JValue]): Future[String] =
    repo map { _ \ "master_branch"} map { case JString(branch) => branch }

  def sha(ref: Future[JValue]): Future[String] =
    ref map { _ \ "object" \ "sha" } map { case JString(hash) => hash }

  def paths(tree: Future[JValue]) =
    tree map { _ \ "tree"} map {
      for {
        JObject(file) <- _
        JField("sha", JString(h)) <- file
        JField("path", JString(p)) <- file
        if TestRegex.findFirstIn(p).isDefined
      } yield (p,h)
    }

  def lookup =
    for {
      b <- masterBranch(repoObj)
      h <- sha(ref(b))
      p <- paths(tree(h))
    } yield p

}
