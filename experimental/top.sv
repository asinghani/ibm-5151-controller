`default_nettype none

`include "tmp.v"
`include "uart_rx.sv"
`include "uart_tx.sv"

module ice40_top (
    input wire CLOCK_12,
    input wire btn_rst,

    input wire btn0,
    input wire btn1,
    input wire btn2,

    input logic urx,
    output logic utx,

    output logic hsync,
    output logic vsync,
    output logic data,
    output logic intensity,

    output logic led
);
    logic pix_clk;

    logic [7:0] uart_data;
    logic uart_valid;

    logic [3:0] rst_buf;
    logic rst;
    logic pll_lock;
    always_ff @(posedge pix_clk) rst_buf <= {rst_buf[2:0], btn_rst};
    assign rst = (~rst_buf[3]) || ~pll_lock;

    always_ff @(posedge pix_clk) begin
        if (uart_valid) led <= !led;
    end

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
            .LOCK(pll_lock),
            .RESETB(1'b1),
            .BYPASS(1'b0),
            .PACKAGEPIN(CLOCK_12),
            .PLLOUTCORE(pix_clk)
        );
        
        uart_rx #(
            .CLK_FREQ(16312000),
            .BAUD(115200)
        ) u_rx (
            .o_data(uart_data),
            .o_valid(uart_valid),
            .i_in(urx),
            .i_rst(rst),
            .i_clk(pix_clk)
        );

        uart_tx #(
            .CLK_FREQ(16312000),
            .BAUD(120000) // TODO
        ) u_tx (
            .o_ready(),
            .o_out(utx),

            .i_data(uart_data),
            .i_valid(uart_valid),

            .i_rst(rst),
            .i_clk(pix_clk)
        );
        
        TopLevel top (
            .clock(pix_clk),
            .reset(rst),
            .io_tp_mode({~btn2, ~btn1, ~btn0}),
            .io_mda_hsync(hsync),
            .io_mda_vsync(vsync),
            .io_mda_pixel(data),
            .io_mda_intens(intensity),
            .io_bell(),
            .io_serial_in_valid(uart_valid),
            .io_serial_in_bits(uart_data)
        );

endmodule

