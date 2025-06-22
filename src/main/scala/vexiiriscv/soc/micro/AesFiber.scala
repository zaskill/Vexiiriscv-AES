package vexiiriscv.soc.micro

import spinal.core._
import spinal.core.fiber._
import spinal.lib._
import spinal.lib.bus.tilelink
import spinal.lib.bus.tilelink.Bus

object AesFiber {
  def getTilelinkSupport(proposed: tilelink.M2sSupport) = tilelink.SlaveFactory.getSupported(
    addressWidth = 12,
    dataWidth = 32,
    allowBurst = false,
    proposed = proposed
  )
}

class AesFiber extends Area {
  val node = tilelink.fabric.Node.slave()

  val logic = Fiber build new Area {
    node.m2s.supported.load(AesFiber.getTilelinkSupport(node.m2s.proposed))
    node.s2m.none()

    val aes = new AesBlackBox
    aes.io.clk_i            := ClockDomain.current.clock
    aes.io.rst_ni           := !ClockDomain.current.reset
    aes.io.rst_shadowed_ni  := !ClockDomain.current.reset
    aes.io.clk_edn_i        := ClockDomain.current.clock
    aes.io.rst_edn_ni       := !ClockDomain.current.reset
    aes.io.alert_rx_i       := B(0, 2 bits)
    aes.io.lc_escalate_en_i := B(0, 4 bits)
    aes.io.keymgr_key_i     := B(0, 384 bits)
    aes.io.edn_i            := B(0, 16 bits)

    // --- Packing and Unpacking ---
    aes.io.tl_i := packTLBundleToBits(node.bus)
    unpackBitsToTLBundle(aes.io.tl_o, node.bus)
  }

  // Compose the A channel into 88 bits for OpenTitan
  def packTLBundleToBits(bus: tilelink.Bus): Bits = {
    // Adjust widths if your bus is parameterized differently!
    bus.a.valid.asBits ##                                 // 1
    bus.a.payload.opcode.asBits ##                        // 3
    bus.a.payload.param ##                                // 3
    bus.a.payload.size.asBits.resize(3) ##                // 3 (pad to 3 bits if only 2)
    bus.a.payload.source.asBits.resize(4) ##              // 4 (pad to 4 bits if smaller)
    bus.a.payload.address.asBits.resize(32) ##            // 32
    bus.a.payload.mask.asBits.resize(4) ##                // 4
    bus.a.payload.data.asBits.resize(32) ##               // 32
    bus.d.ready.asBits ##                                 // 1
    B(0, 5 bits)                                          // padding to reach 88 bits
  }

  // Unpack 72-bit tl_o from AES into D channel of SoC bus
 def unpackBitsToTLBundle(bits: Bits, bus: tilelink.Bus): Unit = {
  var offset = 0
  bus.d.valid := bits(offset)
  offset += 1

  bus.d.payload.opcode.assignFromBits(bits(offset + 2 downto offset))
  offset += 3

  bus.d.payload.param := bits(offset + 2 downto offset)
  offset += 3

  // Use the ACTUAL WIDTH for your signals:
  val sizeWidth = bus.d.payload.size.getWidth
  bus.d.payload.size := bits(offset + sizeWidth - 1 downto offset).asUInt
  offset += sizeWidth

  val sourceWidth = bus.d.payload.source.getWidth
  bus.d.payload.source := bits(offset + sourceWidth - 1 downto offset).asUInt
  offset += sourceWidth

  // Only assign sink if width > 0!
  val sinkWidth = bus.d.payload.sink.getWidth
  if (sinkWidth > 0) {
    bus.d.payload.sink := bits(offset + sinkWidth - 1 downto offset).asUInt
    offset += sinkWidth
  }

  bus.d.payload.denied := bits(offset)
  offset += 1

  bus.d.payload.data := bits(offset + 31 downto offset)
  offset += 32

  bus.d.payload.corrupt := bits(offset)
  offset += 1

  bus.a.ready := bits(offset)
  offset += 1
}

}
