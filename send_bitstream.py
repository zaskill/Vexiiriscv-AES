#!/usr/bin/env python3

import serial
import time

SERIAL_PORT = "/dev/ttyUSB1"
BAUD_RATE = 115200
BITSTREAM_FILE = "test_output_encrypted.bin"

with open(BITSTREAM_FILE, "rb") as f:
    bitstream_data = f.read()

# Open UART port
with serial.Serial(SERIAL_PORT, BAUD_RATE, timeout=2) as ser:
    print("[INFO] Sending bitstream...")
    block_size = 16  # AES block = 16 bytes

    for i in range(0, len(bitstream_data), block_size):
        block = bitstream_data[i:i+block_size]
        if len(block) < block_size:
            block += b"\x00" * (block_size - len(block))  # zero padding

        framed_block = b"\xAA" + block  # prepend 0xAA header

        ser.write(framed_block)
        time.sleep(0.01) 
        print(f"[TX] Block {i//block_size:02d}: {framed_block.hex().upper()}")

        # Read response line (decrypted output)
        print(ser.readline())
  
