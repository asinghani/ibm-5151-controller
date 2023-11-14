package main

import display._
import memory._
import util._
import types._
import chisel3._
import chisel3.util._

class TopLevel extends Module {
    val io = IO(new Bundle {
        val serial_in = Input(ValidIO(UInt(8.W)))
        val tp_mode   = Input(TestPatternMode())
        val mda       = Output(new MDAInterface())
        val bell      = Output(Bool())
    })

    val display_controller = Module(new DisplayController)

    //val memory = Module(new TestPatternMemory)
    val memory = Module(new BasicSerialMemory)
    memory.io.serial_in := io.serial_in
    io.bell := memory.io.bell

    display_controller.io.tp_mode := io.tp_mode
    io.mda := display_controller.io.mda

    display_controller.io.mem_iface <> memory.io.display_iface
}
