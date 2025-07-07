import serial
import struct
from elftools.elf.elffile import ELFFile

# === HARD-CODED CONFIGURATION ===
ELF_PATH     = "/nobackup/Vexiiriscv-AES/VexiiFirmware/build/app/aes/example-aes.elf"
SERIAL_PORT  = "/dev/ttyUSB1"    # ← adapt this if needed
BAUDRATE     = 115200

def send_word(ser, word):
    ser.write(struct.pack('<I', word))  # 4 bytes, little-endian

def send_segment(ser, addr, data):
    print(f"[+] Sending segment at 0x{addr:08X} ({len(data)} bytes)")
    send_word(ser, addr)
    send_word(ser, len(data))
    ser.write(data)

def main():
    with open(ELF_PATH, 'rb') as f:
        elf = ELFFile(f)

        segments = [
            seg for seg in elf.iter_segments()
            if seg['p_type'] == 'PT_LOAD' and seg['p_filesz'] > 0
        ]

        with serial.Serial(SERIAL_PORT, BAUDRATE, timeout=1) as ser:
            print(f"[INFO] Sending {len(segments)} segments...")

            # Step 1: send number of segments
            send_word(ser, len(segments))

            # Step 2: send each segment
            for seg in segments:
                addr = seg['p_paddr']
                data = seg.data()
                send_segment(ser, addr, data)

            # Step 3: send entry point
            entry_point = elf.header['e_entry']
            print(f" Sending entry point: 0x{entry_point:08X}")
            send_word(ser, entry_point)

            print(" Upload complete. Waiting for UART response:\n")
            while True:
                line = ser.readline()
                if line:
                    print(line.decode(errors="ignore"), end='')

if __name__ == "__main__":
    main()
