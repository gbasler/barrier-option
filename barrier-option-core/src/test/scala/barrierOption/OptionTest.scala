package barrierOption

import java.time.temporal.ChronoUnit.YEARS

import org.apache.commons.math3.distribution.NormalDistribution
import org.specs2.mutable.Specification

import scala.math.{exp, log, sqrt}

class OptionTest extends Specification {

  def createNormalDist(μ: Double = 0.0, σ: Double = 1.0) = new NormalDistribution(μ, σ)

  val nd = createNormalDist()

  val cdf: Double => Double = nd.cumulativeProbability _

  def d1(σ: Double, τ: Double, S_t: Double, K: Double, r: Double) = {
    (log(S_t / K) + (r + 0.5 * σ * σ) * τ) / (σ * sqrt(τ))
  }

  def d2(σ: Double, τ: Double, S_t: Double, K: Double, r: Double) = {
    (log(S_t / K) + (r - 0.5 * σ * σ) * τ) / (σ * sqrt(τ))
  }

  def bsPutPrice(σ: Double, τ: Double, S_t: Double, K: Double, r: Double) = {
    cdf(-d2(σ, τ, S_t, K, r)) * K * exp(-r * τ) - cdf(-d1(σ, τ, S_t, K, r)) * S_t
  }

  def bsPutDelta(σ: Double, τ: Double, S_t: Double, K: Double, r: Double) = {
    cdf(d1(σ, τ, S_t, K, r)) - 1.0
  }

  def calculate(content: String) = {
    implicit val cfg = Config.fromContent(content.split('\n'))
    import Config.ConfigOps

    val σ = "volatility" asDouble
    val expiry = "expiry" asDate
    val K = "strike" asDouble
    val r = "rate" asDouble
    val S_0 = "S0" asDouble
    val calcDate = "CalculationDate" asDate
    val τ = YEARS.between(calcDate, expiry).toDouble
    val price = BarrierOption(cfg)
    val bsPrice = bsPutPrice(σ, τ, S_0, K, r)
    price -> bsPrice
  }

  def calculateDelta(content: String) = {
    implicit val cfg = Config.fromContent(content.split('\n'))
    import Config.ConfigOps

    val delta = GreekEngine() {
      S =>
        val updatedCfg = cfg.update("S0", S.toString)
        BarrierOption(updatedCfg)
    }.delta("S0" asDouble)

    val σ = "volatility" asDouble
    val expiry = "expiry" asDate
    val K = "strike" asDouble
    val r = "rate" asDouble
    val S_0 = "S0" asDouble
    val calcDate = "CalculationDate" asDate
    val τ = YEARS.between(calcDate, expiry).toDouble
    val bsDelta = bsPutDelta(σ, τ, S_0, K, r)
    delta -> bsDelta
  }

  "european put" should {
    "zero rate" in {
      val content =
        """|CalculationDate 10.02.2017
           |Volatility 0.2
           |Dividends 0
           |S0 100
           |Strike 100
           |Barrier 100000
           |ObservationDate1 10.08.2017
           |ObservationDate2 10.02.2018
           |Rate 0.0
           |Expiry 10.02.2018
        """.stripMargin

      val (price, bsPrice) = calculate(content)
      price must beCloseTo(bsPrice, 1e-2)
    }

    "zero rate: delta" in {
      val content =
        """|CalculationDate 10.02.2017
           |Volatility 0.2
           |Dividends 0
           |S0 100
           |Strike 100
           |Barrier 100000
           |ObservationDate1 10.08.2017
           |ObservationDate2 10.02.2018
           |Rate 0.0
           |Expiry 10.02.2018
        """.stripMargin

      val (delta, bsDelta) = calculateDelta(content)
      delta must beCloseTo(bsDelta, 1e-4)
    }

    "rate of 50%" in {
      val content =
        """|CalculationDate 10.02.2017
           |Volatility 0.2
           |Dividends 0
           |S0 100
           |Strike 100
           |Barrier 100000
           |ObservationDate1 10.08.2017
           |ObservationDate2 10.02.2018
           |Rate 0.5
           |Expiry 10.02.2018
        """.stripMargin

      val (price, bsPrice) = calculate(content)
      price must beCloseTo(bsPrice, 1e-2)
    }
  }
}
