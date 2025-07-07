#include <system/soc.hpp>
#include <cstring>
#include <cstdlib>

int main() {
    soc::init();

    constexpr std::uintptr_t AES_INPUT_BASE   = 0x10005004;  
    constexpr std::uintptr_t AES_KEY_BASE     = 0x10005014;  
    constexpr std::uintptr_t AES_OUTPUT_BASE  = 0x10005038; 
    constexpr std::uintptr_t AES_DONE_REG     = 0x10005048; 
    constexpr std::uintptr_t AES_MODE_REG     = 0x1000504C; 
    constexpr std::uintptr_t AES_IV_REG       = 0x10005050;
    constexpr std::uintptr_t AES_START_REG       = 0x10005064;  

    volatile uint32_t* aes_input  = reinterpret_cast<volatile uint32_t*>(AES_INPUT_BASE);
    volatile uint32_t* aes_key    = reinterpret_cast<volatile uint32_t*>(AES_KEY_BASE);
    volatile uint32_t* aes_output = reinterpret_cast<volatile uint32_t*>(AES_OUTPUT_BASE);
    volatile uint32_t* aes_done   = reinterpret_cast<volatile uint32_t*>(AES_DONE_REG);
    volatile uint32_t* aes_mode   = reinterpret_cast<volatile uint32_t*>(AES_MODE_REG);
    volatile uint32_t* aes_iv     = reinterpret_cast<volatile uint32_t*>(AES_IV_REG);
    volatile uint32_t* aes_start     = reinterpret_cast<volatile uint32_t*>(AES_START_REG);

    auto print_hex = [](const char* label, const uint32_t* data, int len) {
        soc::uart.write(std::span<const std::byte>(
            reinterpret_cast<const std::byte*>(label), std::strlen(label)));
        std::byte b[1];
        for (int i = 0; i < len; ++i) {
            uint32_t w = data[i];
            for (int j = 7; j >= 0; --j) {
                uint8_t nibble = (w >> (j * 4)) & 0xF;
                b[0] = std::byte(nibble < 10 ? '0' + nibble : 'A' + nibble - 10);
                soc::uart.write(std::span<const std::byte>(b, 1));
            }
            b[0] = std::byte(' ');
            soc::uart.write(std::span<const std::byte>(b, 1));
        }
        const std::byte newline[2] = { std::byte('\r'), std::byte('\n') };
        soc::uart.write(std::span<const std::byte>(newline, 2));
    };

    // Load 256-bit AES key
    uint32_t key[8] = {
        0x11111111, 0x11111111,
        0x11111111, 0x11111111,
        0x11111111, 0x11111111,
        0x11111111, 0x11111111
    };
    for (int i = 0; i < 8; ++i)
        aes_key[i] = key[i];

    // Load 128-bit IV
    uint32_t iv[4] = {
        0x33333333, 0x33333333,
        0x33333333, 0x33333333
    };
    for (int i = 0; i < 4; ++i)
        aes_iv[i] = iv[i];

    // Set AES mode to decryption (CTR)
    *aes_mode = 1;

    soc::uart.write(std::as_bytes(std::span{"[Ready to receive ciphertext blocks]\r\n"}));

    while (true) {
        std::byte header;
        soc::uart.read(std::span(&header, 1));
        
        if (header != std::byte(0xAA)) continue;  // ignore garbage

        std::byte buffer[16];
        soc::uart.read(std::span(buffer));  // now read the AES block

        for (int i = 0; i < 4; ++i) {
            aes_input[i] = 
                (static_cast<uint32_t>(buffer[i * 4 + 0]) << 0)  |
                (static_cast<uint32_t>(buffer[i * 4 + 1]) << 8)  |
                (static_cast<uint32_t>(buffer[i * 4 + 2]) << 16) |
                (static_cast<uint32_t>(buffer[i * 4 + 3]) << 24);
        }

        *aes_start = 1;



        while ((*aes_done & 0x1) == 0)
            for (volatile int i = 0; i < 1000; ++i) __asm__ volatile("");

        uint32_t output[4];
        for (int i = 0; i < 4; ++i)
            output[i] = aes_output[i];

        print_hex("Decrypted : ", output, 4);
    }

}