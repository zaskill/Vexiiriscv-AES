package spinal.lib.bus.amba4.axi

import spinal.core._
import spinal.lib._


case class Axi4WriteOnly(config: Axi4Config) extends Bundle with IMasterSlave with Axi4Bus{

  val aw = Stream(Axi4Aw(config))
  val w = Stream(Axi4W(config))
  val b = Stream(Axi4B(config))


  def writeCmd = aw
  def writeData = w
  def writeRsp = b

  def <<(that : Axi4) : Unit = that >> this
  def >> (that : Axi4) : Unit = {
    this.writeCmd drive that.writeCmd
    this.writeData drive that.writeData
    that.writeRsp drive this.writeRsp
  }

  def <<(that : Axi4WriteOnly) : Unit = that >> this
  def >> (that : Axi4WriteOnly) : Unit = {
    this.writeCmd drive that.writeCmd
    this.writeData drive that.writeData
    that.writeRsp drive this.writeRsp
  }

  def awValidPipe() : Axi4WriteOnly = {
    val sink = Axi4WriteOnly(config)
    sink.aw << this.aw.validPipe()
    sink.w  << this.w
    sink.b  >> this.b
    sink
  }

  def setIdle(): this.type = {
    this.writeCmd.setIdle()
    this.writeData.setIdle()
    this.writeRsp.setBlocked()
    this
  }

  def setBlocked(): this.type = {
    this.writeCmd.setBlocked()
    this.writeData.setBlocked()
    this.writeRsp.setIdle()
    this
  }

  def toAxi4(): Axi4 = {
    val ret = Axi4(config)
    this >> ret
  
    ret.readCmd.setIdle()
    ret.readRsp.setBlocked()

    ret
  }

  def toFullConfig(): Axi4WriteOnly = {
    val ret = Axi4WriteOnly(config.toFullConfig())
    ret << this
    ret
  }

  def pipelined(
    aw: StreamPipe = StreamPipe.NONE,
    w: StreamPipe = StreamPipe.NONE,
    b: StreamPipe = StreamPipe.NONE
  ): Axi4WriteOnly = {
    val ret = cloneOf(this)
    ret.aw << this.aw.pipelined(aw)
    ret.w << this.w.pipelined(w)
    ret.b.pipelined(b) >> this.b
    ret
  }

  override def asMaster(): Unit = {
    master(aw, w)
    slave(b)
  }

  def formalContext(maxBursts: Int = 16, maxStrbs: Int = 256) = new Composite(this, "formal") {
    import spinal.core.formal._

    val addrChecker = aw.payload.formalContext()

    val oRecord = FormalAxi4Record(config, maxStrbs).init()

    val histInput = Flow(cloneOf(oRecord))
    histInput := histInput.getZero
    val hist = HistoryModifyable(histInput, maxBursts)
    hist.io.inStreams.map(_.valid := False)
    hist.io.inStreams.map(_.payload := oRecord)
    hist.io.outStreams.map(_.ready := False)

    val (awExist, awId) = hist.findFirst(x => x.valid && !x.axDone)
    val (wExist, wId) = hist.findFirst(x => x.valid && !x.seenLast)
    val (bExist, bId) =
      hist.findFirst(x => x.valid && !x.responsed && { if (config.useId) b.id === x.id else True })

    val awRecord = CombInit(oRecord)
    val awValid = False
    val addressLogic = new Area {
      when(aw.valid) {
        val ax = aw.asInstanceOf[Stream[Axi4Ax]]
        when(awExist) {
          awRecord := hist.io.outStreams(awId)
          awRecord.allowOverride
          awRecord.assignFromAx(ax)
          awValid := True
        }
          .otherwise { histInput.assignFromAx(ax) }
      }
      when(awValid) {
        hist.io.inStreams(awId).payload := awRecord
        hist.io.inStreams(awId).valid := awValid
      }
    }

    val wRecord = CombInit(oRecord)
    val wValid = False
    val dataLogic = new Area {
      val selected = CombInit(oRecord)
      when(w.valid) {
        when(wExist) {
          wRecord := hist.io.outStreams(wId)
          selected := hist.io.outStreams(wId)
          when(awValid && wId === awId) {
            awRecord.assignFromW(w, selected)
          }.otherwise { wRecord.assignFromW(w, selected); wValid := True }
        }.otherwise { histInput.assignFromW(w, oRecord) }
      }
      when(wValid) {
        hist.io.inStreams(wId).payload := wRecord
        hist.io.inStreams(wId).valid := wValid
      }
    }

    val respErrors = Vec(Bool(), 3)
    respErrors.map(_ := False)

    val bRecord = CombInit(oRecord)
    val bValid = False
    val responseLogic = new Area {
      val selected = CombInit(oRecord)
      when(b.valid) {
        when(bExist) {
          bRecord := hist.io.outStreams(bId)
          selected := hist.io.outStreams(bId)
          when(awValid && bId === awId) {
            awRecord.assignFromB(b)
          }.elsewhen(wValid && bId === wId) {
            wRecord.assignFromB(b)
          }.otherwise { bRecord.assignFromB(b); bValid := True }

          hist.io.outStreams(bId).ready := b.ready & bRecord.axDone & bRecord.seenLast

          respErrors(0) := !selected.axDone
          respErrors(1) := selected.axDone & !selected.seenLast
          if (config.useResp && config.useLock)
            respErrors(2) := selected.axDone & b.resp === Axi4.resp.EXOKAY & !selected.isLockExclusive
        }.otherwise {
          respErrors(0) := True
        }
      }
      when(bValid) {
        hist.io.inStreams(bId).payload := bRecord
        hist.io.inStreams(bId).valid := bValid
      }
      val strbsChecker =
        if (config.useStrb) selected.checkStrbs(b.fire & bExist & selected.axDone & selected.seenLast)
        else null
    }

    when((aw.valid & !awExist) | (w.valid & !wExist)) {
      histInput.valid := True
    }

    val errors = new Area {
      val reset = ClockDomain.current.isResetActive
      val ValidWhileReset = (reset | past(reset)) & (aw.valid === True | w.valid === True)
      val RespWhileReset = (reset | past(reset)) & (b.valid === True)
      val WrongStrb = if (config.useStrb) responseLogic.strbsChecker.strbError else False
      val WrongResponse = respErrors.reduce(_ | _)
      val DataNumberDonotFitLen = hist.io.outStreams.map(x => x.valid & x.checkLen()).reduce(_ | _)
    }

    def formalAssertsMaster(maxStallCycles: Int = 0) = new Area {
      aw.formalAssertsMaster()
      w.formalAssertsMaster()
      b.formalAssertsTimeout(maxStallCycles)

      when(aw.valid) {
        addrChecker.formalAsserts()
      }

      assert(!errors.DataNumberDonotFitLen)
      assert(!errors.WrongStrb)
      assert(!errors.ValidWhileReset)
    }

    def formalAssumesMaster(maxStallCycles: Int = 0) = new Area {
      aw.formalAssumesTimeout(maxStallCycles)
      w.formalAssumesTimeout(maxStallCycles)
      b.formalAssumesSlave()

      assume(!errors.WrongResponse)
      assume(!errors.RespWhileReset)
    }
    
    def formalAssertsSlave(maxStallCycles: Int = 0) = new Area {
      aw.formalAssertsTimeout(maxStallCycles)
      w.formalAssertsTimeout(maxStallCycles)
      b.formalAssertsMaster()

      assert(!errors.WrongResponse)
      assert(!errors.RespWhileReset)
    }

    def formalAssumesSlave(maxStallCycles: Int = 0) = new Area {
      aw.formalAssumesSlave()
      w.formalAssumesSlave()
      b.formalAssumesTimeout(maxStallCycles)

      when(aw.valid) {
        addrChecker.formalAssumes()
      }

      assume(!errors.DataNumberDonotFitLen)
      assume(!errors.WrongStrb)
      assume(!errors.ValidWhileReset)
    }

    def formalCovers() = new Area {
      aw.formalCovers(2)
      when(aw.fire) {
        addrChecker.formalCovers()
      }
      w.formalCovers(2)
      when(w.fire) {
        w.payload.formalCovers()
      }
      b.formalCovers(2)
      when(b.fire) {
        b.payload.formalCovers()
      }
    }
  }
}
