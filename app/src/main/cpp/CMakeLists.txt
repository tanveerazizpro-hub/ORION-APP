cmake_minimum_required(VERSION 3.22.1)
project("orion")

add_library(orion SHARED native-lib.cpp)

if(EXISTS "${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp/CMakeLists.txt")
    add_subdirectory(llama.cpp)
    target_link_libraries(orion llama)
endif()

find_library(log-lib log)
target_link_libraries(orion ${log-lib})
