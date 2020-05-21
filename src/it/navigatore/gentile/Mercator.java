package it.navigatore.gentile;

import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

/**
 *
 * @author Gennaro
 */
public final class Mercator{
    private final int TILE_SIZE = 256;
    private final PointF _pixelOrigin;
    private final double _pixelsPerLonDegree;
    private final double _pixelsPerLonRadian;

    public Mercator(){
        this._pixelOrigin = new PointF(TILE_SIZE / 2.0,TILE_SIZE / 2.0);
        this._pixelsPerLonDegree = TILE_SIZE / 360.0;
        this._pixelsPerLonRadian = TILE_SIZE / (2 * Math.PI);
    }

    double bound(double val, double valMin, double valMax){
        double res;
        res = Math.max(val, valMin);
        res = Math.min(res, valMax);
        return res;
    }

    double degreesToRadians(double deg){
        return deg * (Math.PI / 180);
    }

    double radiansToDegrees(double rad){
        return rad / (Math.PI / 180);
    }

    PointF fromLatLngToPoint(double lat, double lng){
        PointF point = new PointF(0, 0);

        point.x = _pixelOrigin.x + lng * _pixelsPerLonDegree;

        // Truncating to 0.9999 effectively limits latitude to 89.189. This is
        // about a third of a tile past the edge of the world tile.
        double siny = bound(Math.sin(degreesToRadians(lat)), -0.9999,0.9999);
        point.y = _pixelOrigin.y + 0.5 * Math.log((1 + siny) / (1 - siny)) *- _pixelsPerLonRadian;

        return point;
    }

    PointF fromPointToLatLng(PointF point){
        double lng = (point.x - _pixelOrigin.x) / _pixelsPerLonDegree;
        double latRadians = (point.y - _pixelOrigin.y) / - _pixelsPerLonRadian;
        double lat = radiansToDegrees(2 * Math.atan(Math.exp(latRadians)) - Math.PI / 2);
        return new PointF(lat, lng);
    }
    
    static double fromDeltaPixelToDeltaMercator(double deltaPixel, int zoom){
        int numTiles = 1 << zoom;
        return deltaPixel/numTiles;
    }
    static double fromDeltaMercatorToDeltaPixel(double deltaMercator, int zoom){
        int numTiles = 1 << zoom;
        return deltaMercator*numTiles;
    }

    /**
     * It calculates the best zoom level for a Google Map of a given size mapSizePx * mapSizePy containing specified
     * maximum and minimum values of latitude and longitude.
     * @param mapSizePx the number of pixels of the image map on the x axis
     * @param mapSizePy the number of pixels of the image map on the y axis
     * @param minLat the minimum value of latitude to be contained in the image map
     * @param maxLat the maximum value of latitude to be contained in the image map
     * @param minLng the minimum value of longitude to be contained in the image map
     * @param maxLng the maximum value of latitude to be contained in the image map
     * @return the best (i.e. the maximum) zoom level that allows the image map of mapSizePx * mapSizePy pixel dimension
     * to contain the boundaries defined by the provided latitude and longitude values.
     */
    static int optimalZoom(int mapSizePx, int mapSizePy, double minLat, double maxLat, double minLng, double maxLng){

        //define map center geodetic coordinates:
        PointF geodeticC = new PointF((minLat + maxLat)/2.0,(minLng+maxLng)/2.0);

        //let's calculate world coordinates of corners according to Mercator's projection
        Mercator g = new Mercator();
        PointF mercatorNE = g.fromLatLngToPoint(maxLat,maxLng);
        PointF mercatorSW = g.fromLatLngToPoint(minLat,minLng);

        //define map center world coordinates:
        PointF mercatorC = g.fromLatLngToPoint(geodeticC.x,geodeticC.y);

        //let's calculate pixel coordinates according to a given zoom level, starting from the highest
        int tryZoom=23;//min=0 max = 23 see http://stackoverflow.com/questions/9356724/google-map-api-zoom-range
        int minZoom = 0;
        boolean zoomOK=false;
        while ((!zoomOK)&&(tryZoom>=minZoom)) {
            //let's calculate corners' coordinates starting from the center of the map using map's pixel sizes
            double mercatorMinXOnMap = mercatorC.x - fromDeltaPixelToDeltaMercator(mapSizePx / 2, tryZoom);
            double mercatorMaxXOnMap = mercatorC.x + fromDeltaPixelToDeltaMercator(mapSizePx / 2, tryZoom);
            double mercatorMinYOnMap = mercatorC.y - fromDeltaPixelToDeltaMercator(mapSizePy / 2, tryZoom);
            double mercatorMaxYOnMap = mercatorC.y + fromDeltaPixelToDeltaMercator(mapSizePy / 2, tryZoom);
            //zoom is ok if all corners are inside the map
            zoomOK = ((mercatorMinXOnMap <= mercatorSW.x) &&
                    (mercatorMaxXOnMap >= mercatorNE.x) &&
                    (mercatorMinYOnMap <= mercatorNE.y) &&
                    (mercatorMaxYOnMap >= mercatorSW.y));
            tryZoom--;
        }

        return tryZoom+1;
    }
    
    static public Image getImageFromGoogleMaps(String imageUrl) {

        URL url;
        Image image = null;
        try {
            url = new URL(imageUrl);
            //per ottenere una versione scalata appendere .getScaledInstance(640, 640, java.awt.Image.SCALE_SMOOTH)
            image = new ImageIcon(url).getImage();
        } catch (MalformedURLException ex) {
            
        }
        return image;
    }
    
    static List<PointF> get4SubCenters(int mapSizePx, int mapSizePy, double minLat, double maxLat, double minLng, double maxLng, int zoom){
        //define map center geodetic coordinates:
        PointF geodeticC = new PointF((minLat + maxLat)/2.0,(minLng+maxLng)/2.0);

        Mercator g = new Mercator();
        //define map center world coordinates:
        PointF mercatorC = g.fromLatLngToPoint(geodeticC.x,geodeticC.y);
        //let's define the world coordinates of the 4 centres of the 4 quadrants
        PointF mercatorCNE =new PointF( mercatorC.x + fromDeltaPixelToDeltaMercator(mapSizePx / 4, zoom),  mercatorC.y - fromDeltaPixelToDeltaMercator(mapSizePy / 4, zoom));
        PointF mercatorCSE =new PointF( mercatorC.x + fromDeltaPixelToDeltaMercator(mapSizePx / 4, zoom),  mercatorC.y + fromDeltaPixelToDeltaMercator(mapSizePy / 4, zoom));
        PointF mercatorCSW =new PointF( mercatorC.x - fromDeltaPixelToDeltaMercator(mapSizePx / 4, zoom),  mercatorC.y + fromDeltaPixelToDeltaMercator(mapSizePy / 4, zoom));
        PointF mercatorCNW =new PointF( mercatorC.x - fromDeltaPixelToDeltaMercator(mapSizePx / 4, zoom),  mercatorC.y - fromDeltaPixelToDeltaMercator(mapSizePy / 4, zoom));
        //let's calculate corresponding geodetic coordinates
        List<PointF> centres = new ArrayList<>();
        //the order is important!
        centres.add(g.fromPointToLatLng(mercatorCNW));
        centres.add(g.fromPointToLatLng(mercatorCNE));
        centres.add(g.fromPointToLatLng(mercatorCSW));
        centres.add(g.fromPointToLatLng(mercatorCSE));
        return centres;
    }

    
    static public String urlBuilder(double lat, double lng, int zoom,int scale,int mapSizePx, int mapSizePy, String maptype, String format){
         String url = "https://maps.googleapis.com/maps/api/staticmap?";
         url+="center="+lat+","+lng+"&";
         url+="zoom="+zoom+"&";
         url+="scale="+scale+"&";
         url+="size="+mapSizePx+"x"+mapSizePy+"&";
         url+="maptype="+maptype+"&";
         url+="format="+format;
         return  url;
     }
    
    
}
