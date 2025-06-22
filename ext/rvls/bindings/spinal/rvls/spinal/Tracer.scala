package rvls.spinal

import org.apache.commons.io.FileUtils

import java.io.{BufferedWriter, File, FileWriter}
import spinal.core._
import spinal.core.sim._
import spinal.lib.bus.misc.{AddressMapping, OrMapping, SizeMapping}

class TraceIo (var write: Boolean,
               var address: Long,
               var data: Long,
               var mask: Int,
               var size: Int,
               var error: Boolean){

  def this(){
    this(false, 0l, 0l, 0, 0, false)
  }
  def serialized() = f"${write.toInt} $address%016x $data%016x $mask%02x $size ${error.toInt}"
}

trait TraceBackend{
  def spinalSimFlusher(period: Long): this.type = {
    periodicaly(period)(flush())
    onSimEnd(close())
    this
  }

  def spinalSimTime(period: Long): Unit = {
    periodicaly(period)(time(simTime()))
  }

  def spinalSimPeriod(period : Long) = {
    spinalSimFlusher(period)
    spinalSimTime(period)
  }

  def newCpuMemoryView(viewId : Int, readIds : Long, writeIds : Long)
  def newCpu(hartId : Int, isa : String, priv : String, physWidth : Int, pmpNum : Int, memoryViewId : Int) : Unit
  def loadElf(offset : Long, path : File) : Unit
  def loadBin(offset: Long, path: File): Unit
  def loadBytes(offset: Long, bytes: Array[Byte]): Unit
  def setPc(hartId : Int, pc : Long): Unit
  def writeRf(hardId : Int, rfKind : Int, address : Int, data : Long) //address out of range mean unknown
  def readRf(hardId : Int, rfKind : Int, address : Int, data : Long) //address out of range mean unknown
  def commit(hartId : Int, pc : Long): Unit
  def trap(hartId: Int, interrupt : Boolean, code : Int)
  def ioAccess(hartId: Int, access : TraceIo) : Unit
  def setInterrupt(hartId : Int, intId : Int, value : Boolean) : Unit
  def addRegion(hartId : Int, kind : Int, base : Long, size : Long) : Unit
  def addRegion(hartId : Int, kind : Int, mapping : AddressMapping) : Unit = {
    mapping match {
      case SizeMapping(base, size) => addRegion(hartId, kind, base.toLong, size.toLong)
      case OrMapping(conds) => conds.foreach(addRegion(hartId,kind, _))
    }
  }

  def loadExecute(hartId: Int, id : Long, addr : Long, len : Long, data : Long) : Unit
  def loadCommit(hartId: Int, id : Long) : Unit
  def loadFlush(hartId: Int) : Unit
  def storeExecute(hartId: Int, id : Long, addr : Long, len : Long, data : Long) : Unit
  def storeCommit(hartId: Int, id: Long): Unit
  def storeBroadcast(hartId: Int, id: Long): Unit
  def storeConditional(hartId : Int, failure: Boolean) : Unit

  def time(value : Long) : Unit

  def flush() : Unit
  def close() : Unit
}

class DummyBackend() extends TraceBackend{
  override def newCpuMemoryView(viewId: Int, readIds: Long, writeIds: Long) = {}
  override def newCpu(hartId: Int, isa: String, priv: String, physWidth: Int, pmpNum : Int, memoryViewId: Int) = {}
  override def loadElf(offset: Long, path: File) = {}
  override def loadBin(offset: Long, path: File) = {}
  override def loadBytes(offset: Long, bytes: Array[Byte]): Unit = {}
  override def setPc(hartId: Int, pc: Long) = {}
  override def writeRf(hardId: Int, rfKind: Int, address: Int, data: Long) = {}
  override def readRf(hardId: Int, rfKind: Int, address: Int, data: Long) = {}
  override def commit(hartId: Int, pc: Long) = {}
  override def trap(hartId: Int, interrupt: Boolean, code: Int) = {}
  override def ioAccess(hartId: Int, access: TraceIo) = {}
  override def setInterrupt(hartId: Int, intId: Int, value: Boolean) = {}
  override def addRegion(hartId: Int, kind : Int, base: Long, size: Long) = {}
  override def loadExecute(hartId: Int, id: Long, addr: Long, len: Long, data: Long) = {}
  override def loadCommit(hartId: Int, id: Long) = {}
  override def loadFlush(hartId: Int) = {}
  override def storeExecute(hartId: Int, id: Long, addr: Long, len: Long, data: Long) = {}
  override def storeCommit(hartId: Int, id: Long) = {}
  override def storeBroadcast(hartId: Int, id: Long) = {}
  override def storeConditional(hartId: Int, failure: Boolean) = {}
  override def time(value: Long) = {}
  override def flush() = {}
  override def close() = {}
}

class FileBackend(f : File) extends TraceBackend{
  FileUtils.forceMkdir(f.getParentFile)
  val bf = new BufferedWriter(new FileWriter(f))

  def log(line : String) = {
    bf.write(line)
//    println(line)
  }

  override def commit(hartId: Int, pc: Long) = {
    log(f"rv commit $hartId $pc%016x\n")
  }

  override def trap(hartId: Int, interrupt : Boolean, code : Int): Unit ={
    log(f"rv trap $hartId ${interrupt.toInt} $code\n")
  }

  override def writeRf(hartId: Int, rfKind: Int, address: Int, data: Long) = {
    log(f"rv rf w $hartId $rfKind $address $data%016x\n")
  }

  override def readRf(hartId: Int, rfKind: Int, address: Int, data: Long) = {
    log(f"rv rf r $hartId $rfKind $address $data%016x\n")
  }

  def ioAccess(hartId: Int, access : TraceIo) : Unit = {
    log(f"rv io $hartId ${access.serialized()}\n")
  }

  override def setInterrupt(hartId: Int, intId: Int, value: Boolean) = {
    log(f"rv int set $hartId $intId ${value.toInt}\n")
  }

  override def addRegion(hartId: Int, kind: Int, base: Long, size: Long) = {
    log(f"rv region add $hartId $kind $base%016x $size%016x\n")
  }

  override def loadElf(offset: Long, path: File) = {
    log(f"elf load  $offset%016x ${path.getAbsolutePath}\n")
  }

  override def loadBin(offset: Long, path: File) = {
    log(f"bin load $offset%016x ${path.getAbsolutePath} \n")
  }

  override def loadBytes(offset: Long, bytes: Array[Byte]) = {
    log(f"bytes load $offset%016x ${bytes.map(v =>f"$v%02x")mkString(" ")} \n")
  }

  override def setPc(hartId: Int, pc: Long) = {
    log(f"rv set pc $hartId $pc%016x\n")
  }

  override def newCpuMemoryView(memoryViewId : Int, readIds : Long, writeIds : Long) = {
    log(f"memview new $memoryViewId $readIds $writeIds\n")
  }

  override def newCpu(hartId: Int, isa : String, priv : String, physWidth : Int, pmpNum : Int, memoryViewId : Int) = {
    log(f"rv new $hartId $isa $priv $physWidth $pmpNum $memoryViewId\n")
  }

  override def loadExecute(hartId: Int, id : Long, addr : Long, len : Long, data : Long) : Unit = {
    log(f"rv load exe $hartId $id $len $addr%016x $data%016x\n")
  }
  override def loadCommit(hartId: Int, id : Long) : Unit = {
    log(f"rv load com $hartId $id\n")
  }
  override def loadFlush(hartId: Int) : Unit = {
    log(f"rv load flu $hartId\n")
  }
  override def storeExecute(hartId: Int, id : Long, addr : Long, len : Long, data : Long) : Unit = {
    log(f"rv store exe $hartId $id $len $addr%016x $data%016x\n")
  }
  override def storeCommit(hartId: Int, id : Long) : Unit = {
    log(f"rv store com $hartId $id\n")
  }  
  override def storeBroadcast(hartId: Int, id : Long) : Unit = {
    log(f"rv store bro $hartId $id\n")
  }
  override def storeConditional(hartId: Int, failure: Boolean) = {
    log(f"rv store sc $hartId ${failure.toInt}\n")
  }

  override def time(value: Long) = log(f"time $value\n")


  override def flush() = bf.flush()
  override def close() = bf.close()
}

class RvlsBackend(workspace : File = new File(".")) extends TraceBackend{
  import rvls.jni.Frontend
  FileUtils.forceMkdir(workspace)
  val handle = Frontend.newContext(workspace.getAbsolutePath)

  override def flush(): Unit = {}
  override def close(): Unit = {
    Frontend.deleteContext(handle)
  }

  def spikeDebug(): Unit = Frontend.spikeDebug(handle, true)
  def spikeLogCommit() : Unit = Frontend.spikeLogCommit(handle, true)
  def debug() : Unit = {
    spikeDebug()
    spikeLogCommit()
  }

  override def newCpuMemoryView(viewId: Int, readIds: Long, writeIds: Long): Unit = Frontend.newCpuMemoryView(handle, viewId, readIds, writeIds)
  override def newCpu(hartId: Int, isa: String, priv: String, physWidth: Int, pmpNum : Int, memoryViewId: Int): Unit = Frontend.newCpu(handle, hartId, isa, priv, physWidth, pmpNum, memoryViewId)
  override def loadElf(offset: Long, path: File): Unit = Frontend.loadElf(handle, offset, path.getAbsolutePath)
  override def loadBin(offset: Long, path: File): Unit = Frontend.loadBin(handle, offset, path.getAbsolutePath)
  override def loadBytes(offset: Long, bytes: Array[Byte]): Unit = Frontend.loadBytes(handle, offset, bytes)
  override def setPc(hartId: Int, pc: Long): Unit = Frontend.setPc(handle, hartId, pc)
  override def writeRf(hardId: Int, rfKind: Int, address: Int, data: Long): Unit = Frontend.writeRf(handle, hardId, rfKind, address, data)
  override def readRf(hardId: Int, rfKind: Int, address: Int, data: Long): Unit = Frontend.readRf(handle, hardId, rfKind, address, data)
  override def commit(hartId: Int, pc: Long): Unit = if(!Frontend.commit(handle, hartId, pc)) {
    throw new Exception(Frontend.getLastErrorMessage(handle))
  }
  override def trap(hartId: Int, interrupt: Boolean, code: Int): Unit = if(!Frontend.trap(handle, hartId, interrupt, code)) {
    throw new Exception(Frontend.getLastErrorMessage(handle))
  }
  override def ioAccess(hartId: Int, access: TraceIo): Unit = Frontend.ioAccess(handle, hartId, access.write, access.address, access.data, access.mask, access.size, access.error)
  override def setInterrupt(hartId: Int, intId: Int, value: Boolean): Unit = Frontend.setInterrupt(handle, hartId, intId, value)
  override def addRegion(hartId: Int, kind: Int, base: Long, size: Long): Unit = Frontend.addRegion(handle, hartId, kind, base, size)
  override def loadExecute(hartId: Int, id: Long, addr: Long, len: Long, data: Long): Unit = if(!Frontend.loadExecute(handle, hartId, id, addr, len, data)) throw new Exception(Frontend.getLastErrorMessage(handle))
  override def loadCommit(hartId: Int, id: Long): Unit = if(!Frontend.loadCommit(handle, hartId, id)) throw new Exception(Frontend.getLastErrorMessage(handle))
  override def loadFlush(hartId: Int): Unit = if(!Frontend.loadFlush(handle, hartId)) throw new Exception(Frontend.getLastErrorMessage(handle))
  override def storeExecute(hartId: Int, id: Long, addr: Long, len: Long, data: Long): Unit = if(!Frontend.storeExecute(handle, hartId, id, addr, len, data)) throw new Exception(Frontend.getLastErrorMessage(handle))
  override def storeCommit(hartId: Int, id: Long): Unit = if(!Frontend.storeCommit(handle, hartId, id)) throw new Exception(Frontend.getLastErrorMessage(handle))
  override def storeBroadcast(hartId: Int, id: Long): Unit = if(!Frontend.storeBroadcast(handle, hartId, id)) throw new Exception(Frontend.getLastErrorMessage(handle))
  override def storeConditional(hartId: Int, failure: Boolean): Unit = if(!Frontend.storeConditional(handle, hartId, failure)) throw new Exception(Frontend.getLastErrorMessage(handle))
  override def time(value: Long): Unit = Frontend.time(handle, value)

}
