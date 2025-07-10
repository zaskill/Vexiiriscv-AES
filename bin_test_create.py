from pathlib import Path
import secrets  # Or use: import os

# Output path
output_file = Path("test_input.bin")

# Generate 16 random bytes
data = secrets.token_bytes(16)  # Or: os.urandom(16)

# Write to file
output_file.write_bytes(data)

print(f"Generated {output_file} with content:")
print(" ".join(f"{b:02X}" for b in data))