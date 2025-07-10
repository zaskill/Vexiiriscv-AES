#!/usr/bin/env python3

from Crypto.Cipher import AES
from Crypto.Util import Counter
from pathlib import Path

# === CONFIGURATION ===
input_path  = Path("partial_dummy.bin")                  # File filled with 0x66...
output_path = Path("partial_dummy_encrypted.bin")

# === AES Parameters (256-bit key, fixed IV, no counter increment)
key_hex = "1111111111111111111111111111111111111111111111111111111111111111"
iv_hex  = "33333333333333333333333333333333"

key = bytes.fromhex(key_hex)
iv  = int(iv_hex, 16)

# === Load input file and pad to 16-byte multiple if needed
bitstream = input_path.read_bytes()
padding_len = (16 - len(bitstream) % 16) % 16
bitstream += b"\x00" * padding_len

# === Encrypt each 16-byte block independently (same IV every time)
encrypted_blocks = []

for i in range(0, len(bitstream), 16):
    block = bitstream[i:i+16]
    ctr = Counter.new(128, initial_value=iv)
    cipher = AES.new(key, AES.MODE_CTR, counter=ctr)
    encrypted = cipher.encrypt(block)
    encrypted_blocks.append(encrypted)

# === Write encrypted data to output file
output_data = b"".join(encrypted_blocks)
output_path.write_bytes(output_data)

# === Log info
print(f"[OK] Encrypted {len(bitstream)} bytes in {len(encrypted_blocks)} blocks")
print(f"[->] Written to {output_path}")
