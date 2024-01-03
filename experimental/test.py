import serial
import sys
import time

ser = serial.Serial(sys.argv[1], 115200)

# clear screen
ser.write([0x01])
time.sleep(0.1)

# hide cursor
ser.write([0x0E])

def draw_screen_raw(text):
    ser.write([0x02])
    ser.write(text)

def draw_screen(text):
    draw_screen_raw(text.encode())

idx = 0
while True:
    idx += 1
    r = b"\xDB" if (idx & 1) else b" "
    draw_screen_raw((b"hello world "+r) * 100)
