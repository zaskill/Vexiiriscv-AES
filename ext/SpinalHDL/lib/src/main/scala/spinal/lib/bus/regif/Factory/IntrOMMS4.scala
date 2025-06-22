package spinal.lib.bus.regif

import spinal.core._
import spinal.core.fiber.Handle.initImplicit

/* OMMS4(ORIGIN/MASKS/MASKC/STATUS) 4 Interrupt Register Group used for 2nd interrupt signal merge
 * 1. ORIGIN: Origin level-signal Interrupt
 * 3. MASKS:  mask set register, 1: int off, 0: int open, default 1, int off
 * 4. MASKC:  mask clear register, 1: int off, 0: int open, default 1, int off
 * 5. STATUS: status register, status = raw && (!mask)
 * ```
 * always@(posedge clk or negedge rstn)
 *   if(!rstn) begin
 *       mask_x <= 1'b0 ;
 *   end else if(bus_addr == `xxx_MASKS && bus_wdata[n]) begin //W1S
 *       mask_x <= 1'b1 ;
 *   end else if(bus_addr == `xxx_MASKC && bus_wdata[n]) begin //W1C
 *       mask_x <= 1'b0 ;
 *   end
 *
 * assign status_x = signal_x && !mask_x ;
 *
 * always @(*) begin
 *    case(addr) begin
 *      `xxx_RAW:    bus_rdata = {28'b0, raw_status_3....raw_status_0};
 *      `xxx_FORCE:  bus_rdata = {28'b0, raw_status_3....raw_status_0};
 *      `xxx_MASKS:  bus_rdata = {28'b0, mask_3....mask_0};
 *      `xxx_MASKC:  bus_rdata = {28'b0, mask_3....mask_0};
 *      `xxx_STATUS: bus_rdata = {28'b0, status_3....status_0};
 *      ....
 *    endcase
 *  end
 *
 * assign  xxx_int = status_3 || status_2 || status_1 || status_0 ;
 * ```
 */
class IntrOMMS4(val name: String, offset: BigInt, doc: String, bi: BusIf, sec: Secure, grp: GrpTag) extends RegSliceGrp(offset, maxSize = 4*bi.bw, doc, sec, grp)(bi) with IntrBase {
  val ORIGIN = this.newRegAt(0, s"${doc} OMMS4-Raw status Register\n set when event \n clear raw when write 1")(SymbolName(s"${name}_INT_RAW"))
  val MASKS  = this.newReg(s"${doc} OMMS4-Mask W1S Register\n1: int off\n0: int open\n default 1, int off")(SymbolName(s"${name}_INT_MASKS"))
  val MASKC  = this.newReg(s"${doc} OMMS4-Mask W1C Register\n1: int off\n0: int open\n default 1, int off")(SymbolName(s"${name}_INT_MASKC"))
  val STATUS = this.newReg(s"${doc} OMMS4-status Register\n status = raw && (!mask)")(SymbolName(s"${name}_INT_STATUS"))

  def fieldAt[T <: BaseType](pos: Int, signal: T, maskRstVal: BigInt, doc: String)(implicit symbol: SymbolName): T = {
    val nm = if (symbol.name.startsWith("<local")) signal.getPartialName() else symbol.name
    val origin = ORIGIN.fieldAt(pos, signal, AccessType.RO, resetValue = 0, doc = s"${doc} raw, default 0")(SymbolName(s"${nm}_raw"))
    val mask   = MASKS.fieldAt(pos, signal, AccessType.W1S, resetValue = maskRstVal, doc = s"${doc} mask, write 1 set")(SymbolName(s"${nm}_mask"))
                 MASKC.parasiteFieldAt(pos, mask, AccessType.W1C, resetValue = maskRstVal, doc = s"${doc} mask, write 1 clr")
    val status = STATUS.fieldAt(pos, signal, AccessType.RO, resetValue = 0, doc = s"${doc} stauts default 0")(SymbolName(s"${nm}_status"))
    origin := signal
    this.levelLogic(signal, mask, status)
    statusbuf += status
    status
  }

  def field[T <: BaseType](signal: T, maskRstVal: BigInt, doc: String)(implicit symbol: SymbolName): T = {
    val nm = if (symbol.name.startsWith("<local")) signal.getPartialName() else symbol.name
    val origin = ORIGIN.field(signal, AccessType.RO, resetValue = 0, doc = s"${doc} raw, default 0")(SymbolName(s"${nm}_raw"))
    val mask = MASKS.field(signal, AccessType.W1S, resetValue = maskRstVal, doc = s"${doc} mask, write 1 set")(SymbolName(s"${nm}_mask"))
               MASKC.parasiteField(mask, AccessType.W1C, resetValue = maskRstVal, doc = s"${doc} mask, write 1 clr")
    val status = STATUS.field(signal, AccessType.RO, resetValue = 0, doc = s"${doc} stauts default 0")(SymbolName(s"${nm}_status"))
    origin := signal
    this.levelLogic(signal, mask, status)
    statusbuf += status
    status
  }
}
