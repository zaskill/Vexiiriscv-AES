#This is a base xdc file for the project this file would be automatically modified, if partial reconfiguration
#is enabled, please refer to the FPGA_partial_reconfiguration_using_ICAP.pdf file located in the document folder

## Clock
set_property -dict {PACKAGE_PIN E3 IOSTANDARD LVCMOS33} [get_ports CLK100MHZ]
create_clock -period 10.000 -name sys_clk_pin -waveform {0.000 5.000} -add [get_ports CLK100MHZ]

## Reset button (use SW0)
set_property -dict {PACKAGE_PIN A8 IOSTANDARD LVCMOS33} [get_ports soc_reset_button]
set_property -dict {PACKAGE_PIN C11 IOSTANDARD LVCMOS33} [get_ports led_reset]
## UART
set_property -dict {PACKAGE_PIN A9 IOSTANDARD LVCMOS33} [get_ports uart_rxd]
set_property -dict {PACKAGE_PIN D10 IOSTANDARD LVCMOS33} [get_ports uart_txd]

## LEDs (8 bits)
set_property -dict {PACKAGE_PIN H5 IOSTANDARD LVCMOS33} [get_ports {leds[0]}]
set_property -dict {PACKAGE_PIN J5 IOSTANDARD LVCMOS33} [get_ports {leds[1]}]
set_property -dict {PACKAGE_PIN T9 IOSTANDARD LVCMOS33} [get_ports {leds[2]}]
set_property -dict {PACKAGE_PIN T10 IOSTANDARD LVCMOS33} [get_ports {leds[3]}]
set_property -dict {PACKAGE_PIN H6 IOSTANDARD LVCMOS33} [get_ports {leds[4]}]
set_property -dict {PACKAGE_PIN J2 IOSTANDARD LVCMOS33} [get_ports {leds[5]}]
set_property -dict {PACKAGE_PIN J4 IOSTANDARD LVCMOS33} [get_ports {leds[6]}]
set_property -dict {PACKAGE_PIN J4 IOSTANDARD LVCMOS33} [get_ports {leds[6]}]
#set_property -dict {PACKAGE_PIN F6 IOSTANDARD LVCMOS33}  [get_ports {leds[7]}]

## Buttons (use SW1-SW4, you can expand later)
#set_property -dict { PACKAGE_PIN C11 IOSTANDARD LVCMOS33 } [get_ports { buttons[0] }]
#set_property -dict { PACKAGE_PIN C10 IOSTANDARD LVCMOS33 } [get_ports { buttons[1] }]
#set_property -dict { PACKAGE_PIN A10 IOSTANDARD LVCMOS33 } [get_ports { buttons[2] }]
#set_property -dict { PACKAGE_PIN D9  IOSTANDARD LVCMOS33 } [get_ports { buttons[3] }]
# Expand as needed: buttons[4..7] are not mapped yet

set_property CONFIG_MODE S_SELECTMAP32 [current_design]



#set_property RESET_AFTER_RECONFIG true [get_pblocks <pblock_1>]

#set_property C_CLK_INPUT_FREQ_HZ 300000000 [get_debug_cores dbg_hub]
#set_property C_ENABLE_CLK_DIVIDER false [get_debug_cores dbg_hub]
#set_property C_USER_SCAN_CHAIN 1 [get_debug_cores dbg_hub]
#connect_debug_port dbg_hub/clk [get_nets CLK100MHZ_IBUF_BUFG]
