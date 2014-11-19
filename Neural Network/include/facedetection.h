#ifndef FACEDETECTION_H
#define FACEDETECTION_H
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
enum DETECT_RESULT{
    ERROR=0,
    NONE=1,
    CATCHED=2
};
enum MODE{
    SUSPEND_MODE=0,
    IMG_MODE=1,
    VIDEO_MODE
};

class facedetection
{
    public:
        int catchedNum;

        MODE                          mode;
        Mat getFace(int index=0 );
        DETECT_RESULT   run(Mat *input=NULL,Mat *output=NULL);
        facedetection();
        virtual ~facedetection();
    protected:
        Mat porkerFace;
        vector<Rect>  faceRect;
        vector<int>  num;
        Mat                          _img_grey;
        Mat reSize(Mat img,size_t sz=128);
    private:
        string                     cascadeFacePath;
        CascadeClassifier faceCascade;

       bool  init();
};

#endif // FACEDETECTION_H//*/
