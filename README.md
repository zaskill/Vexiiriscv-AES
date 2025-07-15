# Integrate Opentitan AES module to Vexiiriscv MicroSoc

This project aims to integrate the AES hardware IP from the OpenTitan project into the RISC-V-based MicroSoC from the VexRiscv project.

The following steps should be followed to generate a bitstream for the project.

The project can be created by simply running the TCL script located at /Vexiiriscv-AES/Vexii-AES-Vivado-Project/default-vivado/MicroSoc-AES.tcl. This script includes all the modifications described in this README and in the AES_module_integration_with_MicroSoc.pdf document, and generates a Vivado project ready for partial reconfiguration. For better understanding and future modifications, the detailed steps are also provided below.

# Clone VEXIIRISCV-AES Repository

The first step is to clone the VexRiscv repository.
Since both the hardware (e.g. peripheral integration) and software are actively maintained and updated, it's important to regularly pull the latest changes 

https://github.com/zaskill/Vexiiriscv-AES.git 

# Clone Opentitan Repository 

It is necessary to clone the opentitan repository to get the necessary files for the AES HW IP.
It is recommended to clone this repository outside the VEXIIRISCV-AES folder.

https://github.com/lowRISC/opentitan.git

# Opentitan AES IP extraction

Fusesoc is used to extract the necessary RTL files for the AES IP.
Doing this manually is quite challenging, as the AES IP has many dependencies — totaling around 230 files.
Without Fusesoc, correctly resolving these dependencies and file order would be error-prone and time-consuming.

To use the AES IP from OpenTitan, start by cloning the OpenTitan repository from https://github.com/lowRISC/opentitan.git. 

If fusesoc is not already installed, it can be installed using pip install fusesoc. Once installed, you can explore available AES-related IPs by running fusesoc list-cores | grep aes. 

The IP core relevant for integration is aes_wrap, which serves as a top-level wrapper around the AES module. It includes a finite state machine (FSM) to handle TileLink communication. The I/O interface of this wrapper exposes only the key and the encrypted/decrypted data, making it suitable for direct integration into SoC environments.

# Aes_wrap.sv file

The fsm of the aes file has been changed to be compatible with moree functiannalities than the ones provided by the open titan teams 

-Start Signal Support:
The modified version introduces an external start signal (with pulse detection logic) to trigger encryption/decryption, making it more suitable for software-controlled execution from a RISC-V SoC.

-CTR Mode Support & IV Input:
The modified wrapper supports AES in CTR mode and introduces an aes_iv input port. The FSM was updated to handle IV writing via TL-UL transactions.

-Decryption Capability:
Added aes_decrypt_i input to allow dynamic switching between encryption and decryption modes, supporting more flexible use cases.

-Pulse-Based Data_valid
The data_valid signal is asserted when the AES operation finishes and then automatically cleared, ensuring synchronization with the host software.

--The aes_wrap.sv file must be modified from its original version, as the I/O interface differs in the newer implementation !

# Builduing Software

To build software, this command can be used to compile the cpp files, all the Makefiles are already configured for the desired compilation.

cd Vexiifirmware
rm -rf build && cmake -S . -B build -DSOC=microsoc/default -DDEVICE=microsoc_sim &&  make -C build example-aes

# Building Hardware (Microsoc SOC)

The Microsoc Soc is based on a Vexiiriscv RISCV processor with some basic peripherals (UART, RAM ...). 
This SoC was modified in this project, and AES module from the Opentitan project was added as a peripheral, the vexiiriscv cpu can control the IP.

The github of the original project : https://github.com/SpinalHDL/VexiiRiscv.git

To build hardware, this command can be used to compile the scala files. 

sbt "runMain vexiiriscv.soc.micro.MicroSocGen \
  --ram-bytes 16384 \
  --ram-elf "PATH_TO_REPO/Vexiiriscv-AES/VexiiFirmware/build/app/aes/example-aes.elf" \
  --demo-peripheral leds=8,buttons=4 \
  --xlen 32 \
  --with-mul \
  --with-div \
  --with-rvm \
  --system-frequency 100000000 \
  --jtag-tap true \
  --jtag-instruction true \
  --reset-vector 0x80000000 \
  --with-supervisor"

Then the aes_wrap project generated with fusesoc should be opened and the following files located in Rtl_files should be added :
-aes_wrap.sv (should replace the original)
-prim_alert_sender.sv (should replace the original)
-top_microsoc.sv
-fifo128to32.v

After running the command above two files are generated and should be added to the project :

-MicroSoc.v
-soc.h

# Partial reconfiguration

To enable partial reconfiguration and bitstream generation, a document FPGA_partial_configuration_using_ICAP.pdf located in document folder can be helpful to enable reconfiguration and generate a partial bitstream for the desired design.


