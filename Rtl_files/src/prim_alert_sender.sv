//The outputs were set to 0 to avoid alert detection leading to the AES Ip stalling

module prim_alert_sender
  import prim_alert_pkg::*;
#(
  parameter bit AsyncOn = 1'b1,
  parameter bit IsFatal = 1'b0
)(
  input             clk_i,
  input             rst_ni,
  input             alert_test_i,
  input             alert_req_i,
  output logic      alert_ack_o,
  output logic      alert_state_o,
  input  alert_rx_t alert_rx_i,
  output alert_tx_t alert_tx_o
);
  assign alert_tx_o = '{alert_p: 1'b0, alert_n: 1'b1};


  assign alert_ack_o    = 1'b0;
  assign alert_state_o  = 1'b0;

endmodule
