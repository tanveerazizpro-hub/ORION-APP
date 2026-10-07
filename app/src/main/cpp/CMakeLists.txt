cmake_minimum_required(VERSION 3.22.1)
project("orion")

set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_COMMON OFF CACHE BOOL "" FORCE)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
set(LLAMA_CURL OFF CACHE BOOL "" FORCE)

set(GGML_NATIVE OFF CACHE BOOL "" FORCE)
set(GGML_CPU_ALL_VARIANTS OFF CACHE BOOL "" FORCE)

# -mcpu tunes instruction scheduling for Cortex-A76 specifically.
# NO -ffast-math: it implies -ffinite-math-only, which breaks GGML.
# NO LTO: cross-compiling with LTO breaks the linker (ld.gold vs AArch64).
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O3 -mcpu=cortex-a76")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -mcpu=cortex-a76")

add_subdirectory(llama.cpp)

add_library(orion SHARED native-lib.cpp)
target_link_libraries(orion llama android log)
