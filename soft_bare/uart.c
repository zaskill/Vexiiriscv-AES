#include <stdint.h>

#define UART_BASE     0x10001000
#define UART_TX       (*(volatile uint32_t*)UART_BASE)

#define LED_BASE      0x10003000
#define LEDS          (*(volatile uint32_t*)LED_BASE)

void uart_putchar(char c) {
    UART_TX = c;
}

void uart_puts(const char* s) {
    while (*s) uart_putchar(*s++);
}

void delay() {
    for (volatile int i = 0; i < 100000; i++);
}

void _start() {
    LEDS = 0xFF;  // Turn on all 8 LEDs

    while (1) {
        uart_puts("Hello FPGA!\n");
        delay();
    }
}
