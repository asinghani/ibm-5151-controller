package memory

import util._
import types._
import chisel3._
import chisel3.util._

class TestPatternMemory extends Module {
    val io = IO(new Bundle {
        val display_iface = new DisplayMemoryInterface()
    })

    io.display_iface.char.char_code := RegNext(Mux(
        (io.display_iface.xchar < 64.U) && (io.display_iface.ychar < 4.U),
        Cat(io.display_iface.ychar(1, 0), io.display_iface.xchar(5, 0)),
        ' '.U
    ))

    io.display_iface.char.bright    := false.B
    io.display_iface.char.underline := false.B
    io.display_iface.char.blink     := false.B
    io.display_iface.char.reverse   := false.B
    
    io.display_iface.xcursor   := 0.U
    io.display_iface.ycursor   := 4.U
    io.display_iface.cursor_en := true.B
}
