package spinal.lib.eda.xilinx

import org.apache.commons.io.FileUtils
import spinal.core._
import spinal.lib.DoCmd.doCmd
import spinal.lib.eda.bench.{Report, Rtl}

import java.io.File
import java.nio.file.Paths
import scala.io.Source

object VivadoFlow {

  /**
   * Use vivado to run eda flow
   *
   * @param vivadoPath      The path to vivado (e.g. /opt/Xilinx/Vivado/2019.2/bin)
   * @param workspacePath   The temporary workspace path (e.g. /tmp/test)
   * @param toplevelPath    The path to top level hdl file
   * @param family          Xilinx device family (Artix 7, Kintex 7, Kintex UltraScale, Kintex UltraScale+ or Virtex UltraScale+)
   * @param device          Xilinx device part
   * @param frequencyTarget Target clock frequency
   * @param processorCount  Number of processor count used
   * @return Report
   */
  def apply(vivadoPath: String, workspacePath: String, rtl: Rtl, family: String, device: String, frequencyTarget: HertzNumber = null, processorCount: Int = 1): Report = {
    val targetPeriod = (if (frequencyTarget != null) frequencyTarget else 500 MHz).toTime

    val workspacePathFile = new File(workspacePath)
    FileUtils.deleteDirectory(workspacePathFile)
    workspacePathFile.mkdir()
    for (file <- rtl.getRtlPaths()) {
      FileUtils.copyFileToDirectory(new File(file), workspacePathFile)
    }

    val isVhdl = (file: String) => file.endsWith(".vhd") || file.endsWith(".vhdl")
    val readRtl = rtl.getRtlPaths().map(file => s"""read_${if(isVhdl(file)) "vhdl" else "verilog"} ${Paths.get(file).getFileName()}""").mkString("\n")

    // generate tcl script
    val tcl = new java.io.FileWriter(Paths.get(workspacePath, "doit.tcl").toFile)
    tcl.write(
s"""
create_project -force project_bft_batch ./project_bft_batch -part $device

add_files {${rtl.getRtlPaths().mkString(" ")}}
add_files -fileset constrs_1 ./doit.xdc

import_files -force

set_property -name {STEPS.SYNTH_DESIGN.ARGS.MORE OPTIONS} -value {-mode out_of_context} -objects [get_runs synth_1]
launch_runs synth_1
wait_on_run synth_1
open_run synth_1 -name netlist_1

report_timing_summary -delay_type max -report_unconstrained -check_timing_verbose -max_paths 10 -input_pins -file syn_timing.rpt
report_power -file syn_power.rpt

launch_runs impl_1
wait_on_run impl_1

open_run impl_1
report_utilization
report_timing_summary -warn_on_violation
report_pulse_width -warn_on_violation -all_violators
report_design_analysis -logic_level_distribution
"""
    )
    tcl.flush();
    tcl.close();

    // generate xdc constraint
    val xdc = new java.io.FileWriter(Paths.get(workspacePath, "doit.xdc").toFile)
    xdc.write(s"""create_clock -period ${(targetPeriod * 1e9) toBigDecimal} [get_ports clk]""")
    xdc.flush();
    xdc.close();

    // run vivado
    doCmd(s"$vivadoPath/vivado -nojournal -log doit.log -mode batch -source doit.tcl", workspacePath)
    val log = Source.fromFile(Paths.get(workspacePath, "doit.log").toFile)
    val report = log.getLines().mkString

    new Report {
      // Non-logic elements such as PLL or BRAMs may have stricter timing then logic, check for their pulse slack
      // getFMax() will then take this into account later. Uses "report_pulse_width -warn_on_violation -all_violators"
      //
      // Pulse Width Checks
      // <...>
      // Check Type        Corner  Lib Pin             Reference Pin  Required(ns)  Actual(ns)  Slack(ns)  Location      Pin
      // Min Period        n/a     RAMB18E2/CLKARDCLK  n/a            1.569         2.857       1.288      RAMB18_X9Y68  fifo128x32_inst/f/logic_ram_reg/CLKARDCLK
      // Low Pulse Width   Slow    RAMB18E2/CLKARDCLK  n/a            0.542         1.429       0.887      RAMB18_X9Y68  fifo128x32_inst/f/logic_ram_reg/CLKARDCLK
      // High Pulse Width  Slow    RAMB18E2/CLKARDCLK  n/a            0.542         1.429       0.887      RAMB18_X9Y68  fifo128x32_inst/f/logic_ram_reg/CLKARDCLK

      def getPulseSlack(): Double /*nanoseconds*/ = {
        // if not found, assume only logic is involved and do not take pulse slack into account
        var lowest_pulse_slack : Double = 100000.0
        val pulse_strings = "(Min Period|Low Pulse Width|High Pulse Width)(?:\\s+\\S+){5}(?:\\s+)-?(\\d+.?\\d+)+".r.findAllIn(report).toList
        // iterate through pulse slack lines
        for (pulse_string <- pulse_strings) {
          // iterate through number columns
          val pulse_slack_numbers = "\\s-?([0-9]+\\.?[0-9]+)+".r.findAllIn(pulse_string).toList
          // third number column is pulse slack
          if (pulse_slack_numbers.length >= 3) {
            if (pulse_slack_numbers.apply(2).toDouble < lowest_pulse_slack) {
              lowest_pulse_slack = pulse_slack_numbers.apply(2).toDouble
            }
          }
        }       
        return lowest_pulse_slack 
      }

      private def findFirst2StageInReport(regex1st: String, regex2nd: String): String = {
        try{
          regex1st.r.findFirstIn(regex2nd.r.findFirstIn(report).get).get
        } catch {
          case e : Exception => "???"
        }
      }

      override def getFMax(): Double = {
        val intFind = "-?(\\d+\\.?)+"
        var slack = try {
          (family match {
            case "Artix 7" | "Kintex 7" | "Kintex UltraScale" | "Kintex UltraScale+" | "Virtex UltraScale+" =>
              findFirst2StageInReport(intFind, "-?(\\d+.?)+ns  \\(required time - arrival time\\)")
          }).toDouble
        } catch {
          case e : Exception => -100000.0
        }
        val pulse_slack = getPulseSlack()
        if (pulse_slack < slack) {
          slack = pulse_slack
        }
        return 1.0 / (targetPeriod.toDouble - slack * 1e-9)
      }
      
      override def getArea(): String =  {
        // 0, 30, 0.5, 15,5
        val intFind = "(\\d+,?\\.?\\d*)"
        val leArea = try {
          family match {
            case "Artix 7" | "Kintex 7" =>
              findFirst2StageInReport(intFind, "Slice LUTs[ ]*\\|[ ]*(\\d+,?)+") + " LUT " +
              findFirst2StageInReport(intFind, "Slice Registers[ ]*\\|[ ]*(\\d+,?)+") + " FF "
            // Assume the the resources table is the only one with 5 columns (this is the case in Vivado 2021.2)
            // (Not very version-proof, we should actually first look at the right table header first...)
            case "Kintex UltraScale" | "Kintex UltraScale+" | "Virtex UltraScale+" =>
              findFirst2StageInReport(intFind, "\\| CLB LUTs[ ]*\\|([ ]*\\S+\\s+\\|){5}") + " LUT " +
              findFirst2StageInReport(intFind, "\\| CLB Registers[ ]*\\|([ ]*\\S+\\s+\\|){5}") + " FF " +
              findFirst2StageInReport(intFind, "\\| Block RAM Tile[ ]*\\|([ ]*\\S+\\s+\\|){5}") + " BRAM " + 
              findFirst2StageInReport(intFind, "\\| URAM[ ]*\\|([ ]*\\S+\\s+\\|){5}") + " URAM "
          }
        } catch {
          case e : Exception => "???"
        }
        return leArea
      }
    }
  }
}
