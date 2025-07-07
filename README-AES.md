# Integrate Opentitan AES module to Vexiiriscv MicroSoc

This project aims to integrate the AES hardware IP from the OpenTitan project into the RISC-V-based MicroSoC from the VexRiscv project.

The following steps should be followed to generate a bitstream for the project.

# Clone VEXIIRISCV-AES Repository

The first step is to clone the VexRiscv repository.
Since both the hardware (e.g. peripheral integration) and software are actively maintained and updated, it's important to regularly pull the latest changes 

https://github.com/zaskill/Vexiiriscv-AES.git (CEA git instead ????)

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

-Pulse-Based Test Done
The test_done_o signal is now a one-cycle pulse, which is asserted when the AES operation finishes and then automatically cleared, ensuring synchronization with the host software.

--The aes_wrap.sv file must be modified from its original version, as the I/O interface differs in the newer implementation !

# Builduing Software

To build software, this command can be used to compile the cpp files; all the Makefiles are already configured for the desired compilation.

cd Vexiifirmware
