import serial
import sys
import time
import pygame

pygame.init()
window = pygame.display.set_mode((100, 100))

ser = serial.Serial(sys.argv[1], 115200)

CHR_CLR = b" "
CHR_BOX = b"\xDB"
CHR_O   = b"O"

# clear screen
ser.write([0x01])
time.sleep(0.1)

# hide cursor
ser.write([0x0E])

INIT_COORDS = (40, 12)
INIT_VEL = (1, -1)

score = 0
bx, by = INIT_COORDS
px = 20

run = False
vx, vy = INIT_VEL

WIDTH = 10
MOVSPEED = 2
BALLSPEED = 1

lastscreen = [False for _ in range(25)]

def update_screen():
    global lastscreen
    screen = [[CHR_CLR for _ in range(80)] for _ in range(25)]

    for i in range(px, px+WIDTH):
        screen[24][i] = CHR_BOX

    score_str = "{:02d}".format(score)
    score_str = score_str[-2:]

    screen[0][0] = score_str[0].encode()
    screen[0][1] = score_str[1].encode()

    x = min(79, max(int(bx), 0))
    y = min(24, max(int(by), 0))

    screen[y][x] = CHR_O


    ser.write([0x02])

    for i, line in enumerate(screen):
        if any(x != CHR_CLR for x in line):
            lastscreen[i] = True
            ser.write(b"".join(line))
        elif lastscreen[i]:
            ser.write(b"".join(line))
            lastscreen[i] = False
        else:
            ser.write(b"\n\r")
            lastscreen[i] = False

    time.sleep(0.05)

mov = 0

idx = 0
while True:
    idx += 1
    update_screen()

    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            break

        if event.type == pygame.KEYDOWN:
            if pygame.key.name(event.key) == "right":
                mov = 1
            if pygame.key.name(event.key) == "left":
                mov = -1
            if pygame.key.name(event.key) == "space":
                run = True

        if event.type == pygame.KEYUP:
            if pygame.key.name(event.key) == "right" and (mov == 1):
                mov = 0
            if pygame.key.name(event.key) == "left" and (mov == -1):
                mov = 0

    print(mov, run)
    px += MOVSPEED*mov
    px = max(px, 0)
    px = min(px, 80-WIDTH)
    pygame.display.flip()

    if run:
        bx += vx*BALLSPEED
        by += vy*BALLSPEED

        if (by <= 0) and vy < 0:
            vy *= -1

        if (bx <= 0) and vx < 0:
            vx *= -1

        if (bx >= 79) and vx > 0:
            vx *= -1

        if (by >= 23) and vy > 0:
            if bx >= px-1.5 and bx < px+WIDTH+1.5:
                vy *= -1
                score += 1
            else:
                bx, by = INIT_COORDS
                vx, vy = INIT_VEL
                run = False

