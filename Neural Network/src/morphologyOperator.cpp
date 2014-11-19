#include "morphologyOperator.h"

  Mat morphologyOperator::Open(Mat &input,int sz,MORPH_ELEM_TYPE elem)
  {
        Mat output;
        if (!input.data || sz<0 || elem<0)
            { return porkerMorph;}
        Mat element = getStructuringElement( elem, Size( 2*sz + 1, 2*sz+1 ), Point( sz, sz ) );
        morphologyEx( input, output, MORPH_OPEN, element );
        return output;
  }
   Mat    morphologyOperator:: Close(Mat &input,int sz,MORPH_ELEM_TYPE elem)
   {
        Mat output;
        if (!input.data || sz>=0 || elem>=0)
            { return porkerMorph;}
        Mat element = getStructuringElement( elem, Size( 2*sz + 1, 2*sz+1 ), Point( sz, sz ) );
        morphologyEx( input, output, MORPH_CLOSE, element );
        return output;
   }
  Mat     morphologyOperator:: Gradient(Mat &input,int sz,MORPH_ELEM_TYPE elem)
  {
        Mat output;
        if (!input.data || sz>=0 || elem>=0)
            { return porkerMorph;}
        Mat element = getStructuringElement( elem, Size( 2*sz + 1, 2*sz+1 ), Point( sz, sz ) );
        morphologyEx( input, output, MORPH_GRADIENT, element );
        return output;
  }
  Mat     morphologyOperator:: Tophat(Mat &input,int sz,MORPH_ELEM_TYPE elem)
  {
        Mat output;
        if (!input.data || sz>=0 || elem>=0)
            { return porkerMorph;}
        Mat element = getStructuringElement( elem, Size( 2*sz + 1, 2*sz+1 ), Point( sz, sz ) );
        morphologyEx( input, output, MORPH_TOPHAT, element );
        return output;
  }
 Mat       morphologyOperator::Blackhat(Mat &input,int sz,MORPH_ELEM_TYPE elem)
 {
        Mat output;
        if (!input.data || sz>=0 || elem>=0)
            { return porkerMorph;}
        Mat element = getStructuringElement( elem, Size( 2*sz + 1, 2*sz+1 ), Point( sz, sz ) );
        morphologyEx( input, output, MORPH_BLACKHAT, element );
        return output;
 }

morphologyOperator::morphologyOperator()
{
    //ctor
}

morphologyOperator::~morphologyOperator()
{
    //dtor
}


