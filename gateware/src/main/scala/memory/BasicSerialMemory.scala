package memory

import util._
import util.MathUtils._
import types._
import chisel3._
import chisel3.util._

class BasicSerialMemory extends Module {
    val io = IO(new Bundle {
        val serial_in     = Input(ValidIO(UInt(8.W)))
        val bell          = Output(Bool())
        val display_iface = new DisplayMemoryInterface()
    })

    // TODO: this is hardcoded for 80x25 display
    assert(Timings.WIDTH_CHARS == 80)
    assert(Timings.HEIGHT_CHARS == 25)

    val bell_ctr = RegInit(0.U(clog2(Timings.BELL_CTR_CLKS).W))

    io.bell := false.B
    when (bell_ctr =/= 0.U) {
        bell_ctr := bell_ctr - 1.U
        io.bell := true.B
    }

    val mem = SyncReadMem(4096, UInt(8.W))

    val xidx = RegInit(0.U(7.W))
    val yidx = RegInit(0.U(5.W))

    when (io.serial_in.valid) {
        when (io.serial_in.bits === '\n'.U) { // line feed
            yidx := yidx + 1.U
            when (yidx === 24.U) { yidx := 0.U }
        }
        .elsewhen (io.serial_in.bits === '\r'.U) { // carriage return
            xidx := 0.U
        }
        .elsewhen (io.serial_in.bits === 0x02.U) { // STX / reset position
            xidx := 0.U
            yidx := 0.U
        }
        .elsewhen (io.serial_in.bits === 0x07.U) { // bell
            bell_ctr := Timings.BELL_CTR_CLKS.U
        }
        .elsewhen (io.serial_in.bits === 0x08.U) { // backspace
            xidx := Mux(xidx === 0.U, 0.U, xidx - 1.U)
        }
        .elsewhen (io.serial_in.bits === 0x1E.U) { // up arrow
            yidx := Mux(yidx === 0.U, 0.U, yidx - 1.U)
        }
        .elsewhen (io.serial_in.bits === 0x1F.U) { // down arrow
            yidx := Mux(yidx === 24.U, 24.U, yidx + 1.U)
        }
        .elsewhen (io.serial_in.bits === 0x1C.U) { // left arrow
            xidx := Mux(xidx === 0.U, 0.U, xidx - 1.U)
        }
        .elsewhen (io.serial_in.bits === 0x1D.U) { // right arrow
            xidx := Mux(xidx === 79.U, 79.U, xidx + 1.U)
        }
        .otherwise {
            mem.write(Cat(xidx, yidx), io.serial_in.bits)
            xidx := xidx + 1.U
            when (xidx === 79.U) {
                xidx := 0.U
                yidx := Mux(yidx === 24.U, 0.U, yidx + 1.U)
            }
        }
    }

    // Read side
    io.display_iface.char.char_code := mem.read(
        Cat(io.display_iface.xchar(6, 0), io.display_iface.ychar(4, 0))
    )
    io.display_iface.char.bright    := false.B
    io.display_iface.char.underline := false.B
    io.display_iface.char.blink     := false.B
    io.display_iface.char.reverse   := false.B

    io.display_iface.xcursor := xidx
    io.display_iface.ycursor := yidx
}
