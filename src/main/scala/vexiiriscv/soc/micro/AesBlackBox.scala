package vexiiriscv.soc.micro

import spinal.core._

class AesBlackBox extends BlackBox {
  setDefinitionName("aes")  // <-- CRITICAL: matches module aes in aes.sv
  val io = new Bundle {
    val clk_i            = in Bool()
    val rst_ni           = in Bool()
    val rst_shadowed_ni  = in Bool()
    val tl_i             = in  Bits(88 bits)
    val tl_o             = out Bits(72 bits)
    val alert_rx_i       = in  Bits(2 bits)
    val alert_tx_o       = out Bits(2 bits)
    val idle_o           = out Bits(4 bits)
    val lc_escalate_en_i = in  Bits(4 bits)
    val clk_edn_i        = in  Bool()
    val rst_edn_ni       = in  Bool()
    val edn_o            = out Bits(16 bits)
    val edn_i            = in  Bits(16 bits)
    val keymgr_key_i     = in  Bits(384 bits)
  }
  addRTLPath("/home/zakaria/opentitan/aes_rtl_extract/aes/aes.sv")
}
