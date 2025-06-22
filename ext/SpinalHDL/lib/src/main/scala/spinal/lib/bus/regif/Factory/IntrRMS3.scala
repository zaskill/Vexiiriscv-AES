package spinal.lib.bus.regif

import spinal.core._

/* RFMS4(RAW/MASK/STATUS) 3 Interrupt Register Group for 1st interrupt signal generate
  * 1. RAW:    raw status register, set when event, clear raw when write 1
  * 2. MASK:   mask register, 1: int off, 0: int open, default 1, int off
  * 3. STATUS: status register, status = raw && (!mask)
  * ```verilog demo
  * always @(posedge clk or negedge rstn)
  *   if(!rstn) begin
  *       raw_status_x <= 1'b0 ;
  *   end else if(signal_x) begin
  *       raw_status_x <= 1'b1;
  *   end else if(bus_addr == `xxx_RAW && bus_wdata[n]) begin //W1C
  *       raw_status_x <= 1'b0 ;
  *   end else if(bus_addr == `xxx_RAW && bus_wdata[n]) begin  //W1S
  *       raw_status_x <= 1'b1 ;
  *   end
  *
  * always@(posedge clk or negedge rstn)
  *   if(!rstn) begin
  *       mask <= 32'b0 ;
  *   end else if(bus_addr == `xxx_MASK) begin //RW
  *       mask <= bus_wdata ;
  *   end
  *
  *  assign status_x = raw_status_x && !mask[x] ;
  *
  * always @(*) begin
  *   case(addr) begin
  *      `xxx_RAW:    bus_rdata = {28'b0, raw_status_3....raw_status_0};
  *      `xxx_FORCE:  bus_rdata = {28'b0, raw_status_3....raw_status_0};
  *      `xxx_MASK :  bus_rdata = mask;
  *      `xxx_STATUS: bus_rdata = {28'b0, status_3....status_0};
  *      ....
  *   endcase
  *  end
  *
  * assign  xxx_int = status_3 || status_2 || status_1 || status_0 ;
  * ```
  */
class IntrRMS3(val name: String, offset: BigInt, doc: String, bi: BusIf, sec: Secure, grp: GrpTag) extends RegSliceGrp(offset, maxSize = 3*bi.bw, doc, sec, grp)(bi) with IntrBase {
  val RAW    = this.newRegAt(0, s"${doc} RMS3-Raw status Register\n set when event \n clear raw when write 1")(SymbolName(s"${name}_INT_RAW"))
  val MASK   = this.newReg(s"${doc} RMS3-Mask Register\n1: int off\n0: int open\n default 1, int off")(SymbolName(s"${name}_INT_MASK"))
  val STATUS = this.newReg(s"${doc} RMS3-status Register\n status = raw && (!mask)")(SymbolName(s"${name}_INT_STATUS"))

  def fieldAt[T <: BaseType](pos: Int, signal: T, maskRstVal: BigInt, doc: String)(implicit symbol: SymbolName): T  = {
    val nm = if (symbol.name.startsWith("<local")) signal.getPartialName() else symbol.name
    val raw    = RAW.fieldAt(pos, signal, AccessType.W1C, resetValue = 0, doc = s"${doc} raw, default 0")(SymbolName(s"${nm}_raw"))
    val mask   = MASK.fieldAt(pos, signal, AccessType.RW, resetValue = maskRstVal, doc = s"${doc} mask, default 1, int off")(SymbolName(s"${nm}_mask"))
    val status = STATUS.fieldAt(pos, signal, AccessType.RO, resetValue = 0, doc = s"${doc} stauts default 0")(SymbolName(s"${nm}_status"))
//    raw.setWhen(signal)
//    status := raw && (!mask)
    this.eventLogic(signal, raw, mask, status)
    statusbuf += status
    status
  }

  def field[T <: BaseType](signal: T,  maskRstVal: BigInt, doc: String)(implicit symbol: SymbolName): T = {
    val nm = if (symbol.name.startsWith("<local")) signal.getPartialName() else symbol.name
    val raw    = RAW.field(signal, AccessType.W1C, resetValue = 0, doc = s"${doc} raw, default 0")(SymbolName(s"${nm}_raw"))
    val mask   = MASK.field(signal, AccessType.RW, resetValue = maskRstVal, doc = s"${doc} mask, default 1, int off")(SymbolName(s"${nm}_mask"))
    val status = STATUS.field(signal, AccessType.RO, resetValue = 0, doc = s"${doc} stauts default 0")(SymbolName(s"${nm}_status"))
//    raw.setWhen(signal)
//    status := raw && (!mask)
    this.eventLogic(signal, raw, mask, status)
    statusbuf += status
    status
  }
}