LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
# OpenCV
OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on

include C:\DP\OpenCV-2.4.8-android-sdk\sdk\native\jni\OpenCV.mk


LOCAL_MODULE    := native_code
LOCAL_SRC_FILES := native.cpp 
LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)



