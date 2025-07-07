package vexiiriscv.soc.micro

import spinal.core._
import spinal.core.fiber._
import spinal.lib._
import spinal.lib.bus._
import spinal.lib.bus.tilelink.BusParameter
import spinal.lib.misc.InterruptNode

import scala.collection.mutable.ArrayBuffer

// This object provide a set of utilities
object PeripheralDemo {
  // This function will be used during the tilelink parameter negotiation
  // This will translate what the masters propose into what is actually supported by the peripheral .
  // In this implementation, we don't really care about what is proposed. Instead we enforce 12 bits address, 32 bits data ...
  def getTilelinkSupport(proposed: bus.tilelink.M2sSupport) = bus.tilelink.SlaveFactory.getSupported(
    addressWidth = 12,
    dataWidth = 32,
    allowBurst = false,
    proposed = proposed
  )
}

// This class will carry most parameters related to our peripheral.
// It doesn't include the tilelink bus parameter, as those are toplevel negotiation related, and so, not really user related
class PeripheralDemoParam(val ledCount : Int = 8, val buttonCount : Int = 4)

// In this demo, the peripheral will :
// - Implement a tilelink slave interface to provide access to its functionalities
// - Output a leds bus
// - Input a buttons bus
// - Output an interrupt which is function of the buttons
// PeripheralDemo will be a pure SpinalHDL Component which define hardware explicitly. No Fiber yet.
class PeripheralDemo(p : PeripheralDemoParam, busParameter : BusParameter) extends Component{
  //Let's define all our input / output / busses
  val io = new Bundle{
    val bus = slave(tilelink.Bus(busParameter))
    val leds = out Bits(p.ledCount bits)
    val buttons = in Bits(p.buttonCount bits)
    val interrupt = out Bool()
  }

  // tilelink.SlaveFactory is a utility which allow to easily generate the peripheral register mapping from a tilelink bus
  val mapper = new tilelink.SlaveFactory(io.bus, allowBurst = false)

  // Create a register at address 0x00 that we can read and write. That register is assigned to the io.led
  val ledReg = mapper.driveAndRead(io.leds, address = 0x00) init(0)

  // Add a BufferCC which read the io.buttons to allow getting the buttons value without metastable issues
  val buttonsCc = BufferCC(io.buttons)

  // Make the io.buttons readable by the io.bus at address 0x04
  mapper.read(buttonsCc, address = 0x04)

  // Here we will handle all interrupt logic.
  // val interrupts = new Area { ... } create a namespace for all the contained hardware
  val interrupts = new Area{
    val enables = mapper.createReadAndWrite(Bits(p.buttonCount bits), address = 0x10)
    io.interrupt := (enables & io.buttons) =/= 0
  }
}

// PeripheralDemoFiber will be kinda a integration layer for our peripheral into a SoC.
// It will :
// - Handle the tilelink parameter negotiation
// - Instantiate our PeripheralDemo component
// - Bind things together
// - Export the PeripheralDemo.io.led to the toplevel of our SoC (so we don't have to do it manually later on)
class PeripheralDemoFiber(p : PeripheralDemoParam = new PeripheralDemoParam()) extends Area{
  val node = tilelink.fabric.Node.slave()
  val interrupt = InterruptNode.master()

  // Here we define an elaboration thread which will start to run once the build phase (of the Fiber ecosystem) is reached
  val logic = Fiber build new Area{
    // Let's handle the tilelink bus parameter negotiation
    // m2s are general requests negotiation (get, put, acquire, ..)
    node.m2s.supported.load(PeripheralDemo.getTilelinkSupport(node.m2s.proposed))
    // s2m are for memory coherent requests negotiation (probe, ...).
    // This is memory coherency related stuff, and we don't want to deal with that at all ^^
    node.s2m.none()

    // Let instantiate our hardware and bind it
    val core = new PeripheralDemo(p, node.bus.p)
    core.io.bus <> node.bus
    core.io.interrupt <> interrupt.flag

    // Let export the leds and buttons of the PeripheralDemo to the toplevel io.
    val leds = core.io.leds.toIo()
    val buttons = core.io.buttons.toIo()
  }
}
