#include"../include/tooler.h"
using namespace cv;

IplImage tooler::matoipl(Mat&mat){
        _Ipl= new IplImage(mat);
        return *_Ipl;
}
Mat tooler::ipltomat(IplImage &ipl){
        return cvarrToMat(&ipl);
}
tooler::tooler(){
        _mat=   NULL;
        _Ipl=   NULL;
        isAllocatedMat=false;
        isAllocatedIpl=false;
}
 tooler::~tooler(){
        if(isAllocatedMat){
             delete _mat;
        }
        if(isAllocatedIpl){
             delete _Ipl;
        }
 }
