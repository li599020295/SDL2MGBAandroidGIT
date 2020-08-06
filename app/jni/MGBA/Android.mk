LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := main

SDL_PATH := ../SDL
PNG_PATH := ../libpng
ZIP_PATH := ../zlib

#Add include
LOCAL_C_INCLUDES := $(LOCAL_PATH)/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/$(PNG_PATH)/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/$(ZIP_PATH)/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/$(SDL_PATH)/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/core/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/gb/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/gba/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/platform/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/platform/sdl/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/mgba/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/mgba-util/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/mgba/core/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/mgba/internal/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/mgba/debugger/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/mgba/feature/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/mgba/gb/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/mgba/gba/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/third-party/inih/

LOCAL_EXPORT_C_INCLUDES += $(LOCAL_PATH)/include/
LOCAL_EXPORT_C_INCLUDES += $(LOCAL_PATH)/core/
LOCAL_EXPORT_C_INCLUDES += $(LOCAL_PATH)/include/mgba/
LOCAL_EXPORT_C_INCLUDES += $(LOCAL_PATH)/include/mgba/internal/
LOCAL_EXPORT_C_INCLUDES += $(LOCAL_PATH)/include/mgba/core/
LOCAL_EXPORT_C_INCLUDES += $(LOCAL_PATH)/include/mgba-util/
LOCAL_EXPORT_C_INCLUDES += $(LOCAL_PATH)/platform/
LOCAL_EXPORT_C_INCLUDES += $(LOCAL_PATH)/platform/sdl/

# Add your application source files here...
LOCAL_SRC_FILES := \
    $(subst $(LOCAL_PATH)/,, \
    $(wildcard $(LOCAL_PATH)/core/*.c) \
    $(wildcard $(LOCAL_PATH)/feature/*.c) \
    $(wildcard $(LOCAL_PATH)/util/*.c) \
    $(wildcard $(LOCAL_PATH)/util/vfs/*.c) \
    $(wildcard $(LOCAL_PATH)/third-party/inih/*.c) \
    $(wildcard $(LOCAL_PATH)/third-party/blip_buf/*.c) \
    $(wildcard $(LOCAL_PATH)/arm/*.c) \
    $(wildcard $(LOCAL_PATH)/gb/*.c) \
    $(wildcard $(LOCAL_PATH)/gb/extra/*.c) \
    $(wildcard $(LOCAL_PATH)/gb/renderers/*.c) \
    $(wildcard $(LOCAL_PATH)/gb/sio/*.c) \
    $(wildcard $(LOCAL_PATH)/gba/*c) \
    $(wildcard $(LOCAL_PATH)/gba/extra/*.c) \
    $(wildcard $(LOCAL_PATH)/gba/cheats/*.c) \
    $(wildcard $(LOCAL_PATH)/gba/renderers/*.c) \
    $(wildcard $(LOCAL_PATH)/gba/rr/*.c) \
    $(wildcard $(LOCAL_PATH)/platform/sdl/*.c) \
    $(wildcard $(LOCAL_PATH)/platform/posix/*.c) \
    $(wildcard $(LOCAL_PATH)/sm83/*.c) \
    $(wildcard $(LOCAL_PATH)/gba/sio/*.c))

LOCAL_STATIC_LIBRARIES += zlib SDL2 png

LOCAL_LDLIBS := -lGLESv1_CM -llog -lGLESv2 -lOpenSLES -lEGL -landroid

LOCAL_CPPFLAGS += -Wno-write-strings -Wno-overflow -std=c++11

LOCAL_CFLAGS += -DSDL -DMGBA_STANDALONE -DHAVE_XLOCALE -DM_CORE_GBA -DM_CORE_GB -DUSE_PTHREADS -DCOLOR_16_BIT -DCOLOR_5_6_5 -DUSE_PNG

#优化注意03会导致程序不可调试
#LOCAL_CFLAGS += -DNDEBUG -O3 -fno-exceptions -fno-rtti

include $(BUILD_SHARED_LIBRARY)