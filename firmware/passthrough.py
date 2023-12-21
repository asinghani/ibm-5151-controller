import digitalio
import usb_cdc
import busio
import board
import digitalio
import time

FPGA_TX = board.GP24 # RP2040 GP24 -> IF_TP_2 -> FPGA 46 (IOB_0a)
FPGA_RX = board.GP25 # FPGA 45 (IOB_5b) -> IF_TP_3 -> RP2040 GP25

FPGA_RST = board.GP20 # RP2040 GP20 -> FPGA 37 (IOT_45a)

reset = digitalio.DigitalInOut(FPGA_RST)
reset.direction = digitalio.Direction.OUTPUT

serial = usb_cdc.data
uart = busio.UART(FPGA_TX, FPGA_RX, baudrate=115200)

# Reset the FPGA on startup
reset.value = True
time.sleep(1)
reset.value = False
time.sleep(1)

while True:
    while serial.in_waiting:
        uart.write(serial.read(serial.in_waiting))

    while uart.in_waiting:
        serial.write(uart.read(uart.in_waiting))
