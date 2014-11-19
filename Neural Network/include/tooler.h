#ifndef TOOLER_H_INCLUDED
#define TOOLER_H_INCLUDED
#include"opencv/cv.h"
#include "opencv2/imgproc.hpp"

using namespace std;
using namespace cv;
enum CHANGE_MODEL{
    MatToIplImage,
    IplImageToMat
};
class tooler{
     private:
        Mat *_mat;
        IplImage *_Ipl;
        bool isAllocatedMat;
        bool isAllocatedIpl;
        Mat porkerMat;
        IplImage porkerIpl;
      public:
        IplImage matoipl(Mat&);
        Mat ipltomat(IplImage &);
        tooler();
        virtual ~tooler();
};


#endif // TOOLER_H_INCLUDED
