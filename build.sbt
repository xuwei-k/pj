import sbtrelease.ReleaseStateTransformations._

publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)

val Scala212 = "2.12.4"

val updateLaunchconfig = TaskKey[File]("updateLaunchconfig")

val tagName = Def.setting{
  s"v${if (releaseUseGlobalVersion.value) (version in ThisBuild).value else version.value}"
}
val tagOrHash = Def.setting{
  if(isSnapshot.value) gitHash() else tagName.value
}

def gitHash(): String =
  sys.process.Process("git rev-parse HEAD").lines_!.head

val unusedWarnings = (
  "-Ywarn-unused" ::
  "-Ywarn-unused-import" ::
  Nil
)

val commonSettings = Seq(
  scalaVersion := Scala212,
  scalacOptions ++= (
    "-deprecation" ::
    "-unchecked" ::
    "-Xlint" ::
    "-Xfuture" ::
    "-language:existentials" ::
    "-language:higherKinds" ::
    "-language:implicitConversions" ::
    "-Yno-adapted-args" ::
    Nil
  ),
  scalacOptions ++= PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion.value)){
    case Some((2, v)) if v >= 11 => unusedWarnings
  }.toList.flatten,
  scalacOptions in (Compile, doc) ++= {
    val tag = tagOrHash.value
    Seq(
      "-sourcepath", (baseDirectory in LocalRootProject).value.getAbsolutePath,
      "-doc-source-url", s"https://github.com/xuwei-k/pj/tree/${tag}€{FILE_PATH}.scala"
    )
  },
  publishMavenStyle := true,
  publishArtifact in Test := false,
  organization := "com.github.xuwei-k",
  licenses := Seq("MIT" -> url(s"https://github.com/xuwei-k/pj/blob/${tagOrHash.value}/LICENSE")),
  homepage := some(url("https://github.com/xuwei-k/pj/#readme")),
  crossScalaVersions := Seq("2.10.7", "2.11.12", Scala212),
  releaseCrossBuild := true,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runTest,
    setReleaseVersion,
    releaseStepTask(updateLaunchconfig),
    commitReleaseVersion,
    tagRelease,
    ReleaseStep(action = Command.process("publishSigned", _), enableCrossBuild = true),
    setNextVersion,
    commitNextVersion,
    ReleaseStep(action = Command.process("sonatypeReleaseAll", _), enableCrossBuild = true),
    pushChanges
  ),
  pomExtra := (
    <scm>
      <url>git@github.com:xuwei-k/pj.git</url>
      <connection>scm:git:git@github.com:xuwei-k/pj.git</connection>
    </scm>
    <developers>
      <developer>
        <id>xuwei-k</id>
        <name>Kenji Yoshida</name>
        <url>https://github.com/xuwei-k</url>
      </developer>
      <developer>
        <id>softprops</id>
        <name>Doug Tangren</name>
        <url>https://github.com/softprops</url>
      </developer>
    </developers>)
) ++ Seq(Compile, Test).flatMap(c =>
  scalacOptions in (c, console) --= unusedWarnings
)

lazy val root = Project(
  "root", file(".")
).settings(
  commonSettings,
  updateLaunchconfig := {
    val mainClassName = (discoveredMainClasses in Compile in app).value match {
      case Seq(m) => m
      case zeroOrMulti => sys.error(s"could not found main class. $zeroOrMulti")
    }
    val launchconfig = s"""[app]
  version: ${(version in app).value}
  org: ${(organization in app).value}
  name: ${(normalizedName in app).value}
  class: ${mainClassName}
[scala]
  version: ${Scala212}
[repositories]
  local
  maven-central
"""
    val f = (baseDirectory in ThisBuild).value / "src/main/conscript/pj/launchconfig"
    IO.write(f, launchconfig)
    f
  },
  publishArtifact := false,
  publish := { },
  publishLocal := { },
  PgpKeys.publishSigned := { },
  PgpKeys.publishLocalSigned := { }
).aggregate(pj, app)

lazy val pj = Project("pj", file("pj")).settings(
  commonSettings,
  name := "pj",
  libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.8.9",
  buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
  buildInfoObject := "PjBuildInfo",
  buildInfoPackage := "pj",
  description := "A pretty printer for json"
).enablePlugins(BuildInfoPlugin)

lazy val app = Project("app", file("app")).settings(
  commonSettings,
  name := "pj-app",
  description := "A conscript interface for prettifying json strings and streams"
).dependsOn(pj).enablePlugins(ConscriptPlugin)
