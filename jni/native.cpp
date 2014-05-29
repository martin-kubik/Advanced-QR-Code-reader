#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <android/log.h>
#include <vector>

using namespace std;
using namespace cv;

int zeroPixels;
Scalar avgPixelIntensity;
int s;
int s2;

extern "C" {

double angle(Point pt1, Point pt2, Point pt0);
int count(Mat mGr);
int count2(Mat mGr);

JNIEXPORT void JNICALL Java_cz_martinkubik_qrreader_MainActivity_callNative(
		JNIEnv*,
		jobject,

		jboolean doRgba,
		jboolean doGray,
		jboolean doThresh,
		jboolean doAdaptOnly,
		jboolean doOtsuOnly,
		jboolean doErode,
		jboolean doFindContours,
		jboolean doFindSquares,
		jboolean doPersTrans,
		jboolean doQRreader,

		jboolean endWithRgba,
		jboolean endWithGray,
		//jboolean endWithThresh,
		jboolean endWithAdaptOnly,
		jboolean endWithOtsuOnly,
		jboolean endWithErode,
		jboolean endWithContours,
		jboolean endWithSquares,

		jlong addrRgba,
		jlong addrGray,

		jlong addrQr_gray2

)
{
	Mat& mRgb = *(Mat*)addrRgba;
	Mat& mGr  = *(Mat*)addrGray;
	Mat& qr_gray2 = *(Mat*)addrQr_gray2;



	int a, b, c, d, e, f, g, h;
	vector<Point> approx;
	count(mGr);

	if (doRgba == true) {


		if (doGray == true && endWithGray == true) {

			cvtColor(mGr, mRgb, CV_GRAY2RGBA);

		}

		if (doThresh == true) {

			Mat mGrAfterThresh;

			if (doAdaptOnly == true) {
				adaptiveThreshold(mGr, mGrAfterThresh, 255, CV_ADAPTIVE_THRESH_MEAN_C, CV_THRESH_BINARY,75,10); //75,10);
				cvtColor(mGrAfterThresh, mRgb, CV_GRAY2RGBA);
			}

			if (doOtsuOnly == true) {
				threshold(mGr, mGrAfterThresh, 0, 255, CV_THRESH_BINARY | CV_THRESH_OTSU) ;
				cvtColor(mGrAfterThresh, mRgb, CV_GRAY2RGBA);
			}

			if (doAdaptOnly == false &&  doOtsuOnly == false) {

				if (s < 128 ) {
					adaptiveThreshold(mGr, mGrAfterThresh, 255, CV_ADAPTIVE_THRESH_MEAN_C, CV_THRESH_BINARY,75,10); //75,10);
				} else {
					threshold(mGr, mGrAfterThresh, 0, 255, CV_THRESH_BINARY | CV_THRESH_OTSU) ;
				}
			}



			if (doErode == true) {

				Mat mGrAfterErode;
				int erodeIterations  = 7;

				Mat element = getStructuringElement(cv::MORPH_RECT, //Mat element; // – structuring element used for erosion; if element = Mat(), a 3 x 3 rectangular structuring element is used.
						cv::Size(3, 3),
						cv::Point(-1, -1));

				Point anchor = Point(-1, -1);  //– position of the anchor within the element; default value(-1, -1) means that the anchor is at the element center.

				erode( mGrAfterThresh, mGrAfterErode, element, anchor, erodeIterations);

				if (endWithErode == true) {

					cvtColor(mGrAfterErode, mRgb, CV_GRAY2RGBA);
				}



				if (doFindContours == true) {
					vector<vector<Point> > contours;
					findContours(mGrAfterErode, contours, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);

					if (endWithContours == true) {

						cvtColor(mGrAfterErode, mRgb, CV_GRAY2RGBA);
					}

					if (doFindSquares == true) {



						// test each contour
						for( size_t i = 0; i < contours.size(); i++ )
						{
							// approximate contour with accuracy proportional
							// to the contour perimeter
							approxPolyDP(Mat(contours[i]), approx, arcLength(Mat(contours[i]), true)*0.03, true);



							// square contours should have 4 vertices after approximation
							// relatively large area (to filter out noisy contours)
							// and be convex.
							// Note: absolute value of an area is used because
							// area may be positive or negative - in accordance with the
							// contour orientation

							//filter of rectangles
							/*Rect r = cv::boundingRect(contours[i]);
								        					double ratio = std::abs(1 - (double)r.width / r.height);*/

							if( approx.size() == 4 &&
									fabs(contourArea(Mat(approx))) > 4000 &&   // area < 4000 is too small
									// fabs(contourArea(Mat(approx))) < 38400 &&  //480x320 = 153 600 /4 = 38400... filter too big squares for resolution 480x320
									fabs(contourArea(Mat(approx))) < 96000 &&// filter too big squares for resolution 800x480
									// ratio <= 2 &&  //filter of rectangles
									isContourConvex(Mat(approx)) )
							{
								double maxCosine = 0;

								for( int j = 2; j < 5; j++ )
								{
									// find the maximum cosine of the angle between joint edges
									double cosine = fabs(angle(approx[j%4], approx[j-2], approx[j-1]));
									maxCosine = MAX(maxCosine, cosine);
								}

								// if cosines of all angles are small
								// (all angles are ~90 degree) then write quandrange
								// vertices to resultant sequence
								if( maxCosine < 0.5 )
								{

									// The first corner

									a = (int) (approx[0].x);
									b = (int) (approx[0].y);

									// The second corner

									c = (int) (approx[1].x);
									d = (int) (approx[1].y);

									// The third corner

									e = (int) (approx[2].x);
									f = (int) (approx[2].y);

									// The fourth corner

									g = (int) (approx[3].x);
									h = (int) (approx[3].y);


									line(mRgb, approx[0], approx[1], Scalar(0,255,0,255), 2, 4, 0);
									line(mRgb, approx[1], approx[2], Scalar(0,255,0,255), 2, 4, 0);
									line(mRgb, approx[2], approx[3], Scalar(0,255,0,255), 2, 4, 0);
									line(mRgb, approx[3], approx[0], Scalar(0,255,0,255), 2, 4, 0);

									//draw corner points
									circle(mRgb, Point(a,b), 3, Scalar(255,0,0,255), 2);
									circle(mRgb, Point(c,d), 3, Scalar(0,255,0,255), 2);
									circle(mRgb, Point(e,f), 3, Scalar(0,0,255,255), 2);
									circle(mRgb, Point(g,h), 3, Scalar(255,255,0,255), 2);

								}

							}


						}



					}


					if (doQRreader == true) {


						cv::Point2f src[4], dst[4];
						src[0].x = a;
						src[0].y = b;
						src[1].x = c;
						src[1].y = d;
						src[2].x = e;
						src[2].y = f;
						src[3].x = g;
						src[3].y = h;

						dst[0].x = 10;
						dst[0].y = 10;
						dst[1].x = 190;
						dst[1].y = 10;
						dst[2].x = 190;
						dst[2].y = 190;
						dst[3].x = 10;
						dst[3].y = 190;


						Mat qr2(190, 190, CV_8UC1);
						Mat qr_with_border2 (210, 210, CV_8UC1, 255);

						warpPerspective(mGr, qr2, getPerspectiveTransform(src, dst), cvSize(200, 200)); // INTER_LINEAR | WARP_INVERSE_MAP, BORDER_TRANSPARENT

						count2(qr2);
						if (s2 > 80 && s2 < 180 ) {

							Rect rect_for_QR(5, 5, 200, 200);
							Rect rect_for_Zxing(0, 0, 200, 200);
							Rect rect_for_QR_with_border2(0, 0, 210, 210);


						/*	Mat tmp;
							GaussianBlur(qr2, tmp, cv::Size(0,0), 3);
							addWeighted(qr2, 1.5, tmp, -0.9, 0, qr2);*/
							//Source: http://opencv-help.blogspot.cz/2013/01/how-to-sharpen-image-using-opencv.html
							//          http://opencv-code.com/quick-tips/sharpen-image-with-unsharp-mask/

							qr2.copyTo(qr_gray2(rect_for_Zxing));
							qr2.copyTo(qr_with_border2(rect_for_QR));
							cvtColor(qr_with_border2, qr_with_border2, CV_GRAY2RGBA);
							qr_with_border2.copyTo(mRgb(rect_for_QR_with_border2));


						}


					}

					if (doPersTrans == true){

						cv::Point2f src[4], dst[4];
												src[0].x = a;
												src[0].y = b;
												src[1].x = c;
												src[1].y = d;
												src[2].x = e;
												src[2].y = f;
												src[3].x = g;
												src[3].y = h;

												dst[0].x = 10;
												dst[0].y = 10;
												dst[1].x = 450;
												dst[1].y = 10;
												dst[2].x = 450;
												dst[2].y = 450;
												dst[3].x = 10;
												dst[3].y = 450;

												Mat qrPT (460, 460, CV_8UC1);
												Mat qrPT_with_border(480, 480, CV_8UC1, 255);

												warpPerspective(mGr, qrPT, getPerspectiveTransform(src, dst), cvSize(460, 460)); // INTER_LINEAR | WARP_INVERSE_MAP, BORDER_TRANSPARENT

																			Rect rect_for_QR_PT(10, 10, 460, 460);
																			Rect rect_for_QR_PT_with_border(0, 0, 480, 480);

																			qrPT.copyTo(qrPT_with_border(rect_for_QR_PT));

																			cvtColor(qrPT_with_border, qrPT_with_border, CV_GRAY2RGBA);
																			qrPT_with_border.copyTo(mRgb(rect_for_QR_PT_with_border));




				}
				}




			}

		}

	}





}



int count(Mat mGr){

	avgPixelIntensity = cv::mean( mGr );
	s = sum(avgPixelIntensity)[0];

	return s;
}

int count2(Mat mGr){

	avgPixelIntensity = cv::mean( mGr );
	s2 = sum(avgPixelIntensity)[0];

	return s2;
}

JNIEXPORT jint JNICALL Java_cz_martinkubik_qrreader_MainActivity_getMessageFromNative(
		JNIEnv *env,
		jobject callingObject)
{

	return s;
}

}

// finds a cosine of angle between vectors
// from pt0->pt1 and from pt0->pt2
double angle(Point pt1, Point pt2, Point pt0) {
	double dx1 = pt1.x - pt0.x;
	double dy1 = pt1.y - pt0.y;
	double dx2 = pt2.x - pt0.x;
	double dy2 = pt2.y - pt0.y;
	return (dx1 * dx2 + dy1 * dy2)
			/ sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
}

