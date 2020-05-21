package it.navigatore.gentile;

/**
 * Created by Gennaro on 16/11/2015.
 */
import static it.navigatore.gentile.Mercator.fromDeltaPixelToDeltaMercator;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Gennaro
 */
public class DownloaderMaps {

    static public Image getImageFromGoogleMaps(String imageUrl) {

        URL url;
        Image image = null;
        try {
            url = new URL(imageUrl);
            //per ottenre una versione scalata appendere .getScaledInstance(640, 640, java.awt.Image.SCALE_SMOOTH)
            image = new ImageIcon(url).getImage();
        } catch (MalformedURLException ex) {
            Logger.getLogger(DownloaderMaps.class.getName()).log(Level.SEVERE, null, ex);
        }
        return image;
    }

    static public void saveImageToFile(Image img, File f) {

        try {
            BufferedImage bi;
            bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = bi.createGraphics();
            g2.drawImage(img, 0, 0, null);
            g2.dispose();
            ImageIO.write(bi, "png", f);
        } catch (IOException ex) {
            Logger.getLogger(DownloaderMaps.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    static List<PointF> get4SubCenters(int mapSizePx, int mapSizePy, double minLat, double maxLat, double minLng, double maxLng, int zoom){
        //define map center geodetic coordinates:
        PointF geodeticC = new PointF((minLat + maxLat)/2.0,(minLng+maxLng)/2.0);

        Mercator g = new Mercator();
        //define map center world coordinates:
        PointF mercatorC = g.fromLatLngToPoint(geodeticC.x,geodeticC.y);
        //let's define the world coordinates of the 4 centres of the 4 quadrants
        PointF mercatorCNE =new PointF( mercatorC.x + Mercator.fromDeltaPixelToDeltaMercator(mapSizePx / 4, zoom),  mercatorC.y - fromDeltaPixelToDeltaMercator(mapSizePy / 4, zoom));
        PointF mercatorCSE =new PointF( mercatorC.x + Mercator.fromDeltaPixelToDeltaMercator(mapSizePx / 4, zoom),  mercatorC.y + fromDeltaPixelToDeltaMercator(mapSizePy / 4, zoom));
        PointF mercatorCSW =new PointF( mercatorC.x - Mercator.fromDeltaPixelToDeltaMercator(mapSizePx / 4, zoom),  mercatorC.y + fromDeltaPixelToDeltaMercator(mapSizePy / 4, zoom));
        PointF mercatorCNW =new PointF( mercatorC.x - Mercator.fromDeltaPixelToDeltaMercator(mapSizePx / 4, zoom),  mercatorC.y - fromDeltaPixelToDeltaMercator(mapSizePy / 4, zoom));
        //let's calculate corresponding geodetic coordinates
        List<PointF> centres = new ArrayList<>();
        //the order is important!
        centres.add(g.fromPointToLatLng(mercatorCNW));
        centres.add(g.fromPointToLatLng(mercatorCNE));
        centres.add(g.fromPointToLatLng(mercatorCSW));
        centres.add(g.fromPointToLatLng(mercatorCSE));
        return centres;
    }
    
    static public void  save4MergedImagesToFile(List<Image> images, File f4){
        int rows = 2;   //we assume the no. of rows and cols are known and each chunk has equal width and height
        int cols = 2;
        int chunks = rows * cols;
        int chunkWidth, chunkHeight;
        int type;

        type = BufferedImage.TYPE_INT_RGB;
        chunkWidth = images.get(0).getWidth(null);
        chunkHeight = images.get(0).getHeight(null);
        //Initializing the final image
        BufferedImage finalImg = new BufferedImage(chunkWidth*cols, chunkHeight*rows, type);
        int num = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                finalImg.createGraphics().drawImage(images.get(num), chunkWidth * j, chunkHeight * i, null);
                num++;
            }
        }

        try {

            ImageIO.write(finalImg, "png", f4);
        } catch (IOException ex) {
            Logger.getLogger(DownloaderMaps.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * It retrieves a list of Images by invoking Google Maps API with the provided list of url strings
     * @param urlStrings the list of urls corresponding to Google Maps API images
     * @return a list of Images
     */
    static public List<Image> getImageListFromGoogleMaps(List<String> urlStrings){
        URL url;
        List<Image> images= new ArrayList<>();
        try {
            for(String imageUrl: urlStrings) {
                url = new URL(imageUrl);
                images.add(new ImageIcon(url).getImage());
            }
        } catch (MalformedURLException ex) {
            
        }
        return images;
    }

    public static void main(String[] args) {
        System.setProperty("https.proxyHost", "proxy.intranet");
        System.setProperty("https.proxyPort", "3128");
        String url = "https://maps.googleapis.com/maps/api/staticmap?center=45.7125463,9.308430300000055&zoom=12&scale=1&size=640x640&maptype=roadmap&format=png";
        File f = new File("saved.png");
        saveImageToFile(getImageFromGoogleMaps(url), f);
    }
}
