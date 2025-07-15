// Copyright lowRISC contributors (OpenTitan project).
// Licensed under the Apache License, Version 2.0, see LICENSE for details.
// SPDX-License-Identifier: Apache-2.0
//
// AES wrapper 

module aes_wrap
  import aes_pkg::*;
#(
  parameter bit         AES192Enable = 0,           // Can be 0 (disable), or 1 (enable).
  parameter bit         SecMasking   = 1,           // Can be 0 (no masking), or
  //0                                                  // 1 (first-order masking) of the cipher
                                                    // core. Masking requires the use of a
                                                    // masked S-Box, see SBoxImpl parameter.
                                                    // Note: currently, constant masks are
                                                    // used, this is of course not secure.
  parameter sbox_impl_e SecSBoxImpl  = SBoxImplDom  // See aes_pkg.sv
) (
  input  logic         clk_i,
  input  logic         rst_ni,

  input  logic [127:0] aes_input,
  input  logic [255:0] aes_key,
  output logic [127:0] aes_output,
  //----------------
  input  logic         aes_decrypt_i, //Added decrypt mode useful for aes_modes other than CTR
  input logic [127:0] aes_iv,  // Added counter initial value to the FSM for modes other than ECB
  input  logic         start, // added a start signal to trigger fsm after loading a new input through software
  //-----------------
  output logic         alert_recov_o,
  output logic         alert_fatal_o,

  output logic         data_valid
);

  localparam logic SIDELOAD = 1'b1;
  localparam aes_mode_e AES_MODE = AES_CTR;

  import aes_reg_pkg::*;
  import tlul_pkg::*;

  logic unused_idle;
  logic [31:0] unused_wdata;
  logic edn_req;
  keymgr_pkg::hw_key_req_t keymgr_key;
  tl_h2d_t h2d, h2d_intg; // req
  tl_d2h_t d2h; // rsp
  prim_alert_pkg::alert_rx_t [NumAlerts-1:0] alert_rx;
  prim_alert_pkg::alert_tx_t [NumAlerts-1:0] alert_tx;

  // Sideload interface - allows for quicker simulation.
  assign keymgr_key.valid = 1'b1;
  assign keymgr_key.key[0][255:0] = aes_key;
  assign keymgr_key.key[1][255:0] = '0;

  // Alert receivers
  assign alert_rx[0].ping_p = 1'b0;
  assign alert_rx[0].ping_n = 1'b1;
  assign alert_rx[0].ack_p  = 1'b0;
  assign alert_rx[0].ack_n  = 1'b1;
  assign alert_rx[1].ping_p = 1'b0;
  assign alert_rx[1].ping_n = 1'b1;
  assign alert_rx[1].ack_p  = 1'b0;
  assign alert_rx[1].ack_n  = 1'b1;

  // Alert transceivers
  assign alert_recov_o = alert_tx[0].alert_p | ~alert_tx[0].alert_n;
  assign alert_fatal_o = alert_tx[1].alert_p | ~alert_tx[1].alert_n;

  // Command integrity generation
  tlul_cmd_intg_gen tlul_cmd_intg_gen (
    .tl_i(h2d),
    .tl_o(h2d_intg)
  );


  // DUT
  aes #(
    .AES192Enable(AES192Enable),
    .SecMasking(SecMasking),
    .SecSBoxImpl(SecSBoxImpl)
  ) aes (
    .clk_i           (clk_i),
    .rst_ni          (rst_ni),
    .rst_shadowed_ni (rst_ni),
    .idle_o          (unused_idle),
    .lc_escalate_en_i(lc_ctrl_pkg::Off),
    .clk_edn_i       (clk_i),
    .rst_edn_ni      (rst_ni),
    .edn_o           (edn_req),
    .edn_i           ({edn_req, 1'b1, 32'h12345678}),
    .keymgr_key_i    (keymgr_key),
    .tl_i            (h2d_intg),
    .tl_o            (d2h),
    .alert_rx_i      (alert_rx),
    .alert_tx_o      (alert_tx)
  );

  // FSM
  localparam int StateWidth = BlockAw;
  typedef enum logic [StateWidth-1:0] {
    IDLE,
    W_KEY_SHARE0_0,
    W_KEY_SHARE0_1,
    W_KEY_SHARE0_2,
    W_KEY_SHARE0_3,
    W_KEY_SHARE0_4,
    W_KEY_SHARE0_5,
    W_KEY_SHARE0_6,
    W_KEY_SHARE0_7,
    W_KEY_SHARE1_0,
    W_KEY_SHARE1_1,
    W_KEY_SHARE1_2,
    W_KEY_SHARE1_3,
    W_KEY_SHARE1_4,
    W_KEY_SHARE1_5,
    W_KEY_SHARE1_6,
    W_KEY_SHARE1_7,
    W_IV_0,
    W_IV_1,
    W_IV_2,
    W_IV_3,
    W_DATA_IN_0,
    W_DATA_IN_1,
    W_DATA_IN_2,
    W_DATA_IN_3,
    R_DATA_OUT_0,
    R_DATA_OUT_1,
    R_DATA_OUT_2,
    R_DATA_OUT_3,
    W_CTRL_SHADOWED,
    W_CTRL_AUX_SHADOWED,
    W_TRIGGER_OFFSET,
    R_STATUS,
    FINISH
  } aes_wrap_ctrl_e;
  aes_wrap_ctrl_e aes_wrap_ctrl_ns, aes_wrap_ctrl_cs;
  logic [31:0] count_d, count_q;
  logic [127:0] data_out_d, data_out_q;
  //------------------------------------------------
  //Pulse detection of the start signal comming from software is added, since it takes a lot of time for the software
  //to stop the fsm by de-asserting the signal, the fsm was looping arround many times on the same input.
  //The rising edge detection solves that problem
  logic start_r, start_r_prev;
  
  logic start_pulse;
  assign start_pulse = (start_r == 1'b1) && (start_r_prev == 1'b0);
  
  logic start_latched;

 //----------------------------------------------------------------------------------------------------
  always_comb begin : aes_wrap_fsm
    // TL-UL
    h2d.a_valid           = 1'b0;
    h2d.a_opcode          = PutFullData;
    h2d.a_param           = 3'h0;                      // static
    h2d.a_size            = 2'h2;                      // static
    h2d.a_source          = 8'h0;                      // static
    h2d.a_address         = 32'hAAAAAAA8;
    h2d.a_mask            = 4'hF;                      // static
    h2d.a_data            = 32'h55555555;
    h2d.a_user.rsvd       = '0;                        // static
    h2d.a_user.instr_type = prim_mubi_pkg::MuBi4False; // static (Data)
    h2d.a_user.cmd_intg   = '0;                        // will be driven by tlul_cmd_intg_gen
    h2d.a_user.data_intg  = '0;                        // will be driven by prim_secded_enc
    h2d.d_ready           = 1'b1;                      // static

    // FSM
    aes_wrap_ctrl_ns      = aes_wrap_ctrl_cs;
    count_d               = count_q + 32'h1;
    data_out_d            = data_out_q;
    data_valid = 1'b0;
    
    

    
    unique case (aes_wrap_ctrl_cs)

      IDLE: begin

      
      // Wait for 'start' signal before beginning
      if (start_latched) begin
        // Poll the AES core to see if it's idle
         h2d.a_valid   = 1'b1;
         h2d.a_opcode  = Get;
         h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_STATUS_OFFSET};
    
         if (d2h.d_valid) begin
           h2d.a_valid = 1'b0;
           if (d2h.d_data[0] == 1'b1) begin
             aes_wrap_ctrl_ns = W_CTRL_AUX_SHADOWED;
             count_d          = 32'h0;
             data_valid     = 1'b0;
           end
         end
       end
     end

      W_CTRL_AUX_SHADOWED: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_CTRL_AUX_SHADOWED_OFFSET};
        h2d.a_data    = 32'h0;

        // We can't do back to back transactions. De-assert valid while receiving response.
        if (d2h.d_valid) begin
          h2d.a_valid = 1'b0;
          data_valid     = 1'b0;
        end

        // The shadow reg needs to be written twice.
        if (count_q >= 32'h3 && d2h.d_valid) begin
          aes_wrap_ctrl_ns = W_CTRL_SHADOWED;
        end
      end

      W_CTRL_SHADOWED: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_CTRL_SHADOWED_OFFSET};
        h2d.a_data    = {18'h0, 1'b0 ,1'b0, SIDELOAD, AES_256, AES_MODE, aes_decrypt_i ? AES_DEC : AES_ENC};

        // We can't do back to back transactions. De-assert valid while receiving response.
        if (d2h.d_valid) begin
          h2d.a_valid = 1'b0;
          data_valid     = 1'b0;
        end

        // The shadow reg needs to be written twice.
        if (count_q >= 32'h7 && d2h.d_valid) begin
          aes_wrap_ctrl_ns = SIDELOAD == 1'b1 ?
              (AES_MODE == AES_ECB ? W_DATA_IN_0 : W_IV_0) : W_KEY_SHARE0_0;
        end
      end

      W_KEY_SHARE0_0: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_KEY_SHARE0_0_OFFSET};
        h2d.a_data    = aes_key[31:0];
        if (d2h.d_valid) begin
          h2d.a_valid      = 1'b0;
          aes_wrap_ctrl_ns = W_KEY_SHARE0_1;
          data_valid     = 1'b0;
        end
      end

      W_KEY_SHARE0_1: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_KEY_SHARE0_1_OFFSET};
        h2d.a_data    = aes_key[63:32];
        if (d2h.d_valid) begin
          h2d.a_valid      = 1'b0;
          aes_wrap_ctrl_ns = W_KEY_SHARE0_2;
          data_valid     = 1'b0;
        end
      end

      W_KEY_SHARE0_2: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_KEY_SHARE0_2_OFFSET};
        h2d.a_data    = aes_key[95:64];
        if (d2h.d_valid) begin
          h2d.a_valid      = 1'b0;
          aes_wrap_ctrl_ns = W_KEY_SHARE0_3;
          data_valid     = 1'b0;
        end
      end

      W_KEY_SHARE0_3: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_KEY_SHARE0_3_OFFSET};
        h2d.a_data    = aes_key[127:96];
        if (d2h.d_valid) begin
          h2d.a_valid      = 1'b0;
          aes_wrap_ctrl_ns = W_KEY_SHARE0_4;
          data_valid     = 1'b0;
        end
      end

      W_KEY_SHARE0_4: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_KEY_SHARE0_4_OFFSET};
        h2d.a_data    = aes_key[159:128];
        if (d2h.d_valid) begin
          h2d.a_valid      = 1'b0;
          aes_wrap_ctrl_ns = W_KEY_SHARE0_5;
          data_valid     = 1'b0;
        end
      end

      W_KEY_SHARE0_5: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_KEY_SHARE0_5_OFFSET};
        h2d.a_data    = aes_key[195:160];
        if (d2h.d_valid) begin
          h2d.a_valid      = 1'b0;
          aes_wrap_ctrl_ns = W_KEY_SHARE0_6;
          data_valid     = 1'b0;
        end
      end

      W_KEY_SHARE0_6: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_KEY_SHARE0_6_OFFSET};
        h2d.a_data    = aes_key[227:196];
        if (d2h.d_valid) begin
          h2d.a_valid      = 1'b0;
          aes_wrap_ctrl_ns = W_KEY_SHARE0_7;
          data_valid     = 1'b0;
        end
      end

      W_KEY_SHARE0_7: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_KEY_SHARE0_7_OFFSET};
        h2d.a_data    = aes_key[255:228];
        if (d2h.d_valid) begin
          h2d.a_valid      = 1'b0;
          aes_wrap_ctrl_ns = W_KEY_SHARE1_0;
        end
      end

      W_KEY_SHARE1_0: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_KEY_SHARE1_0_OFFSET};
        h2d.a_data    = '0;
        if (d2h.d_valid) begin
          h2d.a_valid      = 1'b0;
          aes_wrap_ctrl_ns = W_KEY_SHARE1_1;
          data_valid     = 1'b0;
        end
      end

      W_KEY_SHARE1_1: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_KEY_SHARE1_1_OFFSET};
        h2d.a_data    = '0;
        if (d2h.d_valid) begin
          h2d.a_valid      = 1'b0;
          aes_wrap_ctrl_ns = W_KEY_SHARE1_2;
          data_valid     = 1'b0;
        end
      end

      W_KEY_SHARE1_2: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_KEY_SHARE1_2_OFFSET};
        h2d.a_data    = '0;
        if (d2h.d_valid) begin
          h2d.a_valid      = 1'b0;
          aes_wrap_ctrl_ns = W_KEY_SHARE1_3;
        end
      end

      W_KEY_SHARE1_3: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_KEY_SHARE1_3_OFFSET};
        h2d.a_data    = '0;
        if (d2h.d_valid) begin
          h2d.a_valid      = 1'b0;
          aes_wrap_ctrl_ns = W_KEY_SHARE1_4;
          data_valid     = 1'b0;
        end
      end

      W_KEY_SHARE1_4: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_KEY_SHARE1_4_OFFSET};
        h2d.a_data    = '0;
        if (d2h.d_valid) begin
          h2d.a_valid      = 1'b0;
          aes_wrap_ctrl_ns = W_KEY_SHARE1_5;
          data_valid     = 1'b0;
        end
      end

      W_KEY_SHARE1_5: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_KEY_SHARE1_5_OFFSET};
        h2d.a_data    = '0;
        if (d2h.d_valid) begin
          h2d.a_valid      = 1'b0;
          aes_wrap_ctrl_ns = W_KEY_SHARE1_6;
          data_valid     = 1'b0;
        end
      end

      W_KEY_SHARE1_6: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_KEY_SHARE1_6_OFFSET};
        h2d.a_data    = '0;
        if (d2h.d_valid) begin
          h2d.a_valid      = 1'b0;
          aes_wrap_ctrl_ns = W_KEY_SHARE1_7;
          data_valid     = 1'b0;
        end
      end

      W_KEY_SHARE1_7: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_KEY_SHARE1_7_OFFSET};
        h2d.a_data    = '0;
        if (d2h.d_valid) begin
          h2d.a_valid      = 1'b0;
          aes_wrap_ctrl_ns = AES_MODE == AES_ECB ? W_DATA_IN_0 : W_IV_0;
          data_valid     = 1'b0;
        end
      end

      W_IV_0: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_IV_0_OFFSET};
        h2d.a_data    = aes_iv[31:0];
        if (d2h.d_valid) begin
          h2d.a_valid      = 1'b0;
          aes_wrap_ctrl_ns = W_IV_1;
          data_valid     = 1'b0;
        end
      end

      W_IV_1: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_IV_1_OFFSET};
        h2d.a_data    = aes_iv[63:32];
        if (d2h.d_valid) begin
          h2d.a_valid      = 1'b0;
          aes_wrap_ctrl_ns = W_IV_2;
          data_valid     = 1'b0;
        end
      end

      W_IV_2: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_IV_2_OFFSET};
        h2d.a_data    = aes_iv[95:64];
        if (d2h.d_valid) begin
          h2d.a_valid      = 1'b0;
          aes_wrap_ctrl_ns = W_IV_3;
          data_valid     = 1'b0;
        end
      end

      W_IV_3: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_IV_3_OFFSET};
        h2d.a_data    = aes_iv[127:96];
        if (d2h.d_valid) begin
          h2d.a_valid      = 1'b0;
          aes_wrap_ctrl_ns = W_DATA_IN_0;
          data_valid     = 1'b0;
        end
      end

      W_DATA_IN_0: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_DATA_IN_0_OFFSET};
        h2d.a_data    = aes_input[31:0];
        if (d2h.d_valid) begin
          h2d.a_valid      = 1'b0;
          aes_wrap_ctrl_ns = W_DATA_IN_1;
          data_valid     = 1'b0;
        end
      end

      W_DATA_IN_1: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_DATA_IN_1_OFFSET};
        h2d.a_data    = aes_input[63:32];
        if (d2h.d_valid) begin
          h2d.a_valid      = 1'b0;
          aes_wrap_ctrl_ns = W_DATA_IN_2;
          data_valid     = 1'b0;
        end
      end

      W_DATA_IN_2: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_DATA_IN_2_OFFSET};
        h2d.a_data    = aes_input[95:64];
        if (d2h.d_valid) begin
          h2d.a_valid      = 1'b0;
          aes_wrap_ctrl_ns = W_DATA_IN_3;
          data_valid     = 1'b0;
        end
      end

      W_DATA_IN_3: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = PutFullData;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_DATA_IN_3_OFFSET};
        h2d.a_data    = aes_input[127:96];
        if (d2h.d_valid) begin
          // Clear the counter to serve as a reference for the experiments.
          h2d.a_valid      = 1'b0;
          aes_wrap_ctrl_ns = R_STATUS;
          count_d          = '0;
          data_valid     = 1'b0;
        end
      end

      R_STATUS: begin
        // After providing the last data word, the DUT will start. Poll the status register until
        // the DUT is idle and has valid output data.
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = Get;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_STATUS_OFFSET};

        if (d2h.d_valid) begin
          h2d.a_valid = 1'b0;
          if ((d2h.d_data[0] == 1'b1) && (d2h.d_data[3] == 1'b1)) begin
            aes_wrap_ctrl_ns = R_DATA_OUT_0;
            data_valid     = 1'b0;
          end
        end
      end

      R_DATA_OUT_0: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = Get;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_DATA_OUT_0_OFFSET};

        if (d2h.d_valid) begin
          h2d.a_valid      = 1'b0;
          data_out_d[31:0] = d2h.d_data;
          aes_wrap_ctrl_ns = R_DATA_OUT_1;
          data_valid     = 1'b0;
        end
      end

      R_DATA_OUT_1: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = Get;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_DATA_OUT_1_OFFSET};

        if (d2h.d_valid) begin
          h2d.a_valid       = 1'b0;
          data_out_d[63:32] = d2h.d_data;
          aes_wrap_ctrl_ns  = R_DATA_OUT_2;
          data_valid     = 1'b0;
        end
      end

      R_DATA_OUT_2: begin
        h2d.a_valid   = 1'b1;
        h2d.a_opcode  = Get;
        h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_DATA_OUT_2_OFFSET};

        if (d2h.d_valid) begin
          h2d.a_valid       = 1'b0;
          data_out_d[95:64] = d2h.d_data;
          aes_wrap_ctrl_ns  = R_DATA_OUT_3;
          data_valid     = 1'b0;
        end
      end

      R_DATA_OUT_3: begin
          h2d.a_valid   = 1'b1;
          h2d.a_opcode  = Get;
          h2d.a_address = {{{32-BlockAw}{1'b0}}, AES_DATA_OUT_3_OFFSET};
            
          if (d2h.d_valid) begin
            h2d.a_valid        = 1'b0;
            data_out_d[127:96] = d2h.d_data;
            aes_wrap_ctrl_ns   = FINISH;
            data_valid     = 1'b0; 
            
            
          end
        end

      FINISH: begin
        // The initial FSM ran the aes module only one time, going back to idle was added to allow the fsm to process a new input
        aes_wrap_ctrl_ns = IDLE;
        data_valid     = 1'b1; // valid signal is asserted only when a new input is processed and the output is ready
        
        end


      default: begin
        aes_wrap_ctrl_ns = FINISH;
      end
    endcase // aes_wrap_ctrl_cs

    // We can't handle TL-UL errors. Abort.
    if (d2h.d_valid && d2h.d_error) begin
      aes_wrap_ctrl_ns = FINISH;
    end
  end

always_ff @(posedge clk_i or negedge rst_ni) begin : fsm_reg
  if (!rst_ni) begin
    aes_wrap_ctrl_cs <= IDLE;
    count_q          <= 32'b0;
    start_r          <= 1'b0;
    start_r_prev <= 1'b0 ;
    start_latched <= 1'b0;
  
  end else begin
    aes_wrap_ctrl_cs <= aes_wrap_ctrl_ns;
    count_q          <= count_d;
    if (aes_wrap_ctrl_cs == IDLE && start_pulse) begin
    start_latched <= 1'b1;  // latch the request
     end else if (aes_wrap_ctrl_cs != IDLE) begin
    start_latched <= 1'b0;  // clear when leaving IDLE
     end
 
    
    start_r_prev <= start_r; 
    start_r <= start;
    
   
   end
  end





  always_ff @(posedge clk_i or negedge rst_ni) begin : data_out_reg
    if (!rst_ni) begin
      data_out_q <= '0;
    end else begin
      data_out_q <= data_out_d;
    end
  end
  assign aes_output = data_out_q;
 
  
  // Ila's were added for testing and can be removed for the final design
  /*
  ila_3 ila_3(
  .clk(clk_i),
  .probe0(data_valid),
  .probe1(aes_wrap_ctrl_cs),
  .probe2(start_pulse),
  .probe3(start_latched)
  
  );
  */
  
endmodule
