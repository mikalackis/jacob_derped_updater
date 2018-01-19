#ifndef DELTA_JNI_H
#define DELTA_JNI_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     com_invictrixrom_updater_Delta
 * Method:    patch
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_invictrixrom_updater_Delta_patch(JNIEnv *, jobject, jstring, jstring, jstring);

#ifdef __cplusplus
}
#endif

#endif
