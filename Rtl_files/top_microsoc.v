module top_microsoc (
  input  wire CLK100MHZ,
  input  wire soc_reset_button,  
  input wire led_reset,

  input  wire uart_rxd,
  output wire uart_txd,


  output wire [6:0] leds


);

  // Internal AES wires (not mapped to pins)
  wire [127:0] aes_output;
  wire         aes_data_valid;
  wire [31:0]  icap_data;
  wire         icap_valid;
  wire [7:0] LEDS;
   

  
  
  MicroSoc dut (
    .socCtrl_systemClk               (CLK100MHZ),
    .socCtrl_asyncReset              (soc_reset_button),

    .socCtrl_debugModule_tck        (1'b0),
    .socCtrl_debugModule_tap_jtag_tms(1'b0),
    .socCtrl_debugModule_tap_jtag_tdi(1'b0),
    .socCtrl_debugModule_tap_jtag_tdo(),
    .socCtrl_debugModule_tap_jtag_tck(1'b0),
    .socCtrl_debugModule_instruction_instruction_tdi(1'b0),
    .socCtrl_debugModule_instruction_instruction_enable(1'b0),
    .socCtrl_debugModule_instruction_instruction_capture(1'b0),
    .socCtrl_debugModule_instruction_instruction_shift(1'b0),
    .socCtrl_debugModule_instruction_instruction_update(1'b0),
    .socCtrl_debugModule_instruction_instruction_reset(1'b0),
    .socCtrl_debugModule_instruction_instruction_tdo(),

    .system_peripheral_uart_logic_uart_txd(uart_txd),
    .system_peripheral_uart_logic_uart_rxd(uart_rxd),

    .system_peripheral_aes_logic_aes_output(aes_output),
    .system_peripheral_aes_logic_data_valid(aes_data_valid),



    .system_peripheral_demo_logic_leds    (LEDS)

  );
  
fifo128to32  fifo128to32 (
    .clk (CLK100MHZ),
    .rst (soc_reset_button),

    
    .write_en(aes_data_valid),
    .data_in(aes_output),

    
    .read_en(1'b1),
    .data_out(icap_data),
    .data_valid(icap_valid)
    
);

// ICAPE2: Internal Configuration Access Port
//         7 Series
// Xilinx HDL Language Template, version 2025.1

ICAPE2 #(
   .DEVICE_ID(32'h3651093),     // Specifies the pre-programmed Device ID value to be used for simulation
                               // purposes.
   .ICAP_WIDTH("X32"),         // Specifies the input and output data width.
   .SIM_CFG_FILE_NAME("NONE")  // Specifies the Raw Bitstream (RBT) file to be parsed by the simulation
                               // model.
)
ICAPE2_inst (
   .O(),         // 32-bit output: Configuration data output bus
   .CLK(CLK100MHZ),     // 1-bit input: Clock Input
   .CSIB(~icap_valid),   // 1-bit input: Active-Low ICAP Enable
   .I(icap_data),         // 32-bit input: Configuration data input bus
   .RDWRB(1'b0)  // 1-bit input: Read/Write Select input
);

led_flashing led_flashing(
    .clk(CLK100MHZ),
    .rst(led_reset),
    .led(leds)
    
);


// End of ICAPE2_inst instantiation

/*
  ila_0 ila_0(
  .clk(CLK100MHZ),
  .probe0(uart_rxd),
  .probe1(uart_txd)

  );
  */
  
  ila_2 ila_2(
  .clk(CLK100MHZ),
  .probe0(aes_test_done),
  .probe1(aes_output),
  .probe2(icap_data),
  .probe3(icap_valid)
  );

  
  
endmodule
