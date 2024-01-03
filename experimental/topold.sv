`default_nettype none

`include "font.sv"

module ice40_top (
    input wire CLOCK_12,
    input wire btn_rst,

    output logic hsync,
    output logic vsync,
    output logic data,
    output logic intensity
);
    logic pix_clk;

        /**
        * Given input frequency:        12.000 MHz
        * Requested output frequency:   16.384 MHz
        * Achieved output frequency:    16.312 MHz
        */
        SB_PLL40_PAD #(
            .FEEDBACK_PATH("SIMPLE"),
            .DIVR(4'b0000),		// DIVR =  0
            .DIVF(7'b1010110),	// DIVF = 86
            .DIVQ(3'b110),		// DIVQ =  6
            .FILTER_RANGE(3'b001)	// FILTER_RANGE = 1
        ) uut (
            .LOCK(),
            .RESETB(1'b1),
            .BYPASS(1'b0),
            .PACKAGEPIN(CLOCK_12),
            .PLLOUTCORE(pix_clk)
        );
        //assign pix_clk = CLOCK_12;


        mda_core mda (
            .clock(pix_clk),
            .MDA_HSYNC(hsync),
            .MDA_VSYNC(vsync),
            .MDA_DATA(data),
            .MDA_INTENSITY(intensity)
        );

endmodule

// https://github.com/MicroCoreLabs/Projects/blob/master/MDA_Video/MDA_Video_core_v1_0.v
module mda_core (
    input logic clock,

    output logic MDA_HSYNC,
    output logic MDA_VSYNC,
    output logic MDA_DATA,
    output logic MDA_INTENSITY
);

    // Internal Signals
    //------------------------------------------------------------------------
    reg mda_hsync_int           = 'h0;
    reg mda_vsync_int           = 'h0;
    reg mda_intensity_int       = 'h0;
    reg mda_shifter_load        = 'h0;

    reg [30:0] ctr;
    reg inv;

    reg [8:0]    yctr       = 'h0;
    reg [15:0]   xctr    = 'h0;

    assign MDA_HSYNC     = mda_hsync_int;
    assign MDA_VSYNC     = mda_vsync_int;

    wire valid = ((xctr < 720) && (yctr < 350));

    wire m_bit;
    
    reg [6:0] xchar;
    reg [6:0] ychar;

    reg [6:0] xchar_ctr;
    reg [6:0] ychar_ctr;

    logic [7:0] char;

    always_comb begin
        char = 0;
        if ((xchar < 64) && (ychar < 4)) begin
            char = {ychar[1:0], xchar[5:0]};
        end
    end

    font_cp437 thefont (
        .xidx(xchar_ctr),
        .yidx(ychar_ctr),
        .char(char),
        .pixel(m_bit)
    );

    //assign MDA_DATA      = (xctr == (inv ? 1 : 0)) && valid;
    //assign MDA_INTENSITY = (yctr > 'd175) && valid;
    
    assign MDA_DATA = m_bit && valid;
    assign MDA_INTENSITY = valid;

    always @(posedge clock) begin
        ctr <= ctr + 1;
        if ((ctr >= 3*16000000) && (mda_vsync_int == 0)) begin
            ctr <= 0;
            inv <= !inv;
        end

        xchar_ctr <= xchar_ctr + 1;
        if (xchar_ctr == 8) begin
            xchar_ctr <= 0;
            xchar <= xchar + 1;
        end

        xctr <= xctr + 1'b1;
        case (xctr)

           // 'd000 : mda_shifter_load <= 1'b1;                                 // Load DPRAM contents into shift register
           // 'd001 : mda_shifter_load <= 1'b0;                                 // Debounce shift register loader

            'd731 : mda_hsync_int <= 1'b1;                                    // 720 clocks for active video and another 10 clocks, then assert HSYNC
            'd866 : mda_hsync_int <= 1'b0;                                    // 135 clocks for HSYNC active

            'd883 : begin
                yctr <= yctr + 1'b1;                      // Advance to the next DPRAM row

                ychar_ctr <= ychar_ctr + 1;
                if (ychar_ctr == 13) begin
                    ychar_ctr <= 0;
                    ychar <= ychar + 1;
                end

                xctr <= 'd0;                                 // Return to the beginning of this state machine  
                xchar <= 0;
                xchar_ctr <= 0;
                if (yctr=='d348)  mda_vsync_int <= 1'b0;        // 17 clocks after HSYNC de-asserted, then check to start VSYNC at end of 350 lines
                if (yctr=='d364)  mda_vsync_int <= 1'b1;        // De-assert VSYNC after 16 lines
                if (yctr=='d368)  begin
                    yctr <= 'h0;          // After four lines of nothing, return to the beginning of the video DPRAM
                    ychar <= 0;
                    ychar_ctr <= 0;
                end
            end

            default: ;
        endcase  
    end

endmodule
