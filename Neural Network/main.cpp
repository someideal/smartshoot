#include"include/facedetection.h"
#include"include/tooler.h"
#include"include/retinex.h"
#include"include/morphologyOperator.h"
using namespace std;
using  namespace cv;
 string win_face="orginal";
 string win_repross="retinex";
int main(void)
{

   facedetection fd;
   tooler tool;
   morphologyOperator morph;
    Mat test_img=imread("s3.jpg"),
              face;
    IplImage test_img_ipl;
    if(!test_img.data)
        {   return -1;}
    double weigthForMSR=1,
                    sigmasForMSR=10;
    namedWindow(win_face);
    namedWindow(win_repross);


   if(!fd.run(&test_img,&face))
            { return -1;}
     imshow(win_face,face);
  test_img_ipl=tool.matoipl(face);
  MultiScaleRetinex(&test_img_ipl,1.0,&weigthForMSR,&sigmasForMSR);
  test_img=tool.ipltomat(test_img_ipl);
   test_img=morph.Open(test_img,2);
  imshow(win_repross,test_img);
    while(true)
    {
        char c=waitKey();
        if(c==27)
            break;
    }




    destroyAllWindows();
    return 0;
    }
