package barrierOption

import java.time.LocalDate
import java.time.temporal.ChronoUnit.{DAYS, YEARS}

import scala.math._

object BarrierOptionMain extends App {
  val config = Config.fromResource("barrier.txt")
  val result = BarrierOption(config)
  println(s"price: $result")
}

object BarrierOption {

  def apply(config: Config): Double = {
    import Config.ConfigOps
    implicit val ctx = config

    val σ = "volatility" asDouble
    val expiry = "expiry" asDate
    val K = "strike" asDouble
    val barrier = "barrier" asDouble
    val barrierDate1 = "ObservationDate1" asDate
    val barrierDate2 = "ObservationDate2" asDate
    val r = "rate" asDouble
    val S_0 = "S0" asDouble
    val calcDate = "CalculationDate" asDate

    val stepsPerDay = 1

    def indexOnTimeGrid(date: LocalDate) = {
      val days = DAYS.between(calcDate, date).toInt
      days * stepsPerDay
    }

    val N: Int = stepsPerDay * indexOnTimeGrid(expiry)

    val barrierDate1Index = indexOnTimeGrid(barrierDate1)

    val T = YEARS.between(calcDate, expiry).toDouble
    val dt = T / N

    // up move of stock
    val u = exp(σ * sqrt(dt))
    val d = 1.0 / u // recombining tree

    // calculate risk-neutral probabilities for each step
    val qu = (exp(r * dt) - d) / (u - d)
    val qd = (u - exp(r * dt)) / (u - d)

    /**
      * (0,0): start state
      *
      * @param i time steps
      * @param k up steps
      * @return
      */
    def spotAt(i: Int, k: Int) = {
      // pow(d, i) * pow(u, 2*k) =(using d=1/u) pow(u, 2*k - i)
      S_0 * pow(u, 2 * k - i)
    }

    // N steps, so we need N+1 elements
    // generate tree initial values at time T.
    // p(0) is bottom element (all down moves), p(n) top element (all up moves).
    val p = Array.tabulate(N + 1) {
      n =>
        spotAt(N, n)
    }

    def payoff(S: Double) = max(0.0, K - S)

    // propagate terminal payoff back to calc date
    def propagateTerminalPayoff(t: Array[Double]): Unit = {
      for {
        j <- N - 1 to(0, -1)
        i <- 0 to j
      } {
        t(i) = qu * t(i + 1) + qd * t(i)
      }
    }

    // all paths that knock in at expiry (optional)
    val downAndInAtExpiry = {
      require(barrierDate2 == expiry, "Only barrier at expiry allowed")
      val downAndInAtExpiry = p.map(payoff).zipWithIndex.map {
        case (v, k) =>
          val s = spotAt(N, k)
          if (s < barrier) {
            v
          } else {
            0.0
          }
      }

      propagateTerminalPayoff(downAndInAtExpiry)
      downAndInAtExpiry(0)
    }

    val downAndInAtFirstObs = {
      // all paths that knock in at first obs date
      // all paths that knock in at expiry
      val downAndInAtFirstObs = {
        p.map(payoff).zipWithIndex.map {
          case (v, k) =>
            val s = spotAt(N, k)
            if (s > barrier) {
              v
            } else {
              0.0
            }
        }
      }

      for {
        j <- N - 1 to(0, -1)
        i <- 0 to j
      } {
        val propagated = qu * downAndInAtFirstObs(i + 1) + qd * downAndInAtFirstObs(i)
        val value = if (j == barrierDate1Index) {
          val s = spotAt(j, i)
          if (s < barrier) {
            propagated
          } else {
            0.0
          }
        } else {
          propagated
        }
        downAndInAtFirstObs(i) = value
      }

      downAndInAtFirstObs(0)
    }

    val undiscountedPrice = downAndInAtExpiry + downAndInAtFirstObs

    // discount
    exp(-r * T) * undiscountedPrice
  }

}