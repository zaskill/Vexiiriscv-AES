import time

def read_bin_file_in_chunks():
    file_path = "partial_dummy.bin"  
    chunk_size = 16

    with open(file_path, "rb") as f:
        i = 0
        while True:
            chunk = f.read(chunk_size)
            if not chunk:
                break

            # Padding si nécessaire
            if len(chunk) < chunk_size:
                chunk += b'\x00' * (chunk_size - len(chunk))

            print(f"Chunk {i:04d}: {chunk.hex().upper()}")
            i += 1
            time.sleep(0.01)

read_bin_file_in_chunks()




