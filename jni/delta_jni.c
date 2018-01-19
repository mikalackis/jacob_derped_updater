#include "delta.h"
#include "delta_jni.h"

JNIEXPORT jboolean JNICALL Java_com_invictrixrom_updater_Delta_patch(JNIEnv *env, jobject obj, jstring source, jstring delta, jstring out) {
	const char* stringSource = (*env)->GetStringUTFChars(env, source, 0);
	const char* stringDelta = (*env)->GetStringUTFChars(env, delta, 0);
	const char* stringOut = (*env)->GetStringUTFChars(env, out, 0);

	jboolean ret = patch((char*) stringSource, (char*) stringDelta, (char*) stringOut);

	(*env)->ReleaseStringUTFChars(env, source, stringSource);
	(*env)->ReleaseStringUTFChars(env, delta, stringDelta);
	(*env)->ReleaseStringUTFChars(env, out, stringOut);

	return ret;
}
