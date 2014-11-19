#ifndef FACEDETECT_INCLUDED
#define FACEDETECT_INCLUDED

#include "opencv2/objdetect.hpp"
#include "opencv2/imgcodecs.hpp"
#include "opencv2/videoio.hpp"
#include "opencv2/highgui.hpp"
#include "opencv2/imgproc.hpp"
#include "opencv2/core/utility.hpp"
#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/videoio/videoio_c.h"
#include "opencv2/highgui/highgui_c.h"
#include <cctype>
#include <iostream>
#include <iterator>
#include <stdio.h>
using namespace std;
using namespace cv;
enum CATCHED_FACE{
    ERROR=0,
    NONE=1,
    CATCHED=2
}
enum MODE{
    SUPEND_MODE=0,
    IMG_MODE=1,
    VIDEO_MODE
}

CATCHED_FACE   detectFaces(Mat &org_img,Mat *output=NULL);
CATCHED_FACE   detectFaces(VideoCapture &org_cam,Mat *output=NULL);

#endif // FACEDETECT_INCLUDED
