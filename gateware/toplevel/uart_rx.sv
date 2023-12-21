`default_nettype none

// UART Reciever (8/N/1)
module uart_rx
#(
    parameter CLK_FREQ = 250000,
    parameter BAUD = 9600
)
(
    output reg [7:0] o_data,
    output reg o_valid,

    input wire i_in,
    input wire i_rst,
    input wire i_clk
); 

localparam _CLKS_PER_BIT = CLK_FREQ / BAUD;
localparam [$clog2(_CLKS_PER_BIT * 2):0] CLKS_PER_BIT = _CLKS_PER_BIT[$clog2(_CLKS_PER_BIT * 2):0];
localparam [$clog2(_CLKS_PER_BIT * 2):0] CLKS_PER_HALF_BIT = CLKS_PER_BIT / 2;

reg[$clog2(_CLKS_PER_BIT * 2):0] counter;
reg [3:0] state = 0; // 0 = idle, 1 = start bit, 2-9 = data bits, 10 = end

always_ff @(posedge i_clk) begin
    o_valid <= 0;
    counter <= 10; // Set counter to default value when idle

    if (i_rst) begin
        state <= 0;
    end
    else if (state == 0) begin
        // Start bit
        if(i_in == 0) begin
            state <= 1;
            counter <= CLKS_PER_HALF_BIT;
        end

        // Else stay in idle
    end
    else if (counter == 0) begin
        // End bit
        if(state == 10) begin
            if (i_in == 1) begin
                o_valid <= 1;
                `ifdef DEBUG
                    $display("RECEIVED 0x%H (%B)", o_data, o_data);
                `endif
            end
            else begin
                `ifdef DEBUG
                    $display("INVALID END BIT");
                `endif
            end

            state <= 0;
        end

        else if (state == 1) begin
            if (i_in == 0) begin
                state <= 2;
                counter <= CLKS_PER_BIT;
            end
            else begin
                state <= 0;
            end
        end

        // Data bits
        else begin
            state <= state + 1;
            o_data[state - 2] <= i_in;

            counter <= CLKS_PER_BIT;
        end
    end
    else begin
        counter <= counter - 1;
    end
end

endmodule

