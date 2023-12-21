package display

import util._
import types._
import chisel3._
import chisel3.util._

class DisplayController extends Module {
    val io = IO(new Bundle {
        val tp_mode   = Input(TestPatternMode())
        val mem_iface = Flipped(new DisplayMemoryInterface())
        val mda       = Output(new MDAInterface())
    })

    val mda       = Wire(new MDAInterface())

    val pix_valid = Wire(Bool())
    val hblank    = Wire(Bool())
    val vblank    = Wire(Bool())

    val cursor    = Wire(Bool())
    val pixel     = Wire(Bool())
    val intens    = Wire(Bool())

    // Register the outputs to make timing way cleaner
    io.mda := RegNext(mda)

    // Generate counters
    val (xctr, xwrap) = Counter(0 until Timings.WIDTH_CTR)
    val (yctr, ywrap) = Counter(0 until Timings.HEIGHT_CTR, xwrap)

    // Generate cursor blink timings
    val (_, cursor_blink_toggle) = Counter(0 until Timings.CURSOR_BLINK_FRAMES, ywrap)
    val cursor_show = Reg(Bool())
    when (cursor_blink_toggle) { cursor_show := !cursor_show }

    // Generate sync pulses
    mda.hsync := (xctr >= (Timings.LBLANK_PX + Timings.WIDTH_PX + Timings.RBLANK_PX).U)

    mda.vsync := !((yctr >= Timings.HEIGHT_PX.U) &&
                   (yctr < (Timings.HEIGHT_PX + Timings.VSYNC_PX).U))

    pix_valid := (xctr >= Timings.LBLANK_PX.U) &&
                 (xctr < (Timings.LBLANK_PX + Timings.WIDTH_PX).U) &&
                 (yctr < Timings.HEIGHT_PX.U)

    // MDA vsync is inverted
    io.mem_iface.vsync := !mda.vsync

    hblank := (xctr < Timings.LBLANK_PX.U)
    vblank := (yctr >= (Timings.HEIGHT_PX + Timings.VSYNC_PX).U)

    // Generate char counters
    val (xchar_idx, xchar_wrap) = Counter(0 until Timings.FONT_WIDTH, true.B, hblank)
    val (ychar_idx, ychar_wrap) = Counter(0 until Timings.FONT_HEIGHT, xwrap, vblank)
    val (xchar, _)              = Counter(0 until Timings.WIDTH_CHARS, xchar_wrap, hblank)
    val (ychar, _)              = Counter(0 until Timings.HEIGHT_CHARS, ychar_wrap, vblank)

    cursor := io.mem_iface.xcursor === xchar &&
              io.mem_iface.ycursor === ychar &&
              cursor_show && io.mem_iface.cursor_en

    // Index into the display memory
    io.mem_iface.xchar := Mux(xchar_wrap, xchar + 1.U, xchar)
    io.mem_iface.ychar := ychar

    val font_rom = Module(new FontROM)
    font_rom.io.char  := io.mem_iface.char
    font_rom.io.x_idx := xchar_idx
    font_rom.io.y_idx := ychar_idx

    // Generate the output pixel
    pixel  := false.B
    intens := false.B

    switch (io.tp_mode) {
        is (TestPatternMode.NONE) {
            pixel  := font_rom.io.pixel
            intens := font_rom.io.intens
        }

        is (TestPatternMode.ALL_OFF) {
            pixel  := false.B
            cursor := false.B
        }

        is (TestPatternMode.ALL_ON) {
            pixel  := true.B
            cursor := false.B
        }

        is (TestPatternMode.VSTRIPS) {
            pixel  := xctr(0)
            cursor := false.B
        }

        is (TestPatternMode.HSTRIPS) {
            pixel  := yctr(0)
            cursor := false.B
        }

        is (TestPatternMode.SCREEN_BORDER) {
            pixel  := yctr(0)
            cursor := false.B
        }

        is (TestPatternMode.CHECKERBOARD_1X1) {
            pixel  := xctr(0) ^ yctr(0)
            cursor := false.B
        }

        is (TestPatternMode.CHECKERBOARD_2X2) {
            pixel  := xctr(1) ^ xctr(0) ^ yctr(1)
            cursor := false.B
        }
    }

    // TODO: implement alternate cursor modes
    mda.pixel  := (pixel ^ cursor) && pix_valid
    mda.intens := intens && pix_valid
}
