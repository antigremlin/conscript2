import scopt.mutable.OptionParser

object Config {
  var cleanBoot: Boolean = false
}

object Conscript {
	def main(args: Array[String]) = {
    val parser = new OptionParser("cs", "0.1") {
      opt("clean-boot", "clears boot dir", { Config.cleanBoot = true })
    }
    parser.parse(args)
    if (Config.cleanBoot) println(Config)
	}
}
