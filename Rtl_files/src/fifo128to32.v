module fifo128to32 (
    input  wire        clk,
    input  wire        rst,

    // Write interface (128-bit input)
    input  wire        write_en,
    input  wire [127:0] data_in,

    // Read interface (32-bit output)
    input  wire        read_en,
    output reg  [31:0] data_out,
    output reg         data_valid
);

    localparam FIFO_DEPTH = 16;
    localparam FIFO_PTR_WIDTH = 4;

    reg [127:0] fifo_mem [0:FIFO_DEPTH-1];
    reg [FIFO_PTR_WIDTH-1:0] write_ptr = 0;
    reg [FIFO_PTR_WIDTH-1:0] read_ptr  = 0;
    reg [1:0] chunk_index = 0;
    reg [127:0] current_word;
    reg has_data = 0;

    // Byte swap 32-bit word
    function [31:0] byte_swap32(input [31:0] in);
        byte_swap32 = {in[7:0], in[15:8], in[23:16], in[31:24]};
    endfunction

    // Write logic
    always @(posedge clk) begin
        if (rst) begin
            write_ptr <= 0;
        end else if (write_en) begin
            fifo_mem[write_ptr] <= data_in;
            write_ptr <= write_ptr + 1;
        end
    end

    // Read logic
    always @(posedge clk) begin
        if (rst) begin
            read_ptr     <= 0;
            chunk_index  <= 0;
            data_out     <= 0;
            data_valid   <= 0;
            has_data     <= 0;
        end else if (read_en) begin
            if (!has_data && (read_ptr != write_ptr)) begin
                // Load 128-bit word, delay actual output to next cycle
                current_word <= fifo_mem[read_ptr];
                read_ptr     <= read_ptr + 1;
                chunk_index  <= 0;
                has_data     <= 1;
                data_valid   <= 0;  // output will begin next cycle
            end else if (has_data) begin
                case (chunk_index)
                    0: begin
                        data_out    <= byte_swap32(current_word[31:0]);
                        data_valid  <= 1;
                        chunk_index <= 1;
                    end
                    1: begin
                        data_out    <= byte_swap32(current_word[63:32]);
                        data_valid  <= 1;
                        chunk_index <= 2;
                    end
                    2: begin
                        data_out    <= byte_swap32(current_word[95:64]);
                        data_valid  <= 1;
                        chunk_index <= 3;
                    end
                    3: begin
                        data_out    <= byte_swap32(current_word[127:96]);
                        data_valid  <= 1;
                        chunk_index <= 0;
                        has_data    <= 0;
                    end
                    default: begin
                        data_valid <= 0;
                    end
                endcase
            end else begin
                data_valid <= 0;
            end
        end else begin
            data_valid <= 0;
        end
    end

endmodule
