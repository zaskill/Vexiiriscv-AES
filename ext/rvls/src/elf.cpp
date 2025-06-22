// SPDX-FileCopyrightText: 2023 "Everybody"
//
// SPDX-License-Identifier: MIT


#include "elf.hpp"

Elf::Elf(const char* path){
    if ( !reader.load( path )) {
        std::cout << "Can't find or process ELF file " << path << std::endl;
        exit(1);
    }
}

void Elf::visitBytes(std::function<void(u8, u64)> func){
    // Print ELF file sections info
    Elf_Half sec_num = reader.sections.size();
    for (int i = 0; i < sec_num; ++i) {
        section *psec = reader.sections[i];
        Elf_Xword flags = psec->get_flags();
        if(flags & SHF_ALLOC){
            auto size = psec->get_size();
            auto data = reader.sections[i]->get_data();
            u64 address = psec->get_address();
            for(u32 i = 0;i < size; i++){
                func(data ? *data++ : 0, address++);
            }
        }
    }
}

u64 Elf::getSymbolAddress(char *targetName){
    Elf_Half sec_num = reader.sections.size();
    for (int i = 0; i < sec_num; ++i) {
        section *psec = reader.sections[i];
        // Check section type
        if (psec->get_type() == SHT_SYMTAB) {
            const symbol_section_accessor symbols(reader, psec);
            for (unsigned int j = 0; j < symbols.get_symbols_num(); ++j) {
                std::string name;
                Elf64_Addr value;
                Elf_Xword size;
                unsigned char bind;
                unsigned char type;
                Elf_Half section_index;
                unsigned char other;

                // Read symbol properties
                symbols.get_symbol(j, name, value, size, bind, type,
                        section_index, other);
                if(!strcmp(targetName, name.c_str())){
                    return value;
                }
            }
        }
    }
    std::cout << "Can't find symbole " << targetName << std::endl;
    exit(1);
}
