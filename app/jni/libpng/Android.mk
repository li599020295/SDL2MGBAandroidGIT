LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := png

LOCAL_STATIC_LIBRARIES += zlib

ZIP_PATH := ../zlib
LOCAL_C_INCLUDES := $(LOCAL_PATH)/$(ZIP_PATH)/

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/.

LOCAL_SRC_FILES := \
	$(subst $(LOCAL_PATH)/,, \
	$(wildcard $(LOCAL_PATH)/*.c))

include $(BUILD_STATIC_LIBRARY)

