cmake_minimum_required(VERSION 3.22.1)
project("orion")

set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_COMMON OFF CACHE BOOL "" FORCE)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
set(LLAMA_CURL OFF CACHE BOOL "" FORCE)

# Cross-compiling from x86 — never use native detection
set(GGML_NATIVE OFF CACHE BOOL "" FORCE)

# Only build for this specific device, no runtime variants
set(GGML_CPU_ALL_VARIANTS OFF CACHE BOOL "" FORCE)

# Link-time optimization
set(CMAKE_INTERPROCEDURAL_OPTIMIZATION ON)

# Target the Cortex-A76 pipeline directly.
# -mcpu=cortex-a76 implies ARMv8.2-A + DOTPROD + FP16 and tunes scheduling.
# -mcpu implies -mtune=cortex-a76, unlike -march which only enables features.
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O3 -mcpu=cortex-a76 -ffast-math")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -mcpu=cortex-a76 -ffast-math")

add_subdirectory(llama.cpp)

add_library(orion SHARED native-lib.cpp)
target_link_libraries(orion llama android log)
