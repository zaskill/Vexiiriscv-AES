# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 3.28

# Delete rule output on recipe failure.
.DELETE_ON_ERROR:

#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:

# Disable VCS-based implicit rules.
% : %,v

# Disable VCS-based implicit rules.
% : RCS/%

# Disable VCS-based implicit rules.
% : RCS/%,v

# Disable VCS-based implicit rules.
% : SCCS/s.%

# Disable VCS-based implicit rules.
% : s.%

.SUFFIXES: .hpux_make_needs_suffix_list

# Command-line flag to silence nested $(MAKE).
$(VERBOSE)MAKESILENT = -s

#Suppress display of executed commands.
$(VERBOSE).SILENT:

# A target that is always out of date.
cmake_force:
.PHONY : cmake_force

#=============================================================================
# Set environment variables for the build.

# The shell in which to execute make rules.
SHELL = /bin/sh

# The CMake executable.
CMAKE_COMMAND = /usr/bin/cmake

# The command to remove a file.
RM = /usr/bin/cmake -E rm -f

# Escaping for special characters.
EQUALS = =

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = /nobackup/Vexiiriscv-AES/VexiiFirmware

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = /nobackup/Vexiiriscv-AES/VexiiFirmware/build

# Include any dependencies generated for this target.
include app/asm_app/CMakeFiles/asm-app.dir/depend.make
# Include any dependencies generated by the compiler for this target.
include app/asm_app/CMakeFiles/asm-app.dir/compiler_depend.make

# Include the progress variables for this target.
include app/asm_app/CMakeFiles/asm-app.dir/progress.make

# Include the compile flags for this target's objects.
include app/asm_app/CMakeFiles/asm-app.dir/flags.make

app/asm_app/CMakeFiles/asm-app.dir/main.S.obj: app/asm_app/CMakeFiles/asm-app.dir/flags.make
app/asm_app/CMakeFiles/asm-app.dir/main.S.obj: /nobackup/Vexiiriscv-AES/VexiiFirmware/app/asm_app/main.S
	@$(CMAKE_COMMAND) -E cmake_echo_color "--switch=$(COLOR)" --green --progress-dir=/nobackup/Vexiiriscv-AES/VexiiFirmware/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_1) "Building ASM object app/asm_app/CMakeFiles/asm-app.dir/main.S.obj"
	cd /nobackup/Vexiiriscv-AES/VexiiFirmware/build/app/asm_app && /nobackup/0-ZTBER/RISCV/bin/riscv64-unknown-elf-gcc $(ASM_DEFINES) $(ASM_INCLUDES) $(ASM_FLAGS) -o CMakeFiles/asm-app.dir/main.S.obj -c /nobackup/Vexiiriscv-AES/VexiiFirmware/app/asm_app/main.S

app/asm_app/CMakeFiles/asm-app.dir/main.S.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color "--switch=$(COLOR)" --green "Preprocessing ASM source to CMakeFiles/asm-app.dir/main.S.i"
	cd /nobackup/Vexiiriscv-AES/VexiiFirmware/build/app/asm_app && /nobackup/0-ZTBER/RISCV/bin/riscv64-unknown-elf-gcc $(ASM_DEFINES) $(ASM_INCLUDES) $(ASM_FLAGS) -E /nobackup/Vexiiriscv-AES/VexiiFirmware/app/asm_app/main.S > CMakeFiles/asm-app.dir/main.S.i

app/asm_app/CMakeFiles/asm-app.dir/main.S.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color "--switch=$(COLOR)" --green "Compiling ASM source to assembly CMakeFiles/asm-app.dir/main.S.s"
	cd /nobackup/Vexiiriscv-AES/VexiiFirmware/build/app/asm_app && /nobackup/0-ZTBER/RISCV/bin/riscv64-unknown-elf-gcc $(ASM_DEFINES) $(ASM_INCLUDES) $(ASM_FLAGS) -S /nobackup/Vexiiriscv-AES/VexiiFirmware/app/asm_app/main.S -o CMakeFiles/asm-app.dir/main.S.s

app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/common/start.S.obj: app/asm_app/CMakeFiles/asm-app.dir/flags.make
app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/common/start.S.obj: /nobackup/Vexiiriscv-AES/VexiiFirmware/soc/common/start.S
	@$(CMAKE_COMMAND) -E cmake_echo_color "--switch=$(COLOR)" --green --progress-dir=/nobackup/Vexiiriscv-AES/VexiiFirmware/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_2) "Building ASM object app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/common/start.S.obj"
	cd /nobackup/Vexiiriscv-AES/VexiiFirmware/build/app/asm_app && /nobackup/0-ZTBER/RISCV/bin/riscv64-unknown-elf-gcc $(ASM_DEFINES) $(ASM_INCLUDES) $(ASM_FLAGS) -o CMakeFiles/asm-app.dir/__/__/soc/common/start.S.obj -c /nobackup/Vexiiriscv-AES/VexiiFirmware/soc/common/start.S

app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/common/start.S.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color "--switch=$(COLOR)" --green "Preprocessing ASM source to CMakeFiles/asm-app.dir/__/__/soc/common/start.S.i"
	cd /nobackup/Vexiiriscv-AES/VexiiFirmware/build/app/asm_app && /nobackup/0-ZTBER/RISCV/bin/riscv64-unknown-elf-gcc $(ASM_DEFINES) $(ASM_INCLUDES) $(ASM_FLAGS) -E /nobackup/Vexiiriscv-AES/VexiiFirmware/soc/common/start.S > CMakeFiles/asm-app.dir/__/__/soc/common/start.S.i

app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/common/start.S.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color "--switch=$(COLOR)" --green "Compiling ASM source to assembly CMakeFiles/asm-app.dir/__/__/soc/common/start.S.s"
	cd /nobackup/Vexiiriscv-AES/VexiiFirmware/build/app/asm_app && /nobackup/0-ZTBER/RISCV/bin/riscv64-unknown-elf-gcc $(ASM_DEFINES) $(ASM_INCLUDES) $(ASM_FLAGS) -S /nobackup/Vexiiriscv-AES/VexiiFirmware/soc/common/start.S -o CMakeFiles/asm-app.dir/__/__/soc/common/start.S.s

app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/common/trap.S.obj: app/asm_app/CMakeFiles/asm-app.dir/flags.make
app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/common/trap.S.obj: /nobackup/Vexiiriscv-AES/VexiiFirmware/soc/common/trap.S
	@$(CMAKE_COMMAND) -E cmake_echo_color "--switch=$(COLOR)" --green --progress-dir=/nobackup/Vexiiriscv-AES/VexiiFirmware/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_3) "Building ASM object app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/common/trap.S.obj"
	cd /nobackup/Vexiiriscv-AES/VexiiFirmware/build/app/asm_app && /nobackup/0-ZTBER/RISCV/bin/riscv64-unknown-elf-gcc $(ASM_DEFINES) $(ASM_INCLUDES) $(ASM_FLAGS) -o CMakeFiles/asm-app.dir/__/__/soc/common/trap.S.obj -c /nobackup/Vexiiriscv-AES/VexiiFirmware/soc/common/trap.S

app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/common/trap.S.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color "--switch=$(COLOR)" --green "Preprocessing ASM source to CMakeFiles/asm-app.dir/__/__/soc/common/trap.S.i"
	cd /nobackup/Vexiiriscv-AES/VexiiFirmware/build/app/asm_app && /nobackup/0-ZTBER/RISCV/bin/riscv64-unknown-elf-gcc $(ASM_DEFINES) $(ASM_INCLUDES) $(ASM_FLAGS) -E /nobackup/Vexiiriscv-AES/VexiiFirmware/soc/common/trap.S > CMakeFiles/asm-app.dir/__/__/soc/common/trap.S.i

app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/common/trap.S.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color "--switch=$(COLOR)" --green "Compiling ASM source to assembly CMakeFiles/asm-app.dir/__/__/soc/common/trap.S.s"
	cd /nobackup/Vexiiriscv-AES/VexiiFirmware/build/app/asm_app && /nobackup/0-ZTBER/RISCV/bin/riscv64-unknown-elf-gcc $(ASM_DEFINES) $(ASM_INCLUDES) $(ASM_FLAGS) -S /nobackup/Vexiiriscv-AES/VexiiFirmware/soc/common/trap.S -o CMakeFiles/asm-app.dir/__/__/soc/common/trap.S.s

app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/common/trap.cpp.obj: app/asm_app/CMakeFiles/asm-app.dir/flags.make
app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/common/trap.cpp.obj: /nobackup/Vexiiriscv-AES/VexiiFirmware/soc/common/trap.cpp
app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/common/trap.cpp.obj: app/asm_app/CMakeFiles/asm-app.dir/compiler_depend.ts
	@$(CMAKE_COMMAND) -E cmake_echo_color "--switch=$(COLOR)" --green --progress-dir=/nobackup/Vexiiriscv-AES/VexiiFirmware/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_4) "Building CXX object app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/common/trap.cpp.obj"
	cd /nobackup/Vexiiriscv-AES/VexiiFirmware/build/app/asm_app && /nobackup/0-ZTBER/RISCV/bin/riscv64-unknown-elf-g++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -MD -MT app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/common/trap.cpp.obj -MF CMakeFiles/asm-app.dir/__/__/soc/common/trap.cpp.obj.d -o CMakeFiles/asm-app.dir/__/__/soc/common/trap.cpp.obj -c /nobackup/Vexiiriscv-AES/VexiiFirmware/soc/common/trap.cpp

app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/common/trap.cpp.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color "--switch=$(COLOR)" --green "Preprocessing CXX source to CMakeFiles/asm-app.dir/__/__/soc/common/trap.cpp.i"
	cd /nobackup/Vexiiriscv-AES/VexiiFirmware/build/app/asm_app && /nobackup/0-ZTBER/RISCV/bin/riscv64-unknown-elf-g++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -E /nobackup/Vexiiriscv-AES/VexiiFirmware/soc/common/trap.cpp > CMakeFiles/asm-app.dir/__/__/soc/common/trap.cpp.i

app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/common/trap.cpp.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color "--switch=$(COLOR)" --green "Compiling CXX source to assembly CMakeFiles/asm-app.dir/__/__/soc/common/trap.cpp.s"
	cd /nobackup/Vexiiriscv-AES/VexiiFirmware/build/app/asm_app && /nobackup/0-ZTBER/RISCV/bin/riscv64-unknown-elf-g++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -S /nobackup/Vexiiriscv-AES/VexiiFirmware/soc/common/trap.cpp -o CMakeFiles/asm-app.dir/__/__/soc/common/trap.cpp.s

app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/microsoc/default/system/soc.cpp.obj: app/asm_app/CMakeFiles/asm-app.dir/flags.make
app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/microsoc/default/system/soc.cpp.obj: /nobackup/Vexiiriscv-AES/VexiiFirmware/soc/microsoc/default/system/soc.cpp
app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/microsoc/default/system/soc.cpp.obj: app/asm_app/CMakeFiles/asm-app.dir/compiler_depend.ts
	@$(CMAKE_COMMAND) -E cmake_echo_color "--switch=$(COLOR)" --green --progress-dir=/nobackup/Vexiiriscv-AES/VexiiFirmware/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_5) "Building CXX object app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/microsoc/default/system/soc.cpp.obj"
	cd /nobackup/Vexiiriscv-AES/VexiiFirmware/build/app/asm_app && /nobackup/0-ZTBER/RISCV/bin/riscv64-unknown-elf-g++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -MD -MT app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/microsoc/default/system/soc.cpp.obj -MF CMakeFiles/asm-app.dir/__/__/soc/microsoc/default/system/soc.cpp.obj.d -o CMakeFiles/asm-app.dir/__/__/soc/microsoc/default/system/soc.cpp.obj -c /nobackup/Vexiiriscv-AES/VexiiFirmware/soc/microsoc/default/system/soc.cpp

app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/microsoc/default/system/soc.cpp.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color "--switch=$(COLOR)" --green "Preprocessing CXX source to CMakeFiles/asm-app.dir/__/__/soc/microsoc/default/system/soc.cpp.i"
	cd /nobackup/Vexiiriscv-AES/VexiiFirmware/build/app/asm_app && /nobackup/0-ZTBER/RISCV/bin/riscv64-unknown-elf-g++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -E /nobackup/Vexiiriscv-AES/VexiiFirmware/soc/microsoc/default/system/soc.cpp > CMakeFiles/asm-app.dir/__/__/soc/microsoc/default/system/soc.cpp.i

app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/microsoc/default/system/soc.cpp.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color "--switch=$(COLOR)" --green "Compiling CXX source to assembly CMakeFiles/asm-app.dir/__/__/soc/microsoc/default/system/soc.cpp.s"
	cd /nobackup/Vexiiriscv-AES/VexiiFirmware/build/app/asm_app && /nobackup/0-ZTBER/RISCV/bin/riscv64-unknown-elf-g++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -S /nobackup/Vexiiriscv-AES/VexiiFirmware/soc/microsoc/default/system/soc.cpp -o CMakeFiles/asm-app.dir/__/__/soc/microsoc/default/system/soc.cpp.s

# Object files for target asm-app
asm__app_OBJECTS = \
"CMakeFiles/asm-app.dir/main.S.obj" \
"CMakeFiles/asm-app.dir/__/__/soc/common/start.S.obj" \
"CMakeFiles/asm-app.dir/__/__/soc/common/trap.S.obj" \
"CMakeFiles/asm-app.dir/__/__/soc/common/trap.cpp.obj" \
"CMakeFiles/asm-app.dir/__/__/soc/microsoc/default/system/soc.cpp.obj"

# External object files for target asm-app
asm__app_EXTERNAL_OBJECTS =

app/asm_app/asm-app.elf: app/asm_app/CMakeFiles/asm-app.dir/main.S.obj
app/asm_app/asm-app.elf: app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/common/start.S.obj
app/asm_app/asm-app.elf: app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/common/trap.S.obj
app/asm_app/asm-app.elf: app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/common/trap.cpp.obj
app/asm_app/asm-app.elf: app/asm_app/CMakeFiles/asm-app.dir/__/__/soc/microsoc/default/system/soc.cpp.obj
app/asm_app/asm-app.elf: app/asm_app/CMakeFiles/asm-app.dir/build.make
app/asm_app/asm-app.elf: app/asm_app/CMakeFiles/asm-app.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color "--switch=$(COLOR)" --green --bold --progress-dir=/nobackup/Vexiiriscv-AES/VexiiFirmware/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_6) "Linking CXX executable asm-app.elf"
	cd /nobackup/Vexiiriscv-AES/VexiiFirmware/build/app/asm_app && $(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/asm-app.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
app/asm_app/CMakeFiles/asm-app.dir/build: app/asm_app/asm-app.elf
.PHONY : app/asm_app/CMakeFiles/asm-app.dir/build

app/asm_app/CMakeFiles/asm-app.dir/clean:
	cd /nobackup/Vexiiriscv-AES/VexiiFirmware/build/app/asm_app && $(CMAKE_COMMAND) -P CMakeFiles/asm-app.dir/cmake_clean.cmake
.PHONY : app/asm_app/CMakeFiles/asm-app.dir/clean

app/asm_app/CMakeFiles/asm-app.dir/depend:
	cd /nobackup/Vexiiriscv-AES/VexiiFirmware/build && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /nobackup/Vexiiriscv-AES/VexiiFirmware /nobackup/Vexiiriscv-AES/VexiiFirmware/app/asm_app /nobackup/Vexiiriscv-AES/VexiiFirmware/build /nobackup/Vexiiriscv-AES/VexiiFirmware/build/app/asm_app /nobackup/Vexiiriscv-AES/VexiiFirmware/build/app/asm_app/CMakeFiles/asm-app.dir/DependInfo.cmake "--color=$(COLOR)"
.PHONY : app/asm_app/CMakeFiles/asm-app.dir/depend

