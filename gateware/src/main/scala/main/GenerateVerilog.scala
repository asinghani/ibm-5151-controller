package main

import java.io._

import circt.stage.ChiselStage
import display._

object GenerateVerilog extends App {
    val build_dir = new File("build/")
    if (!build_dir.exists) build_dir.mkdirs

    val outfile = new File("build/top.sv")

    val verilog = ChiselStage.emitSystemVerilog(new TopLevel)

    val writer = new BufferedWriter(new FileWriter(outfile))
    writer.write(verilog)
    writer.close()
}
