package barrierOption

final case class GreekEngine(relativeBump: Double = 0.01)
                            (f: Double => Double) {
  def delta(S: Double): Double = {
    val absoluteBump = S * relativeBump
    val a = f(S + absoluteBump)
    val b = f(S)
    (a - b) / absoluteBump
  }
}
