package vexiiriscv.soc.micro

import spinal.core._

class AesWrapBlackBox extends BlackBox {
  setDefinitionName("aes_wrap")

  val io = new Bundle {
    val clk_i         = in Bool()
    val rst_ni        = in Bool()
    val aes_input     = in Bits(128 bits)
    val aes_key       = in Bits(256 bits)
    val aes_decrypt_i = in Bool()
    val aes_iv     = in Bits(128 bits)
    val start      = in Bool()
    val aes_output    = out Bits(128 bits)
    val alert_recov_o = out Bool()
    val alert_fatal_o = out Bool()
    val test_done_o   = out Bool()
  }

  noIoPrefix() //le module Verilog n'utilise pas le préfixe "io_"
}



