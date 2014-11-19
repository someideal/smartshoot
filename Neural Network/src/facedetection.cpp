#include "../include/facedetection.h"
using namespace cv;
    Mat facedetection::getFace(int index)
     {
                if(index>=0 &&index <catchedNum)
                    {return  reSize(_img_grey(faceRect[index]));}
                else{
                    return porkerFace;
                    }
     }
    DETECT_RESULT    facedetection::run(Mat *input,Mat *output)
    {

            vector<Rect>  faceRectLocal;
            if (input==NULL )
            {
                if(!input->data)
                {   return ERROR;}
                return ERROR;
            }
            switch(mode){
            case SUSPEND_MODE: return ERROR;
            case IMG_MODE: break;
            case VIDEO_MODE:break;
            default:
            ;
            }
            cvtColor(*input,_img_grey,CV_RGB2GRAY);
            equalizeHist(_img_grey,_img_grey);
          /*  imshow("face",_img_grey);
            waitKey(0);//*/
    //double scaleFactor=1.1;int minNeighbors=3;int flags=0;
        if(faceCascade.empty()){
               return ERROR;
            }
            Size minSize(30,30) ;Size maxSize(3,3);
         //   faceCascade.detectMultiScale(_img_grey,faceRectLocal, 1.1, 2, 0  |CASCADE_SCALE_IMAGE,Size(0, 0) ,Size(1, 1) ,);
         faceCascade.detectMultiScale(_img_grey,faceRectLocal, 1.1, 2, 0  |CASCADE_SCALE_IMAGE ,minSize);
                                 //|CASCADE_FIND_BIGGEST_OBJECT
                                 //|CASCADE_DO_ROUGH_SEARCH
                                 faceRect = faceRectLocal;
            catchedNum=faceRect.size();
            if(catchedNum != 0  )
                {
                    if(output!=NULL)
                    {
                        *output=reSize(_img_grey(faceRect[0]),125);
                    }
                    return CATCHED;
                }
            else{
                    return NONE;
                }
    }
   Mat facedetection::reSize(Mat img,size_t sz)
   {
            Mat output;
            if(img.data  && sz >0  && sz<=2048){
                    resize( img, output, Size(sz, sz), 0, 0, INTER_LINEAR);
                    return output;
            }else{
                    return porkerFace;
            }
   }

     facedetection::facedetection()
     {
        catchedNum=0;
        cascadeFacePath="/root/downloads/opencv-3.0.0-beta/data/haarcascades/haarcascade_frontalface_alt.xml";
        mode=SUSPEND_MODE;
        init();
   }
 facedetection::~facedetection()
    {

    }
    bool   facedetection::init()
    {
     if(!faceCascade.load(cascadeFacePath)){
                mode=SUSPEND_MODE;
                return false;
            }
        else{
               mode=IMG_MODE;
                return true;
        }

    }
