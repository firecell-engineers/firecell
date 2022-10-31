#include <stdio.h>
#include "c/pl_edu_agh_firecell_engine_utils_cCodeManager.h"

JNIEXPORT void JNICALL Java_pl_edu_agh_firecell_engine_utils_cCodeManager_print(JNIEnv *env, jobject obj)
{
    printf("Hello world");
    return;
}

JNIEXPORT jdouble JNICALL Java_pl_edu_agh_firecell_engine_utils_cCodeManager_multiplication
    (JNIEnv *env, jobject obj, jdouble a, jdouble b)
{
    return a*b;
}
