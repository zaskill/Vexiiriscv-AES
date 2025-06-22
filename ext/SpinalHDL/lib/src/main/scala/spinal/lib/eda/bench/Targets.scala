package spinal.lib.eda.bench

import spinal.core._
import spinal.lib.eda.altera.QuartusFlow
import spinal.lib.eda.efinix.EfinixFlow
import spinal.lib.eda.xilinx.VivadoFlow
import spinal.lib.eda.microsemi.LiberoFlow

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.collection.Seq

trait Target {
  def synthesise(rtl: Rtl, workspace: String): Report
  def getFamilyName() : String
}

object AlteraStdTargets {
  def apply(quartusCycloneIIPath : String = sys.env.getOrElse("QUARTUS_CYCLONE_II_BIN", null),
            quartusCycloneIVPath : String = sys.env.getOrElse("QUARTUS_CYCLONE_IV_BIN", null),
            quartusCycloneVPath: String = sys.env.getOrElse("QUARTUS_CYCLONE_V_BIN", null),
            quartusAgilexVPath: String = sys.env.getOrElse("QUARTUS_AGILEX_V_BIN", null)): Seq[Target] = {
    val targets = ArrayBuffer[Target]()

    if(quartusCycloneVPath != null) {
      targets += new Target {
        override def getFamilyName(): String = "Cyclone V"
        override def synthesise(rtl: Rtl, workspace: String): Report = {
          QuartusFlow(
            quartusPath = quartusCycloneVPath,
            workspacePath = workspace,
            toplevelPath = rtl.getRtlPath(),
            family = getFamilyName(),
            device = "5CSEMA5F31C6"
//            device = "5CEFA7F31C6"
          )
        }
      }
    }

    if(quartusCycloneIVPath != null) {
      targets += new Target {
        override def getFamilyName(): String = "Cyclone IV"
        override def synthesise(rtl: Rtl, workspace: String):  Report = {
          QuartusFlow(
            quartusPath = quartusCycloneIVPath,
            workspacePath = workspace,
            toplevelPath = rtl.getRtlPath(),
            family = getFamilyName(),
            device = "EP4CE30F29C6"
          )
        }
      }
    }
    if(quartusCycloneIIPath != null) {
      targets += new Target {
        override def getFamilyName(): String = "Cyclone II"
        override def synthesise(rtl: Rtl, workspace: String): Report = {
          QuartusFlow(
            quartusPath = quartusCycloneIIPath,
            workspacePath = workspace,
            toplevelPath = rtl.getRtlPath(),
            family = getFamilyName(),
            device = "EP2C35F672C6"
          )
        }
      }
    }

    if (quartusAgilexVPath != null) {
      targets += new Target {
        override def getFamilyName(): String = "Agilex V"

        override def synthesise(rtl: Rtl, workspace: String): Report = {
          QuartusFlow(
            quartusPath = quartusAgilexVPath,
            workspacePath = workspace,
            toplevelPath = rtl.getRtlPath(),
            family = getFamilyName(),
            device = "A5ED065BB32AI4S" //A5ED065BB32AE6SR0
          )
        }
      }
    }

    targets
  }
}


object XilinxStdTargets {
  def apply(vivadoArtix7Path : String = sys.env.getOrElse("VIVADO_ARTIX_7_BIN", null), withArea : Boolean = true, withFMax : Boolean = true): Seq[Target] = {
    val targets = ArrayBuffer[Target]()

    if(vivadoArtix7Path != null) {
      if(withArea) targets += new Target {
        override def getFamilyName(): String = "Artix 7"
        override def synthesise(rtl: Rtl, workspace: String): Report = {
          VivadoFlow(
            frequencyTarget = 50 MHz,
            vivadoPath=vivadoArtix7Path,
            workspacePath=workspace + "_area",
            rtl=rtl,
            family=getFamilyName(),
            device="xc7a200tffv1156-3"
            //            device="xc7k70t-fbg676-3"
          )
        }
      }
      if(withFMax) targets += new Target {
        override def getFamilyName(): String = "Artix 7"
        override def synthesise(rtl: Rtl, workspace: String): Report = {
          VivadoFlow(
            frequencyTarget = 400 MHz,
            vivadoPath=vivadoArtix7Path,
            workspacePath=workspace + "_fmax",
            rtl=rtl,
            family=getFamilyName(),
            device="xc7a200tffv1156-3"
            //            device="xc7k70t-fbg676-3"  xc7a75tfgg676-3
          )
        }
      }
    }

    targets
  }
}



object EfinixStdTargets {
  def apply(efinixPath : String = sys.env.getOrElse("EFINIX_BIN", null), withArea : Boolean = true, withFMax : Boolean = true): Seq[Target] = {
    val targets = ArrayBuffer[Target]()

    if(efinixPath != null) {
      if(withArea) targets += new Target {
        override def getFamilyName(): String = "Trion"
        override def synthesise(rtl: Rtl, workspace: String): Report = {
          EfinixFlow(
            frequencyTarget = 10 MHz,
            efinixPath = efinixPath,
            workspacePath = workspace + "_area",
            rtl = rtl,
            family = getFamilyName(),
            device = "T120F576",
            timing = "C4"
          )
        }
      }
      if(withFMax) targets += new Target {
        override def getFamilyName(): String = "Trion"
        override def synthesise(rtl: Rtl, workspace: String): Report = {
          EfinixFlow(
            frequencyTarget = 200 MHz,
            efinixPath = efinixPath,
            workspacePath = workspace + "_fmax",
            rtl = rtl,
            family = getFamilyName(),
            device = "T120F576",
            timing = "C4"
          )
        }
      }
      if(withArea) targets += new Target {
        override def getFamilyName(): String = "Titanium"
        override def synthesise(rtl: Rtl, workspace: String): Report = {
          EfinixFlow(
            frequencyTarget = 40 MHz,
            efinixPath=efinixPath,
            workspacePath=workspace + "_area",
            rtl=rtl,
            family=getFamilyName(),
            device="Ti180M484",
            timing = "C4"
          )
        }
      }
     if(withFMax) targets += new Target {
        override def getFamilyName(): String = "Titanium"
        override def synthesise(rtl: Rtl, workspace: String): Report = {
          EfinixFlow(
            frequencyTarget = 500 MHz,
            efinixPath=efinixPath,
            workspacePath=workspace + "_fmax",
            rtl=rtl,
            family=getFamilyName(),
            device="Ti180M484",
            timing = "C4"
          )
        }
      }
    }

    targets
  }
}


object MicrosemiStdTargets {
  def apply(liberoProasic3Path : String = null): Seq[Target] = {
    val targets = ArrayBuffer[Target]()

    if(liberoProasic3Path != null) {
      targets += new Target {
        override def getFamilyName(): String = "ProASIC3E"
        override def synthesise(rtl: Rtl, workspace: String): Report = {
          LiberoFlow(
            liberoPath=liberoProasic3Path,
            workspacePath=workspace,
            toplevelPath=rtl.getRtlPath(),
            family=getFamilyName(),
            device="a3pe3000l-fg484-2"
          )
        }
      }
    }

    targets
  }
}
