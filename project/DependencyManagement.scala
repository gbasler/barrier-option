import sbt._

object DependencyManagement {

  /** Excluded and version managed transitive artifacts */
  def ivyExclusionsAndOverrides =
    <dependencies>
      <exclude org="org.junit" rev="*"/>
      <exclude org="org.junit" rev="4.8.1"/>
      <exclude org="colt" rev="1.2.0"/>
      <exclude org="blas" module="blas" rev="*"/>
      <exclude org="javax.script" rev="*"/>
      <exclude org="org.scala-tools.testing" module="scalacheck_2.8.0" rev="1.7"/>
      <override org="junit" module="junit" rev="4.8.1"/>
      <exclude org="org.eclipse" rev="*"/>
    </dependencies>

  /** Utilities for File IO */
  def CommonsIo = "commons-io" % "commons-io" % "2.4"

  /** General utilities for Java language */
  def CommonsLang = "commons-lang" % "commons-lang" % "2.6"

  val scalazVersion = "7.1.1"

  /** Scala library providing Actors and Promises (for concurrency), and functional programming tools */
  def ScalazCore = "org.scalaz" %% "scalaz-core" % scalazVersion

  def ScalazConcurrent = "org.scalaz" %% "scalaz-concurrent" % scalazVersion

  def SpringCore = "org.springframework" % "spring-core" % "2.5.6"

  /**
   * Specs, unit testing framework
   *
   * http://code.google.com/p/specs/
   */
  object Specs {
    def SpecsModule(name: String) = {
      "org.specs2" %% name % "3.8.9" % "test"
    }

    lazy val dependencies = Seq(
      SpecsModule("specs2-core"),
      SpecsModule("specs2-matcher-extra"),
      SpecsModule("specs2-scalacheck"),
      SpecsModule("specs2-junit")
    )
  }

  def JUnit = "junit" % "junit" % "4.12" % "test" intransitive()

  /**
   * Scalacheck, automated unit testing using randomized test cases.
   *
   * http://code.google.com/p/scalacheck/
   *
   * We use this through Specs.
   */
  def Scalacheck = "org.scalacheck" %% "scalacheck" % "1.12.2" % "test"

  /** Dependency of Specs */
  def MockitoAll = "org.mockito" % "mockito-all" % "1.10.19" % "test"

  //  def TreeHugger = "com.eed3si9n" % "treehugger_2.9.1" % "0.2.1"
  def TreeHugger = "com.eed3si9n" %% "treehugger" % "0.3.0"

  /** Date and Time represenation */
  def JodaTime = "joda-time" % "joda-time" % "2.7"

  def JodaConvert = "org.joda" % "joda-convert" % "1.7"

  def ScalaMeter = "com.storm-enroute" %% "scalameter" % "0.6" % "test" exclude("jfree", "jfreechart")

  /**
   * Collections specialized for primitive elements
   * http://fastutil.dsi.unimi.it/
   */
  def Fastutil = "it.unimi.dsi" % "fastutil" % "7.0.2"

  def Jna = "net.java.dev.jna" % "jna" % "4.1.0"

  def JnaPlatform = "net.java.dev.jna" % "jna-platform" % "4.1.0"

  /**
   * Scala Swing: GUI framework
   *
   * http://www.scala-lang.org/sid/8
   */
  def ScalaSwing = "org.scala-lang.modules" %% "scala-swing" % "2.0.0-M2"

  def Jzy3d = "org.jzy3d" % "jzy3d-api" % "0.9.1"

  /** Apache Commons Math, used for a interpolators, integration, utilities */
  def CommonsMath3 = "org.apache.commons" % "commons-math3" % "3.6.1"

  def Shapeless = "com.chuusai" %% "shapeless" % "2.2.5"

  def ChocoSolver = "org.choco-solver" % "choco-solver" % "4.0.0"

}
