package com.example;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import com.google.android.maps.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MyActivity extends MapActivity {
    private MapView map;
    InterestingLocation interestingLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Drawable d = getResources().getDrawable(android.R.drawable.star_on);
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        interestingLocation = new InterestingLocation(d);
        map = (MapView) findViewById(R.id.map_id);
        map.setBuiltInZoomControls(true);
        map.getOverlays().add(interestingLocation);
        MyLocationOverlay whereAmI = new MyLocationOverlay(this, map);
        whereAmI.enableMyLocation();
        map.getOverlays().add(whereAmI);
        GeoPoint geoPoint = interestingLocation.getCenter();
        map.getController().setCenter(geoPoint);
        map.getController().setZoom(15);

    }

    public void onClick(View v) {
        GeoPoint gp = map.getMapCenter();
        interestingLocation.add(new OverlayItem(gp, "Fuck", "fuck " + Math.random()));
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    class InterestingLocation extends ItemizedOverlay {
        private List<OverlayItem> location = new ArrayList<OverlayItem>();
        private Drawable marker;

        public InterestingLocation(Drawable d) {
            super(d);
            marker = d;
            GeoPoint gp = new GeoPoint((int) (50.55358 * 1000000), (int) (30.510406 * 1000000));
            location.add(new OverlayItem(gp, "Fuck", "fuck " + Math.random()));
            populate();
        }

        @Override
        protected OverlayItem createItem(int i) {
            return location.get(i);
        }

        @Override
        public int size() {
            return location.size();
        }

        public void add(OverlayItem oi) {
            location.add(oi);
            populate();
        }

        private List<Point> getPoints() {
            List<Point> ls = new ArrayList<Point>();
            Projection projection = map.getProjection();
            for (OverlayItem oi : location)
                ls.add(projection.toPixels(oi.getPoint(), new Point()));
            return ls.size() > 3 ? orderedPoints(ls) : ls;
        }

        private List<Point> orderedPoints(List<Point> ls) {
            Point first = Collections.min(ls, new Comparator<Point>() {
                @Override
                public int compare(Point o1, Point o2) {
                    if (o1.x < o2.x) return -1;
                    if (o1.x == o2.y && o1.y < o2.y) return -1;
                    return Integer.valueOf(o1.y).compareTo(o2.y);
                }
            });
            List<Point> res = new ArrayList<Point>();
            res.add(first);
            Point current = null;
            Point prev = first;
            while (current != first) {
                for (Point pt : ls)
                    if (pt != prev) current = pt;

                for (Point pt : ls)
                    if (pt != prev && pt != current) if ((current.x - prev.x) * (pt.y - prev.y) - (current.y - prev.y) * (pt.x - prev.x) > 0) current = pt;
                res.add(current);
                prev = current;
            }
            return res;
        }

        @Override
        public boolean draw(Canvas canvas, MapView mapView, boolean b, long l) {
            boolean r = super.draw(canvas, mapView, b, l);
            Paint p = new Paint();
            p.setAntiAlias(true);
            p.setColor(0x6600ff00);
            canvas.drawCircle(mapView.getWidth() / 2, mapView.getHeight() / 2, 10f, p);
            Path path = new Path();
            List<Point> ls = getPoints();

            path.moveTo(ls.get(0).x, ls.get(0).y);
            for (Point pt : ls)
                path.lineTo(pt.x, pt.y);
            canvas.drawPath(path, p);
            boundCenter(marker);
            return r;
        }

        private boolean isPinch = false;

        @Override
        public boolean onTap(GeoPoint p, MapView map) {
            if (isPinch) {
                return false;
            } else {
                if (p != null) {
                    add(new OverlayItem(p, "new", "new"));
                    return true;
                } else {
                    return false;
                }
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent e, MapView mapView) {
            int fingers = e.getPointerCount();
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                isPinch = false;  // Touch DOWN, don't know if it's a pinch yet
            }
            if (e.getAction() == MotionEvent.ACTION_MOVE && fingers == 2) {
                isPinch = true;   // Two fingers, def a pinch
            }
            return super.onTouchEvent(e, mapView);
        }
    }
}
