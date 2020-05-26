#include "pihelper.h"
#include <jni.h>
#include <stdio.h>

static pihole_config *config;

JNIEXPORT void JNICALL
Java_com_wbrawner_libpihelper_PiHelperNative_initConfig(JNIEnv *env, jclass clazz) {
    config = pihelper_new_config();
}

JNIEXPORT void JNICALL
Java_com_wbrawner_libpihelper_PiHelperNative_readConfig(JNIEnv *env, jclass clazz,
                                                        jstring config_path) {
    const char *nativeString = (*env)->GetStringUTFChars(env, config_path, 0);
    config = pihelper_read_config((char *) nativeString);
    (*env)->ReleaseStringUTFChars(env, config_path, nativeString);
}

JNIEXPORT jstring JNICALL
Java_com_wbrawner_libpihelper_PiHelperNative_getHost(JNIEnv *env, jclass clazz) {
    if (config == NULL) {
        return NULL;
    }
    return (*env)->NewStringUTF(env, config->host);
}

JNIEXPORT void JNICALL
Java_com_wbrawner_libpihelper_PiHelperNative_setHost(JNIEnv *env, jclass clazz, jstring host) {
    if (config == NULL || host == NULL) {
        return;
    }
    const char *nativeString = (*env)->GetStringUTFChars(env, host, 0);
    pihelper_config_set_host(config, (char *) nativeString);
    (*env)->ReleaseStringUTFChars(env, host, nativeString);
}

JNIEXPORT jstring JNICALL
Java_com_wbrawner_libpihelper_PiHelperNative_getApiKey(JNIEnv *env, jclass clazz) {
    if (config == NULL) {
        return NULL;
    }
    return (*env)->NewStringUTF(env, config->api_key);
}

JNIEXPORT void JNICALL
Java_com_wbrawner_libpihelper_PiHelperNative_setApiKey(JNIEnv *env, jclass clazz,
                                                            jstring api_key) {
    if (config == NULL || api_key == NULL) {
        return;
    }
    const char *nativeString = (*env)->GetStringUTFChars(env, api_key, 0);
    pihelper_config_set_api_key(config, (char *) nativeString);
    (*env)->ReleaseStringUTFChars(env, api_key, nativeString);
}

JNIEXPORT void JNICALL
Java_com_wbrawner_libpihelper_PiHelperNative_setPassword(JNIEnv *env, jclass clazz,
                                                         jstring password) {
    if (config == NULL || password == NULL) {
        return;
    }
    const char *nativeString = (*env)->GetStringUTFChars(env, password, 0);
    pihelper_config_set_password(config, (char *) nativeString);
    (*env)->ReleaseStringUTFChars(env, password, nativeString);
}

JNIEXPORT void JNICALL
Java_com_wbrawner_libpihelper_PiHelperNative_saveConfig(JNIEnv *env, jclass clazz,
                                                        jstring config_path) {

    if (config == NULL || config_path == NULL) {
        return;
    }
    const char *nativeString = (*env)->GetStringUTFChars(env, config_path, 0);
    pihelper_save_config(config, (char *) nativeString);
    (*env)->ReleaseStringUTFChars(env, config_path, nativeString);
}

JNIEXPORT jint JNICALL
Java_com_wbrawner_libpihelper_PiHelperNative_getStatus(JNIEnv *env, jclass clazz) {
    return pihelper_get_status(config);
}

JNIEXPORT jint JNICALL
Java_com_wbrawner_libpihelper_PiHelperNative_enable(JNIEnv *env, jclass clazz) {
    return pihelper_enable_pihole(config);
}

JNIEXPORT jint JNICALL
Java_com_wbrawner_libpihelper_PiHelperNative_disable(JNIEnv *env, jclass clazz, jstring seconds) {
    const char *nativeString = (*env)->GetStringUTFChars(env, seconds, 0);
    int retVal = pihelper_disable_pihole(config, (char *) nativeString);
    (*env)->ReleaseStringUTFChars(env, seconds, nativeString);
    return retVal;
}

JNIEXPORT void JNICALL
Java_com_wbrawner_libpihelper_PiHelperNative_cleanup(JNIEnv *env, jclass clazz) {
    pihelper_free_config(config);
    config = NULL;
}
