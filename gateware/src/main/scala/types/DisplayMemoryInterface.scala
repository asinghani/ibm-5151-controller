package types

import util._
import util.MathUtils._
import chisel3._

class DisplayMemoryInterface extends Bundle {
    val xchar   = Input(UInt(clog2(Timings.WIDTH_CHARS).W))
    val ychar   = Input(UInt(clog2(Timings.HEIGHT_CHARS).W))

    // Allows for bank-switching during blanking period
    val vsync   = Input(Bool())

    val xcursor = Output(UInt(clog2(Timings.WIDTH_CHARS).W))
    val ycursor = Output(UInt(clog2(Timings.HEIGHT_CHARS).W))

    val char    = Output(new DisplayCharacter())
}
