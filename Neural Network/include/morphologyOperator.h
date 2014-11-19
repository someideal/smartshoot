#ifndef MORPHOLOGYOPERATOR_H
#define MORPHOLOGYOPERATOR_H
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/imgcodecs.hpp"
#include "opencv2/highgui/highgui.hpp"
#include <stdlib.h>
#include <stdio.h>

using namespace cv;
enum MORPH_TYPE{
    _MORPH_OPEN =2,
    _MORPH_CLOSE,
    _MORPH_GRADIENT,
    _MORPH_TOPHAT,
    _MORPH_BLACKHAT
};
enum MORPH_ELEM_TYPE{
    RECT,
    CROSS,
    ECLIPSE
};


class morphologyOperator
{
    public:
      Mat   Open(Mat &input,int sz=0,MORPH_ELEM_TYPE elem=RECT);
      Mat   Close(Mat &input,int sz=0,MORPH_ELEM_TYPE elem=RECT);
      Mat   Gradient(Mat &input,int sz=0,MORPH_ELEM_TYPE elem=RECT);
      Mat    Tophat(Mat &input,int sz=0,MORPH_ELEM_TYPE elem=RECT);
      Mat    Blackhat(Mat &input,int sz=0,MORPH_ELEM_TYPE elem=RECT);
        morphologyOperator();
        virtual ~morphologyOperator();
    protected:
    private:
        Mat porkerMorph;
        int morph_elem = 0;
        int morph_size = 0;

};

#endif // MORPHOLOGYOPERATOR_H
