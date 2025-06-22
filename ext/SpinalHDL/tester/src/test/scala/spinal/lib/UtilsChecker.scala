package spinal.lib

import spinal.core._
import spinal.core.formal._
// import spinal.lib.{StreamFifo, History, OHToUInt}
import spinal.lib.formal._

case class PriorityMuxFixture(width: Int, msbFirst: Boolean) extends Component{
  val inputWidth = log2Up(width)
  val io = new Bundle {
    val sel = in Bits(width bits)
    val inputs = in Vec(Bits(inputWidth bits), width)
    val output = out(Bits(inputWidth bits))
  }
  val output = PriorityMux(io.sel, io.inputs, msbFirst)
  io.output <> output
}

class PriorityMuxChecker extends SpinalFormalFunSuite {
  def testMain(width: Int, msbFirst: Boolean, backend: FormalBackend) {
    FormalConfig
      .withBackend(backend)
      .withBMC(3)
      .withCover(3)
      .doVerify(new Component {
        val sel = in Bits(width bits)
        val inputWidth = log2Up(width)
        val inputs = in Vec(Bits(inputWidth bits), width)
        val output = out(Bits(inputWidth bits))

        val dut = FormalDut(PriorityMuxFixture(width, msbFirst))
        sel <> dut.io.sel
        inputs <> dut.io.inputs
        output <> dut.io.output

        for(id <- 0 until inputs.size){
          assume(inputs(id) === id)
        }
        
        val selReorder = if(!msbFirst) sel else sel.asBools.reverse.asBits
        val inputsReorder = if(!msbFirst) inputs else inputs.reverse

        val selected = OhMux.or(OHMasking.firstV2(selReorder), inputsReorder)
        val expected = cloneOf(output)
        when(sel === 0){
          expected := inputsReorder.last
        }.otherwise{
          expected := selected
        }
        assert(expected === output)

        cover(sel === 0)        
        cover(sel =/= 0)
        cover(sel === sel.getAllTrue)
      })
  }

  test("check in out basic - symbiyosys") {
    testMain(2, false, SymbiYosysFormalBackend)
  }
  test("check in out reverse - symbiyosys") {
    testMain(2, true, SymbiYosysFormalBackend)
  }
  test("check 3 in out basic - symbiyosys") {
    testMain(3, false, SymbiYosysFormalBackend)
  }
  test("check 3 in out reverse - symbiyosys") {
    testMain(3, true, SymbiYosysFormalBackend)
  }

  test("check in out basic - ghdl") {
    testMain(2, false, GhdlFormalBackend)
  }
  test("check in out reverse - ghdl") {
    testMain(2, true, GhdlFormalBackend)
  }
  test("check 3 in out basic - ghdl") {
    testMain(3, false, GhdlFormalBackend)
  }
  test("check 3 in out reverse - ghdl") {
    testMain(3, true, GhdlFormalBackend)
  }
}
