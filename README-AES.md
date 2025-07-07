# Integrate Opentitan AES module to Vexiiriscv MicroSoc

This project aims to integrate the AES hardware IP from the OpenTitan project into the RISC-V-based MicroSoC from the VexRiscv project.

The following steps should be followed to generate a bitstream for the project.

# Opentitan AES IP extraction

Fusesoc is used to extract the necessary RTL files for the AES IP.
Doing this manually is quite challenging, as the AES IP has many dependencies — totaling around 230 files.
Without Fusesoc, correctly resolving these dependencies and file order would be error-prone and time-consuming.

