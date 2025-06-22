package vexiiriscv.soc.micro

import spinal.core._
import spinal.lib._
import spinal.lib.com.uart._

case class UartToRamLoaderConfig(
  baseAddress: Long,
  maxWords: Int,
  uartClockHz: Long,
  baudRate: Int
)

class UartToRamLoader(config: UartToRamLoaderConfig) extends Component {
  val io = new Bundle {
    val uart = master(Uart())
  }

  val uartCtrl = new UartCtrl(UartCtrlGenerics(
    dataWidthMax = 8,
    clockDividerWidth = 20,
    preSamplingSize = 1,
    samplingSize = 5,
    postSamplingSize = 2
  ))

  // Manually set UART config registers
  uartCtrl.io.config.clockDivider := ((config.uartClockHz / config.baudRate) / uartCtrl.io.config.g.rxSamplePerBit).toInt - 1
  uartCtrl.io.config.frame.dataLength := 7 // 8 bits (value +1)
  uartCtrl.io.config.frame.parity := UartParityType.NONE
  uartCtrl.io.config.frame.stop := UartStopType.ONE

  // Connect UART pins
  io.uart <> uartCtrl.io.uart

  val rx = uartCtrl.io.read

  val byteCounter = Reg(UInt(2 bits)) init(0)
  val word = Reg(Bits(32 bits)) init(0)
  val address = Reg(UInt(log2Up(config.maxWords) bits)) init(0)

  val mem = Mem(Bits(32 bits), config.maxWords)

  when(rx.valid) {
    word := rx.payload ## word(31 downto 8) // Corrected: remove 'ac'
    byteCounter := byteCounter + 1

    when(byteCounter === 3) {
      mem.write(address, word)
      address := address + 1
      byteCounter := 0
    }
  }
}
