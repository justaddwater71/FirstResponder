package edu.nps.FirstResponder;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class AOIMapOverlay extends Overlay {

	private MapActivity parent;
	private ShapeDrawable mDrawable;
	private GeoPoint pt1;
	private GeoPoint pt2;
	private int displayWidth;
	private int displayHeight;
	private boolean isSelectAOI = false;

	public AOIMapOverlay(MapActivity aParent) {// , GeoPoint geoPoint1,
		// GeoPoint geoPoint2) {
		super();
		parent = aParent;
		// pt1 = geoPoint1;
		// pt2 = geoPoint2;
		mDrawable = new ShapeDrawable(new RectShape());
		mDrawable.getPaint().setColor(Color.BLUE);
		// Display display = parent.getWindowManager().getDefaultDisplay();
		//
		// displayWidth = display.getWidth();
		// displayHeight = display.getHeight();
	}

	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
			long when) {
		super.draw(canvas, mapView, shadow);
		// mDrawable.draw(canvas);
		Point point1 = new Point();
		Point point2 = new Point();

		if (((MapsActivity) parent).isAoiSelection()) {
			displayWidth = mapView.getWidth();
			displayHeight = mapView.getHeight();

			int line1_x1 = (displayWidth / 3) * 1;
			int line1_y1 = 0;
			int line1_x2 = (displayWidth / 3) * 1;
			int line1_y2 = displayHeight;

			int line2_x1 = (displayWidth / 3) * 2;
			int line2_y1 = 0;
			int line2_x2 = (displayWidth / 3) * 2;
			int line2_y2 = displayHeight;

			int line3_x1 = 0;
			int line3_y1 = (displayHeight / 3) * 1;
			int line3_x2 = displayWidth;
			int line3_y2 = (displayHeight / 3) * 1;

			int line4_x1 = 0;
			int line4_y1 = (displayHeight / 3) * 2;
			int line4_x2 = displayWidth;
			int line4_y2 = (displayHeight / 3) * 2;

			Paint paint = new Paint();
			paint.setColor(Color.BLACK);
			canvas.drawLine(line1_x1, line1_y1, line1_x2, line1_y2, paint);
			canvas.drawLine(line2_x1, line2_y1, line2_x2, line2_y2, paint);
			canvas.drawLine(line3_x1, line3_y1, line3_x2, line3_y2, paint);
			canvas.drawLine(line4_x1, line4_y1, line4_x2, line4_y2, paint);
		}
		// mapView.getProjection().toPixels(pt1, point1);
		// mapView.getProjection().toPixels(pt2, point2);
		// Toast.makeText(parent.getBaseContext(),
		// point1.x + "," + point1.y + "," +
		// point2.x + "," + point2.y ,
		// Toast.LENGTH_SHORT).show();
		// mDrawable.setBounds(point1.x, point1.y, point2.x, point2.y);
		// mDrawable.draw(canvas);
		mapView.invalidate();
		return true;
	}

	public void determinePoints(MapView mapView, int x, int y, Point pt1,
			Point pt2) {
		displayWidth = mapView.getWidth();
		displayHeight = mapView.getHeight();

		if ((x >= 0 && x <= (displayWidth / 3))
				&& (y >= 0 && y <= (displayHeight / 3))) {
			pt1.x = 0;
			pt1.y = 0;
			pt2.x = (displayWidth / 3);
			pt2.y = (displayHeight / 3);
		} else if ((x > (displayWidth / 3) && x <= (displayWidth * 2 / 3))
				&& (y >= 0 && y <= (displayHeight / 3))) {
			pt1.x = (displayWidth / 3);
			pt1.y = 0;
			pt2.x = (displayWidth * 2 / 3);
			pt2.y = (displayHeight / 3);
		} else if ((x > (displayWidth * 2 / 3) && x <= (displayWidth))
				&& (y >= 0 && y <= (displayHeight / 3))) {
			pt1.x = (displayWidth * 2 / 3);
			pt1.y = 0;
			pt2.x = (displayWidth);
			pt2.y = (displayHeight / 3);
		} else if ((x >= 0 && x <= (displayWidth / 3))
				&& (y > (displayHeight / 3) && y <= (displayHeight * 2 / 3))) {
			pt1.x = 0;
			pt1.y = (displayHeight / 3);
			pt2.x = (displayWidth / 3);
			pt2.y = (displayHeight * 2 / 3);
		} else if ((x > (displayWidth / 3) && x <= (displayWidth * 2 / 3))
				&& (y > (displayHeight / 3) && y <= (displayHeight * 2 / 3))) {
			pt1.x = (displayWidth / 3);
			pt1.y = (displayHeight / 3);
			pt2.x = (displayWidth * 2 / 3);
			pt2.y = (displayHeight * 2 / 3);
		} else if ((x > (displayWidth * 2 / 3) && x <= (displayWidth))
				&& (y > (displayHeight / 3) && y <= (displayHeight * 2 / 3))) {
			pt1.x = (displayWidth * 2 / 3);
			pt1.y = (displayHeight / 3);
			pt2.x = (displayWidth);
			pt2.y = (displayHeight * 2 / 3);
		} else if ((x >= 0 && x <= (displayWidth / 3))
				&& (y > (displayHeight * 2 / 3) && y <= (displayHeight))) {
			pt1.x = 0;
			pt1.y = (displayHeight * 2 / 3);
			pt2.x = (displayWidth / 3);
			pt2.y = (displayHeight);
		} else if ((x > (displayWidth / 3) && x <= (displayWidth * 2 / 3))
				&& (y > (displayHeight * 2 / 3) && y <= (displayHeight))) {
			pt1.x = (displayWidth / 3);
			pt1.y = (displayHeight * 2 / 3);
			pt2.x = (displayWidth * 2 / 3);
			pt2.y = (displayHeight);
		} else if ((x > (displayWidth * 2 / 3) && x <= (displayWidth))
				&& (y > (displayHeight * 2 / 3) && y <= (displayHeight))) {
			pt1.x = (displayWidth * 2 / 3);
			pt1.y = (displayHeight * 2 / 3);
			pt2.x = (displayWidth);
			pt2.y = (displayHeight);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {

		int posX = 0;
		int posY = 0;

		if (((MapsActivity) parent).isAoiSelection()) {
			posX = (int) event.getX();
			posY = (int) event.getY();
			Point pt1 = new Point();
			Point pt2 = new Point();
			determinePoints(mapView, posX, posY, pt1, pt2);
			GeoPoint geoPt1 = mapView.getProjection().fromPixels(pt1.x, pt1.y);
			GeoPoint geoPt2 = mapView.getProjection().fromPixels(pt2.x, pt2.y);
			
			AreaOfInterest aoi = new AreaOfInterest(geoPt1, geoPt2);
			((MapsActivity) parent).addToAOIList(aoi);
			
			Toast.makeText(parent.getBaseContext(), "Area Of Interest Added", Toast.LENGTH_SHORT).show();

			// Uncomment to debug
//			GeoPoint clickedPt = mapView.getProjection().fromPixels(posX, posY);
//			Toast.makeText(
//					parent.getBaseContext(),
//					"Clicked : " + clickedPt.getLatitudeE6() / 1E6 + ","
//							+ clickedPt.getLongitudeE6() / 1E6 + "\nPoint 1 : "
//							+ geoPt1.getLatitudeE6() / 1E6 + ","
//							+ geoPt1.getLongitudeE6() / 1E6 + "\nPoint 2 : "
//							+ geoPt2.getLatitudeE6() / 1E6 + ", "
//							+ geoPt2.getLongitudeE6() / 1E6, Toast.LENGTH_LONG)
//					.show();

			((MapsActivity) parent).disableAoiSelection();
		}

		// int iPosX = 0;
		// int iPosY = 0;
		// int fPosX = 0;
		// int fPosY = 0;
		// GeoPoint iPoint;
		// if (event.getAction() == MotionEvent.ACTION_DOWN) {
		// // mDrawable.
		// iPosX = (int) event.getX();
		// iPosY = (int) event.getY();
		// iPoint = mapView.getProjection().fromPixels((int) event.getX(),
		// (int) event.getY());
		// }
		//
		// // ---when user lifts his finger---
		// if (event.getAction() == MotionEvent.ACTION_UP) {
		// fPosX = (int) event.getX();
		// fPosY = (int) event.getY();
		// mDrawable.setBounds(iPosX, iPosY, fPosX, fPosY);
		// GeoPoint p = mapView.getProjection().fromPixels((int) event.getX(),
		// (int) event.getY());
		// Toast.makeText(
		// parent.getBaseContext(),
		// p.getLatitudeE6() / 1E6 + "," + p.getLongitudeE6() / 1E6
		// + ", X : " + fPosX + ", Y : " + fPosY,
		// Toast.LENGTH_LONG).show();
		// }
		return false;
	}

	public void setParent(MapActivity aParent) {
		parent = aParent;
	}

}
