# Set the minimum required version for cmake
cmake_minimum_required(VERSION 3.4.1)

include_directories(include)

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

add_library(PIHELPER
        STATIC
        IMPORTED
        GLOBAL)
set_target_properties(
        PIHELPER
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/${ANDROID_ABI}/libpihelper.a)

add_library(
        pihelper-android
        SHARED
        pihelper-android.c)

find_library(
        log-lib
        log)

# The ordering below is important, at least regarding the OPENSSL & CRYPTO libraries
target_link_libraries(
        pihelper-android
        ${log-lib}
        z
        PIHELPER
        CURL
        OPENSSL
        CRYPTO
        JSONC)