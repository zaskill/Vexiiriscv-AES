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
    

    volatile uint32_t* aes_input  = reinterpret_cast<volatile uint32_t*>(AES_INPUT_BASE);
    volatile uint32_t* aes_key    = reinterpret_cast<volatile uint32_t*>(AES_KEY_BASE);
    volatile uint32_t* aes_output = reinterpret_cast<volatile uint32_t*>(AES_OUTPUT_BASE);
    volatile uint32_t* aes_done   = reinterpret_cast<volatile uint32_t*>(AES_DONE_REG);
    volatile uint32_t* aes_mode   = reinterpret_cast<volatile uint32_t*>(AES_MODE_REG);
    volatile uint32_t* aes_iv     = reinterpret_cast<volatile uint32_t*>(AES_IV_REG);


    auto print_hex = [](const char* label, const uint32_t* data, int len) {
        soc::uart.write(std::span<const std::byte>(
            reinterpret_cast<const std::byte*>(label), std::strlen(label)));

        std::byte b[1];
        for (int i = len - 1; i >= 0; --i) {
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

   
    uint32_t key[8] = {
        0x11111111, 0x11111111,
        0x11111111, 0x11111111,
        0x11111111, 0x11111111,
        0x11111111, 0x11111111
    };

   
    uint32_t input[4] = {
        0x55555555, 0x55555555, 0x55555555, 0x55555555
    };

    uint32_t iv[4] = {
        0x33333333, 0x33333333, 0x33333333, 0x33333333
    };

    uint32_t ciphertext[4];

    // Write mode (0 = encryption)
    *aes_mode = 0;

    for (int i = 0; i < 4; ++i) {
    aes_input[i] = input[i];
    //print_hex("aes_input  : ", const_cast<const uint32_t*>(&input[i]), 1);
    }   

    for (int i = 0; i < 8; ++i) {
    aes_key[i] = key[i];
    //print_hex("aes_key    : ", const_cast<const uint32_t*>(&key[i]), 1);
    }

      for (int i = 0; i < 4; ++i) {
    aes_iv[i] = iv[i];
    //print_hex("aes_input  : ", const_cast<const uint32_t*>(&input[i]), 1);
     }   

 
    while ((*aes_done & 0x1) == 0)
        for (volatile int i = 0; i < 10000; ++i) __asm__ volatile("");

    /*
    for (int i = 0; i < 4; ++i)
        ciphertext[i] = aes_output[i];
    */

    print_hex("Key        : ", key, 8);
    print_hex("Plaintext  : ", input, 4);
    print_hex("iv  : ", iv, 4);
    //print_hex("Ciphertext : ", ciphertext, 4);




    while (true); // halt
}
