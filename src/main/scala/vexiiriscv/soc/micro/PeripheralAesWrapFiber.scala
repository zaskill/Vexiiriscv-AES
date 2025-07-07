package vexiiriscv.soc.micro

import spinal.core._
import spinal.core.fiber._
import spinal.lib._
import spinal.lib.bus.tilelink._

class PeripheralAesWrapFiber extends Area {
  val node = fabric.Node.slave()

  val logic = Fiber build new Area {
    node.m2s.supported.load(SlaveFactory.getSupported(
      addressWidth = 12,
      dataWidth = 32,
      allowBurst = false,
      proposed = node.m2s.proposed
    ))
    node.s2m.none()

    val busParameter = node.bus.p

    val core = new Component {
      setDefinitionName("PeripheralAesCore")

      val io = new Bundle {
        val bus        = slave(Bus(busParameter))
        val aes_output = out Bits(128 bits)
        val test_done  = out Bool()
        val clk        = in Bool()
        val reset      = in Bool()
      }

      val aes = new AesWrapBlackBox
      aes.io.clk_i  := io.clk
      aes.io.rst_ni := !io.reset

      val inputData     = Reg(Bits(128 bits)) init(0)
      val keyData       = Reg(Bits(256 bits)) init(0)
      val aesOutput     = Reg(Bits(128 bits)) init(0)
      val aesIv         = Reg(Bits(128 bits)) init(0)
      val decryptModeReg = Reg(Bool()) init(False)
      val startReg      = Reg(Bool) init(False)

      val testDoneRaw   = Reg(Bool) init(False)
      val testDonePrev  = RegNext(testDoneRaw) init(False)
      val testDone      = Bool()
      testDone := testDoneRaw && !testDonePrev

      val startPulse = startReg && !RegNext(startReg, False)
      aes.io.start         := startPulse
      aes.io.aes_input     := inputData
      aes.io.aes_key       := keyData
      aes.io.aes_decrypt_i := decryptModeReg
      aes.io.aes_iv        := aesIv

      when(startPulse) {
        startReg := False
        testDoneRaw := False  // Clear done on new start
      }

      when(aes.io.test_done_o) {
        aesOutput   := aes.io.aes_output
        testDoneRaw := True
      }

      io.aes_output := aesOutput
      io.test_done  := testDone

      val bus     = io.bus
      val address = bus.a.payload.address(11 downto 2)
      val wdata   = bus.a.payload.data
      val rdata   = Bits(32 bits)

      rdata := 0
      bus.a.ready := True
      bus.d.valid := bus.a.valid

      bus.d.payload.opcode  := Opcode.D.ACCESS_ACK
      bus.d.payload.param   := 0
      bus.d.payload.size    := bus.a.payload.size
      bus.d.payload.source  := bus.a.payload.source
      bus.d.payload.sink    := 0
      bus.d.payload.denied  := False
      bus.d.payload.data    := rdata
      bus.d.payload.corrupt := False

      switch(address) {
        // Input data
        is(0x001) { when(bus.a.payload.opcode === Opcode.A.PUT_FULL_DATA) { inputData(31  downto 0)   := wdata } otherwise { rdata := inputData(31  downto 0)   } }
        is(0x002) { when(bus.a.payload.opcode === Opcode.A.PUT_FULL_DATA) { inputData(63  downto 32)  := wdata } otherwise { rdata := inputData(63  downto 32)  } }
        is(0x003) { when(bus.a.payload.opcode === Opcode.A.PUT_FULL_DATA) { inputData(95  downto 64)  := wdata } otherwise { rdata := inputData(95  downto 64)  } }
        is(0x004) { when(bus.a.payload.opcode === Opcode.A.PUT_FULL_DATA) { inputData(127 downto 96)  := wdata } otherwise { rdata := inputData(127 downto 96)  } }

        // Key data
        is(0x005) { when(bus.a.payload.opcode === Opcode.A.PUT_FULL_DATA) { keyData(31  downto 0)    := wdata } otherwise { rdata := keyData(31  downto 0)    } }
        is(0x006) { when(bus.a.payload.opcode === Opcode.A.PUT_FULL_DATA) { keyData(63  downto 32)   := wdata } otherwise { rdata := keyData(63  downto 32)   } }
        is(0x007) { when(bus.a.payload.opcode === Opcode.A.PUT_FULL_DATA) { keyData(95  downto 64)   := wdata } otherwise { rdata := keyData(95  downto 64)   } }
        is(0x008) { when(bus.a.payload.opcode === Opcode.A.PUT_FULL_DATA) { keyData(127 downto 96)   := wdata } otherwise { rdata := keyData(127 downto 96)   } }
        is(0x009) { when(bus.a.payload.opcode === Opcode.A.PUT_FULL_DATA) { keyData(159 downto 128)  := wdata } otherwise { rdata := keyData(159 downto 128)  } }
        is(0x00A) { when(bus.a.payload.opcode === Opcode.A.PUT_FULL_DATA) { keyData(191 downto 160)  := wdata } otherwise { rdata := keyData(191 downto 160)  } }
        is(0x00B) { when(bus.a.payload.opcode === Opcode.A.PUT_FULL_DATA) { keyData(223 downto 192)  := wdata } otherwise { rdata := keyData(223 downto 192)  } }
        is(0x00C) { when(bus.a.payload.opcode === Opcode.A.PUT_FULL_DATA) { keyData(255 downto 224)  := wdata } otherwise { rdata := keyData(255 downto 224)  } }

        // Output data (read only)
        is(0x00E) { rdata := aesOutput(31  downto 0)    }
        is(0x00F) { rdata := aesOutput(63  downto 32)   }
        is(0x010) { rdata := aesOutput(95  downto 64)   }
        is(0x011) { rdata := aesOutput(127 downto 96)   }

        // Test done
        is(0x012) { rdata := B(0, 31 bits) ## testDone }

        // Decrypt mode
        is(0x013) {
          when(bus.a.payload.opcode === Opcode.A.PUT_FULL_DATA) {
            decryptModeReg := wdata(0)
          } otherwise {
            rdata := B(0, 31 bits) ## decryptModeReg
          }
        }

        // IV
        is(0x014) { when(bus.a.payload.opcode === Opcode.A.PUT_FULL_DATA) { aesIv(31  downto 0)    := wdata } otherwise { rdata := aesIv(31  downto 0)    } }
        is(0x015) { when(bus.a.payload.opcode === Opcode.A.PUT_FULL_DATA) { aesIv(63  downto 32)   := wdata } otherwise { rdata := aesIv(63  downto 32)   } }
        is(0x016) { when(bus.a.payload.opcode === Opcode.A.PUT_FULL_DATA) { aesIv(95  downto 64)   := wdata } otherwise { rdata := aesIv(95  downto 64)   } }
        is(0x017) { when(bus.a.payload.opcode === Opcode.A.PUT_FULL_DATA) { aesIv(127 downto 96)   := wdata } otherwise { rdata := aesIv(127 downto 96)   } }

        // Start signal
        is(0x019) {
          when(bus.a.payload.opcode === Opcode.A.PUT_FULL_DATA) {
            startReg := True
          }
        }
      }
    }

    core.io.bus   <> node.bus
    core.io.clk   := ClockDomain.current.clock
    core.io.reset := ClockDomain.current.reset

    val aes_output = core.io.aes_output.toIo()
    val test_done  = core.io.test_done.toIo()
  }
}
