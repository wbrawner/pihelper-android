# Set the minimum required version for cmake
cmake_minimum_required(VERSION 3.4.1)

include_directories(include)

message("Cmake install libdir: ${CMAKE_CURRENT_SOURCE_DIR}/")
add_library(CURL
        STATIC
        IMPORTED
        GLOBAL)
set_target_properties(
        CURL
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/${ANDROID_ABI}/libcurl.a)

add_library(CRYPTO
        STATIC
        IMPORTED
        GLOBAL)
set_target_properties(
        CRYPTO
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/${ANDROID_ABI}/libcrypto.a)

add_library(OPENSSL
        STATIC
        IMPORTED
        GLOBAL)
set_target_properties(
        OPENSSL
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/${ANDROID_ABI}/libssl.a)

add_library(JSONC
        STATIC
        IMPORTED
        GLOBAL)
set_target_properties(
        JSONC
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/${ANDROID_ABI}/libjson-c.a)

add_subdirectory(pihelper)

add_library(
        pihelper
        SHARED
        pihelper-android.c
)

# Include the Android logging library for native code
find_library(
        log-lib
        log
)

# Link our library with the Android logging library
target_link_libraries(
        pihelper
        ${log-lib}
        z
        libpihelperstatic
        CURL
        CRYPTO
        OPENSSL
        JSONC
)