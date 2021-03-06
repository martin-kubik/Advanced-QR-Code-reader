<img align="right" src="http://martinkubik.cz/DP/logo.png"/>

Advanced QR Code reader
=======================

Advanced QR Code reader is aplication for detection and decoding QR codes using devices with the Android Operating System. 

For preprocessing of the visual field camera is used OpenCV library. Designed preprocessing string searches a perspective transforms possible QR codes. This is suppressed a shape distortion QR code that would otherwise reduce the successful decoding. At the same time the preprocessing string applied unsharp mask, which reduces the effect of motion blur image caused by the camera motion. Thus preprocessing scene is finally decoded using ZXing library. Described manner it is possible to achieve a higher success rate detection and decoding of QR codes than other conventional QR readers.

<img align="centre" src="http://martinkubik.cz/DP/screenshot.png"/>

Advanced QR Code is available at Google Play:
[![Download at Google Play!](http://developer.android.com/images/brand/en_generic_rgb_wo_60.png)](https://play.google.com/store/apps/details?id=cz.martinkubik.qrreader)

