#include <stdint.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdio.h>
#include <string.h>


#include "utils/bytes.hpp"  
#include "system/soc.hpp"   

int main() {
    soc::init();

    soc::uart.write(utils::to_bytes("Hello Vexii!\n"));

    uint8_t led_state = 1;

    while (1) {
        
        soc::uart.write(utils::to_bytes("Led blink!\n"));

       
        volatile uint32_t* leds = (uint32_t*)(0x10003000 + 0x00); 
        *leds = led_state;
        led_state = (led_state << 1) | (led_state >> 7); 

     
        soc::delay(std::chrono::milliseconds(500));
    }

    return 0;
}