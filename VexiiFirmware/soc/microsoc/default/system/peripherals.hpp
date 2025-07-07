#pragma once

#include <cstdint>

constexpr static const std::uintptr_t CLINT  = 0x10010000;
constexpr static const std::uintptr_t PLIC   = 0x10C00000;
constexpr static const std::uintptr_t UART_A = 0x10001000;
constexpr std::uintptr_t AES_BASE         = 0x10005000;


/*

constexpr std::uintptr_t AES_INPUT_BASE   = 0x10005004;  


constexpr std::uintptr_t AES_KEY_BASE     = 0x10005014;  


constexpr std::uintptr_t AES_OUTPUT_BASE  = 0x10005034; 


constexpr std::uintptr_t AES_DONE_REG     = 0x10005044; 


constexpr std::uintptr_t AES_MODE_REG     = 0x10005048; 

*/