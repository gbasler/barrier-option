package barrierOption

import java.time.temporal.ChronoUnit.YEARS

import org.apache.commons.math3.distribution.{MultivariateNormalDistribution, NormalDistribution}
import org.specs2.mutable.Specification

import scala.math.{exp, log, sqrt}

// Attempt to use the approach to price barrier options with one discrete date
// "An Introduction to Exotic Option Pricing, Peter Buchen"
class BarrierOptionTest extends Specification {

  def createBivariateNormalDist(μ: Double = 0.0,
                                σ: Double = 1.0) = {
    new MultivariateNormalDistribution(Array(μ, μ),
      Array(Array(0, σ), Array(σ, 0)))
  }

  def N(d1: Double, d2: Double, ρ: Double) = {
    // N(0, 1; ρ)
    createBivariateNormalDist(σ = ρ) // TODO: integrate for cumulative density
  }

}
