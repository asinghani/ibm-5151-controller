package types

import chisel3._

class MDAInterface extends Bundle {
    val hsync  = Bool()
    val vsync  = Bool()
    val pixel  = Bool()
    val intens = Bool()
}
