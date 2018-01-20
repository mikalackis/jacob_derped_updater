#include "dd.h"
#include "dd_jni.h"

JNIEXPORT jboolean JNICALL Java_com_invictrixrom_updater_DD_dd(JNIEnv *env, jobject obj, jstring input, jstring output) {
	const char* stringInput = (*env)->GetStringUTFChars(env, input, 0);
	const char* stringOutput = (*env)->GetStringUTFChars(env, output, 0);

	jboolean ret = dd((char*) stringInput, (char*) stringOutput);

	(*env)->ReleaseStringUTFChars(env, input, stringInput);
	(*env)->ReleaseStringUTFChars(env, output, stringOutput);

	return ret;
}
