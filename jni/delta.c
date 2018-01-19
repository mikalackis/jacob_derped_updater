#include <fcntl.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

#include "delta.h"
#include "xdelta3/xdelta3.h"

jboolean patch(char* source, char* delta, char* out) {
	jboolean retu = JNI_TRUE;

	FILE* SourceFile;
	FILE* DeltaFile;
	FILE* OutFile;
	int ret;

	SourceFile = fopen(source, "rb");
	DeltaFile = fopen(delta, "rb");
	OutFile = fopen(out, "wb");

	ret = decode(SourceFile, DeltaFile, OutFile, 0x1000);

	fclose(SourceFile);
	fclose(DeltaFile);
	fclose(OutFile);

	if (ret) {
		fprintf (stderr, "Decode error: %d\n", ret);
		retu = JNI_FALSE;
	}

	return retu;
}

int decode(FILE* SrcFile, FILE* InFile, FILE* OutFile, int BufSize) {
	int r, ret;
	struct stat statbuf;
	xd3_stream stream;
	xd3_config config;
	xd3_source source;
	void* Input_Buf;
	int Input_Buf_Read;

	if (BufSize < XD3_ALLOCSIZE)
		BufSize = XD3_ALLOCSIZE;

	memset (&stream, 0, sizeof (stream));
	memset (&source, 0, sizeof (source));

	xd3_init_config(&config, XD3_ADLER32);
	config.winsize = BufSize;
	xd3_config_stream(&stream, &config);

	if (SrcFile) {
		r = fstat(fileno(SrcFile), &statbuf);
		if (r)
			return r;

		source.blksize = BufSize;
		source.curblk = malloc(source.blksize);

		/* Load 1st block of stream. */
		r = fseek(SrcFile, 0, SEEK_SET);
		if (r)
			return r;
		source.onblk = fread((void*)source.curblk, 1, source.blksize, SrcFile);
		source.curblkno = 0;
		/* Set the stream. */
		xd3_set_source(&stream, &source);
	}

	Input_Buf = malloc(BufSize);

	fseek(InFile, 0, SEEK_SET);
	do {
		Input_Buf_Read = fread(Input_Buf, 1, BufSize, InFile);
		if (Input_Buf_Read < BufSize) {
			xd3_set_flags(&stream, XD3_FLUSH | stream.flags);
		}
		xd3_avail_input(&stream, Input_Buf, Input_Buf_Read);

		process:
			ret = xd3_decode_input(&stream);

			switch (ret) {
				case XD3_INPUT: {
					fprintf (stderr,"XD3_INPUT\n");
					continue;
				}

				case XD3_OUTPUT: {
					fprintf (stderr,"XD3_OUTPUT\n");
					r = fwrite(stream.next_out, 1, stream.avail_out, OutFile);
					if (r != (int)stream.avail_out)
						return r;
					xd3_consume_output(&stream);
					goto process;
				}

				case XD3_GETSRCBLK: {
					fprintf (stderr,"XD3_GETSRCBLK %qd\n", source.getblkno);
					if (SrcFile) {
						r = fseek(SrcFile, source.blksize * source.getblkno, SEEK_SET);
						if (r)
							return r;
						source.onblk = fread((void*)source.curblk, 1, source.blksize, SrcFile);
						source.curblkno = source.getblkno;
					}
					goto process;
				}

				case XD3_GOTHEADER: {
					fprintf (stderr,"XD3_GOTHEADER\n");
					goto process;
				}

				case XD3_WINSTART: {
					fprintf (stderr,"XD3_WINSTART\n");
					goto process;
				}

				case XD3_WINFINISH: {
					fprintf (stderr,"XD3_WINFINISH\n");
					goto process;
				}

				default: {
					fprintf (stderr,"!!! INVALID %s %d !!!\n", stream.msg, ret);
					return ret;
				}

			}
	} while (Input_Buf_Read == BufSize);

	free(Input_Buf);

	free((void*)source.curblk);
	xd3_close_stream(&stream);
	xd3_free_stream(&stream);

	return 0;

}
