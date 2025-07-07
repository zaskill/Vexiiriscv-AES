# VexiiRiscv

VexiiRiscv (Vex2Risc5) is the successor of VexRiscv. Work in progress, here are its currently implemented features :

- RV32/64 I[M][A][F][D][C][S][U][B]
- Up to 5.24 coremark/Mhz 2.50 dhystone/Mhz
- In-order execution
- early [late-alu]
- single/dual issue (can be asymmetric)
- BTB, GShare, RAS branch prediction
- cacheless fetch/load/store
- Optional I$, D$
- Optional SV32/SV39 MMU
- Can run linux / buildroot / Debian
- Pipeline visualisation in simulation via Konata
- Lock step simulation via RVLS and Spike
- AXI4, Wishbone, Tilelink memory busses (RVA is not available in some configs, see the RTD doc SoC main page)
- ... and many other things

Here is a demonstration of a quad core VexiiRiscv running debian on FPGA : https://youtu.be/dR_jqS13D2c?t=112

Overall the goal is to have a design which can stretch (through configuration) from Cortex M0 up to a Cortex A53 and potentialy beyond.

Here is the online documentation : 

- https://spinalhdl.github.io/VexiiRiscv-RTD/master/VexiiRiscv/Introduction/#
- https://spinalhdl.github.io/VexiiRiscv-RTD/master/VexiiRiscv/HowToUse/index.html

Here is the VexiiRiscv's scala doc (auto-generated from the source code) :

- https://spinalhdl.github.io/VexiiRiscv/doc/vexiiriscv/index.html

A roadmap is available here : 

- https://github.com/SpinalHDL/VexiiRiscv/issues/1

# TL;DR Getting started

The quickest way for getting started is to pull the Docker image with all the dependencies installed

Please refer to the self contained tutorial for a comprehensive step by step instruction manual with
screenshots: https://spinalhdl.github.io/VexiiRiscv-RTD/master/VexiiRiscv/Tutorial/index.html

After running the generation you'll find a file named "VexiiRiscv.v" in the root
of the repository folder, which you can drag into your Quartus or whatever.

We decided to not start covering FPGA boards because there's just too many, so it's up to you
to define your pin configuration for your specific FPGA board

If you want to know what else you can do with sbt, please refer to the complete documentation.

# Rebuild the Docker container

In case you wanna rebuild leviathan's Docker container you can run

    docker build . -f docker/Dockerfile -t vexiiriscv --progress=plain


rm -rf build && cmake -S . -B build -DSOC=microsoc/default -DDEVICE=microsoc_sim &&  make -C build example-aes

sbt "runMain vexiiriscv.soc.micro.MicroSocGen \
  --ram-bytes 16384 \
  --ram-elf /nobackup/Vexiiriscv-AES/VexiiFirmware/build/app/aes/example-aes.elf \
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


aes_wrap aes (
    .clk_i         (io_clk               ), //i
    .rst_ni        (aes_rst_ni           ), //i
    .aes_input     (128'h6bc1bee22e409f96e93d7e117393172a), //i
    .aes_key       (256'h00000000000000008e73b0f7da0e6452c810f32b809079e562f8ead2522c6b7b), //i
    .aes_output    (aes_aes_output[127:0]), //o
    .alert_recov_o (aes_alert_recov_o    ), //o
    .alert_fatal_o (aes_alert_fatal_o    ), //o
    .test_done_o   (aes_test_done_o      )  //o
  );

   ila_1 ila_1(
    .clk(io_clk),
    .probe0(aes_aes_output),
    .probe1(aes_test_done_o),
    .probe2(aes_alert_fatal_o),
    .probe3(aes_alert_recov_o),
    .probe4(aes_rst_ni),
    .probe5(inputData),
    .probe6(keyData),
    .probe7(decryptModeReg),
    .probe8(aesIv),
    .probe9(start)

  );


  6bc1bee22e409f96e93d7e117393172a
  8e73b0f7da0e6452c810f32b809079e562f8ead2522c6b7b
  ec46c3488dbe811a0dfbcb30440c243b




f41fddab55e3987067a9112a15c93ae8
E83AC9152A11A9677098E355ABDD1FF4

AES_CTR

