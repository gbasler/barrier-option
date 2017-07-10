import sbt._
import Keys._
import sbt.Reference._

object build extends Build {

  import DependencyManagement._

  // Settings shared by all sub-projects.
  val standardSettings: Seq[Project.Setting[_]] =
    Seq[Project.Setting[_]](
      ivyXML := DependencyManagement.ivyExclusionsAndOverrides,
      scalaVersion := "2.12.2",
      resolvers ++= Seq(
        "Nexus Public" at "http://nexus.fpprod.corp/nexus/content/groups/public",
        "Nexus Public2" at "http://nexus.fpprod.corp/nexus/content/groups/public",
        "snapshots" at "http://scala-tools.org/repo-snapshots",
        "releases" at "http://scala-tools.org/repo-releases")
    )

  //
  // PROJECTS
  //

  // Parent Project, it aggregates all others.
  lazy val jnaTest = Project(
    id = "barrier-option",
    base = file("."),
    settings = Defaults.defaultSettings ++ standardSettings,
    aggregate = Seq[ProjectReference](scalazSbtSandboxCore)
  )

  lazy val scalazSbtSandboxCore = Project(
    id = "barrier-option-core",
    base = file("barrier-option-core"),
    settings = Defaults.defaultSettings ++ standardSettings ++ Seq(
      libraryDependencies ++= Seq(CommonsMath3) ++ Specs.dependencies,
      logBuffered := false
    )
  )

}
