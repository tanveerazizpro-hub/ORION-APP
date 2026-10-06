cmake_minimum_required(VERSION 3.22.1)
project("orion")

# ============================================
# GGML / llama.cpp options
# ============================================
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_COMMON OFF CACHE BOOL "" FORCE)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
set(LLAMA_CURL OFF CACHE BOOL "" FORCE)

# Do NOT use -march=native (we're cross-compiling, not on the target device)
set(GGML_NATIVE OFF CACHE BOOL "" FORCE)

# ============================================
# Helio G99 CPU optimizations
# Cortex-A76 + A55, ARMv8.2-A with:
#   - dotprod : integer dot product (massive speedup for quantized models)
#   - fp16    : half-precision floating point
# ============================================
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O3 -march=armv8.2-a+dotprod+fp16 -ffast-math")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -march=armv8.2-a+dotprod+fp16 -ffast-math")

# ============================================
# Build llama.cpp and our JNI bridge
# ============================================
add_subdirectory(llama.cpp)

add_library(orion SHARED native-lib.cpp)

target_link_libraries(orion
    llama
    android
    log
)
