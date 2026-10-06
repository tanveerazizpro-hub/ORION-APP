#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_orion_app_MainActivity_stringFromJNI(JNIEnv* env, jobject /* this */) {
    std::string hello = "O.R.I.O.N. Native Engine Ready";
    return env->NewStringUTF(hello.c_str());
}
