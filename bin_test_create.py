
from pathlib import Path

# Output path
output_file = Path("test_input.bin")

# 16 bytes of value 0x66
data = bytes([0x22] * 1600)

# Write to file
output_file.write_bytes(data)

print(f"Generated {output_file} with content:")
print(" ".join(f"{b:02X}" for b in data))