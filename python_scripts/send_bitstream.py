#This script sends encrypted partial bitstream via UART
# Its sends 128bits at a time to be processed by the AES module

#The path must be changed with the path of the correct encrypted partial bitstream

import serial
import time

SERIAL_PORT = "/dev/ttyUSB1"
BAUD_RATE = 115200
BITSTREAM_FILE = "partial_flashing_encrypted.bin"

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

        # Frame format: 0xAA (sync) + 0x11 (start signal) + 16-byte block
        framed_block = b"\xAA" + block
        #stop_block =

        ser.write(framed_block)
        print(f"[TX] Block {i//block_size:02d}: {framed_block.hex().upper()}")

        # Wait for decrypted response
        print(ser.readline())

 
       
