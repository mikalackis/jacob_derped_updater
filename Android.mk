LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := Updater
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_JAVA_LIBRARIES := bouncycastle
LOCAL_JNI_SHARED_LIBRARIES := libinvupdater libinvdd
LOCAL_REQUIRED_MODULES := libinvupdater libinvdd

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
