#ifndef __DELTA_H
#define __DELTA_H

#include <jni.h>
#include <stdio.h>
#include <sys/stat.h>

jboolean patch(char* source, char* delta, char* out);
int decode(FILE* SrcFile, FILE* InFile, FILE* OutFile, int BufSize);

#endif
