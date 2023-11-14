package types

import chisel3._

class DisplayCharacter extends Bundle {
    val char_code = UInt(8.W)
    
    val bright    = Bool()
    val underline = Bool()
    val blink     = Bool()
    val reverse   = Bool()
}
