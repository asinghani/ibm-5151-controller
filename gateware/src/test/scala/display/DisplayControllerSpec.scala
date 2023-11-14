package display

import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import chisel3.experimental.BundleLiterals._
import types._

class DisplayControllerSpec extends AnyFreeSpec with ChiselScalatestTester {
  "Display should generate appropriate pulses" in {
    test(new DisplayController).withAnnotations(Seq(VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(0)

      dut.io.tp_mode.poke(TestPatternMode.NONE)
      dut.clock.step(1200000)
    }
  }
}
