<img align="right" src="https://lh6.ggpht.com/kO9hmdBhFFul8IuZUOpZJM96X3hbfbkH-61UK340p4riB4Ay54NuqagC10FB2uHP3dI=w300-rw"/>

Advanced-QR-Code-reader
=======================

Advanced QR Code reader is aplication for detection and decoding QR codes using devices with the Android Operating System. 

For preprocessing of the visual field camera is used OpenCV library. Designed preprocessing string searches a perspective transforms possible QR codes. This is suppressed a shape distortion QR code that would otherwise reduce the successful decoding. At the same time the preprocessing string applied unsharp mask, which reduces the effect of motion blur image caused by the camera motion. Thus preprocessing scene is finally decoded using ZXing library. Described manner it is possible to achieve a higher success rate detection and decoding of QR codes than other conventional QR readers.
