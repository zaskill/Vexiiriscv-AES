# This script was used to test the aes module output
# It encrypts a 128bit value to compare with the output of the opentitan module

from Crypto.Cipher import AES
from Crypto.Util import Counter
from binascii import unhexlify, hexlify

def reverse_full(data: bytes) -> bytes:
    return data[::-1]  # Reverse all 16 bytes

# Inputs
plaintext_hex = "55555555555555555555555555555555"
key_hex       = "1111111111111111111111111111111111111111111111111111111111111111"
iv_hex        = "33333333333333333333333333333333"

plaintext = unhexlify(plaintext_hex)
key       = unhexlify(key_hex)
iv        = int(iv_hex, 16)

ctr = Counter.new(128, initial_value=iv)
cipher = AES.new(key, AES.MODE_CTR, counter=ctr)
ciphertext_raw = cipher.encrypt(plaintext)
ciphertext_hw  = reverse_full(ciphertext_raw)

print("Plaintext (hex)         :", plaintext_hex.upper())
print("Key (256-bit)           :", key_hex.upper())
print("IV (hex)                :", iv_hex.upper())
print("Ciphertext (CTR, raw)   :", hexlify(ciphertext_raw).decode().upper())
print("Ciphertext (HW order)   :", hexlify(ciphertext_hw).decode().upper())
