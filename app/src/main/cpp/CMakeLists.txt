cmake_minimum_required(VERSION 3.22.1)
project("orion")

# Configure llama.cpp for Android build
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_COMMON OFF CACHE BOOL "" FORCE)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
set(LLAMA_CURL OFF CACHE BOOL "" FORCE)

# Add llama.cpp as subdirectory
add_subdirectory(llama.cpp)

# Our JNI bridge library
add_library(orion SHARED native-lib.cpp)

target_link_libraries(orion
    llama
    android
    log
)
