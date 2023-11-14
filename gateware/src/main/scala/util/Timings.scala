package util

object Timings {

    // Horizontal axis timings
    val LBLANK_PX           = 17
    val WIDTH_PX            = 720
    val RBLANK_PX           = 12
    val HSYNC_PX            = 135

    val WIDTH_CTR           = LBLANK_PX + WIDTH_PX + RBLANK_PX + HSYNC_PX

    // Vertical axis timings
    val HEIGHT_PX           = 350
    val VSYNC_PX            = 16
    val VBLANK_PX           = 3

    val HEIGHT_CTR          = HEIGHT_PX + VSYNC_PX + VBLANK_PX

    val FONT_WIDTH          = 9
    val FONT_HEIGHT         = 14

    val WIDTH_CHARS         = WIDTH_PX / FONT_WIDTH
    val HEIGHT_CHARS        = HEIGHT_PX / FONT_HEIGHT

    val CURSOR_BLINK_FRAMES = 14
    val BELL_CTR_CLKS       = 1300000
}
