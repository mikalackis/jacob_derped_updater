#include <fcntl.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>

#include "dd.h"

jboolean dd(char* input, char* output) {
	int outputFile, inputFile;
	char buf[4096];
	ssize_t nread;
	int saved_errno;

	inputFile = open(input, O_RDONLY);
	if (inputFile < 0)
		return JNI_FALSE;

	outputFile = open(output, O_WRONLY | O_CREAT | O_TRUNC, 0755);
	if (outputFile < 0)
		goto out_error;

	while (nread = read(inputFile, buf, sizeof buf), nread > 0) {
		char *out_ptr = buf;
		ssize_t nwritten;

		do {
			nwritten = write(outputFile, out_ptr, nread);

			if (nwritten >= 0) {
				nread -= nwritten;
				out_ptr += nwritten;
			} else if (errno != EINTR) {
				goto out_error;
			}
		} while (nread > 0);
	}

	if (nread == 0) {
		if (close(outputFile) < 0) {
			outputFile = -1;
			goto out_error;
		}
		close(inputFile);
		return JNI_TRUE;
	}

	out_error:
		if (inputFile >= 0)
			close(inputFile);
		if (outputFile >= 0)
			close(outputFile);

	return JNI_FALSE;

}
