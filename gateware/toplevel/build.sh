set -Eeuo pipefail
set -o xtrace

# Make a build folder
rm -r build || true
mkdir build

make -C ../chisel -B build/top.sv
sv2v ../chisel/build/top.sv > build/tmp.v

# Synthesis (can add more SV files, space-separated, if desired)
yosys -p 'read_verilog -sv top.sv build/tmp.v; synth_ice40 -json build/synthesis.json -top ice40_top'

# Place-and-route
nextpnr-ice40 --up5k --json build/synthesis.json --asc build/pnr.asc --package sg48 --pcf constraints.pcf --freq 25

# Compress the bitstream
icepack build/pnr.asc build/bitstream.bit

# Load the bitstream
#iceprog build/bitstream.bit
