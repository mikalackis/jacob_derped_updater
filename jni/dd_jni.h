#ifndef DD_JNI_H
#define DD_JNI_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     com_invictrixrom_updater_DD
 * Method:    patch
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_invictrixrom_updater_DD_dd(JNIEnv *, jobject, jstring, jstring);

#ifdef __cplusplus
}
#endif

#endif
