CXX = g++

g++ `pkg-config opencv --cflags` -fPIC 
`pkg-config opencv --libs` -shared -o libaam.so
 AAM_Basic.cpp AAM_MovieAVI.cpp AAM_Shape.cpp AAM_CAM.cpp  AAM_PAW.cpp AAM_TDM.cpp AAM_IC.cpp AAM_PDM.cpp AAM_Util.cpp VJfacedetect.cpp

ar -r libaam.a  AAM_Basic.o AAM_MovieAVI.o AAM_Shape.o AAM_CAM.o  AAM_PAW.o AAM_TDM.o AAM_IC.o AAM_PDM.o AAM_Util.o VJfacedetect.o
 g++ `pkg-config opencv --cflags` -fPIC  `pkg-config opencv --libs` -shared -o  libaam.so AAM_Basic.cpp AAM_MovieAVI.cpp AAM_Shape.cpp AAM_CAM.cpp  AAM_PAW.cpp AAM_TDM.cpp AAM_IC.cpp AAM_PDM.cpp AAM_Util.cpp VJfacedetect.cpp
g++ `pkg-config opencv --cflags` -fPIC  `pkg-config opencv --libs` -o  libaam.o AAM_Basic.cpp AAM_MovieAVI.cpp AAM_Shape.cpp AAM_CAM.cpp  AAM_PAW.cpp AAM_TDM.cpp AAM_IC.cpp AAM_PDM.cpp AAM_Util.cpp VJfacedetect.cpp
