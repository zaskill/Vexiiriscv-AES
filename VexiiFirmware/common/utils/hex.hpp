
#pragma once
#include <cstdint>

namespace utils::hex {
    // Converts a 32-bit word to an 8-char hex string 
   inline void word_to_hex(uint32_t value, char* out) {
    const char* hex = "0123456789ABCDEF";
    for (int i = 0; i < 8; ++i) {
        out[7 - i] = hex[(value >> (i * 4)) & 0xF];
    }
}

}

