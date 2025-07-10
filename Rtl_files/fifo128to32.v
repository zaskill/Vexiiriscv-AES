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

    // Internal FIFO depth for example: store up to 16 * 128-bit entries
    localparam FIFO_DEPTH = 16;
    localparam FIFO_PTR_WIDTH = 4; // log2(FIFO_DEPTH)

    // 128-bit wide FIFO
    reg [127:0] fifo_mem [0:FIFO_DEPTH-1];
    reg [FIFO_PTR_WIDTH-1:0] write_ptr = 0;
    reg [FIFO_PTR_WIDTH-1:0] read_ptr  = 0;
    reg [1:0] chunk_index = 0; // Index into 32-bit chunks: 0 to 3
    reg [127:0] current_word;
    reg has_data = 0; // Indicates current_word is valid

    // Write logic (128-bit input)
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
            read_ptr    <= 0;
            chunk_index <= 0;
            data_out    <= 0;
            data_valid  <= 0;
            has_data    <= 0;
        end else if (read_en) begin
            if (!has_data && (read_ptr != write_ptr)) begin
                // Load next 128-bit word and output first chunk
                current_word <= fifo_mem[read_ptr];
                read_ptr     <= read_ptr + 1;
                chunk_index  <= 1;
                data_out     <= fifo_mem[read_ptr][31:0];
                data_valid   <= 1;
                has_data     <= 1;
            end else if (has_data) begin
                // Output next 32-bit chunk
                case (chunk_index)
                    1: begin
                        data_out   <= current_word[63:32];
                        data_valid <= 1;
                        chunk_index <= 2;
                    end
                    2: begin
                        data_out   <= current_word[95:64];
                        data_valid <= 1;
                        chunk_index <= 3;
                    end
                    3: begin
                        data_out   <= current_word[127:96];
                        data_valid <= 1;
                        has_data   <= 0;
                        chunk_index <= 0;
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
