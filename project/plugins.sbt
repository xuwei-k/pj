addSbtPlugin("net.databinder" % "conscript-plugin" % "0.3.3")

addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.1")

resolvers ++= Seq(
  "less is" at "http://repo.lessis.me",
  "coda" at "http://repo.codahale.com"
)
