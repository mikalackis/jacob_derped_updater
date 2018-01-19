LOCAL_PATH := $(call my-dir)

xdelta3_cflags := \
        -O3 \
        -fno-function-sections -fno-data-sections -fno-inline \
        -DSUPPORT_ANDROID_PRELINK_TAGS \
        -DGENERIC_ENCODE_TABLES=0 \
        -DREGRESSION_TEST=0 \
        -DSECONDARY_DJW=1 \
        -DSECONDARY_FGK=1 \
        -DXD3_DEBUG=0 \
        -DXD3_MAIN=0 \
        -DXD3_POSIX=1 \
        -DXD3_USE_LARGEFILE64=1 \
	-DSIZEOF_UNSIGNED_LONG_LONG=8 \
	-DSIZEOF_SIZE_T=8

include $(CLEAR_VARS)

LOCAL_MODULE := libinvupdater
LOCAL_SRC_FILES += delta.c delta_jni.c

LOCAL_CFLAGS += $(xdelta3_cflags)

LOCAL_C_INCLUDES += external/xdelta3

LOCAL_SHARED_LIBRARIES := libxdelta3

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE := libinvdd
LOCAL_SRC_FILES += dd.c dd_jni.c

include $(BUILD_SHARED_LIBRARY)
