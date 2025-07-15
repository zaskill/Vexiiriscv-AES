module led_flashing (
    input  wire       clk,    // System clock
    input  wire       rst,    // Active-high reset
    output reg [6:0]  led     // 7 LEDs for chenillard effect
);

    reg [23:0] counter;       // Slow counter
    reg [2:0]  position;      // Position of the active LED (0 to 6)

    always @(posedge clk or posedge rst) begin
       if (rst) begin
        counter  <= 24'd0;
        position <= 3'd0;
        led      <= 7'b0000001;
        
       end else begin
        led      <= 7'b0000001;
        
       end
    end

endmodule