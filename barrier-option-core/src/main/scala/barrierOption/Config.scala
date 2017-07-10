package barrierOption

import java.time.LocalDate
import java.time.format.DateTimeFormatter

final case class Config(kvs: Map[String, String]) {
  def apply(key: String) = kvs(key)

  def update(key: String, value: String) = copy(kvs + (key.toLowerCase -> value))
}

object Config {

  def fromContent(content: Seq[String]): Config = {
    val kvs: Map[String, String] = {
      content.map(_.trim).filterNot(_.isEmpty).map {
        line =>
          line.split(" ") match {
            case Array(key, value) => key.toLowerCase -> value
            case _                 => sys.error(s"Could not parse $line.")
          }
      }.toMap
    }
    Config(kvs)
  }

  def fromResource(fileName: String): Config = {
    val content = scala.io.Source.fromResource(fileName).getLines().toSeq
    fromContent(content)
  }

  implicit class ConfigOps(key: String) /*extends AnyVal*/ {
    val dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    def value(implicit ctx: Config) = ctx(key.toLowerCase)

    def asDouble(implicit ctx: Config): Double = value.toDouble

    def asDate(implicit ctx: Config): LocalDate =
      LocalDate.parse(value, dateFormat)

    def asDateOpt(implicit ctx: Config): Option[LocalDate] = {
      ctx.kvs.get(key.toLowerCase).map(LocalDate.parse(_, dateFormat))
    }

    def asListOfDates(implicit ctx: Config): Seq[LocalDate] = {
      val kvs = ctx.kvs.toSeq.collect {
        case (k, v) if k.startsWith(key.toLowerCase()) =>
          k -> v
      }
      kvs.unzip._2.map(LocalDate.parse(_, dateFormat)).sortWith {
        case (a, b) =>
          a.compareTo(b) < 0
      }
    }
  }

}
