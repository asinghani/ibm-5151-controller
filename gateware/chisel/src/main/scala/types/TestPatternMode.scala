package types

import chisel3._

object TestPatternMode extends ChiselEnum {
    val NONE             = Value(0.U)
    val ALL_OFF          = Value(1.U)
    val ALL_ON           = Value(2.U)
    val VSTRIPS          = Value(3.U)
    val HSTRIPS          = Value(4.U)
    val SCREEN_BORDER    = Value(5.U)
    val CHECKERBOARD_1X1 = Value(6.U)
    val CHECKERBOARD_2X2 = Value(7.U)
}
