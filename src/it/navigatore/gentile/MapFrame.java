/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.navigatore.gentile;

import PrimaParte.Citta;
import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import static it.navigatore.gentile.Mercator.optimalZoom;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jgrapht.ListenableGraph;
import org.jgrapht.demo.JGraphXAdapterDemo;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultListenableGraph;
/**
 *
 * @author New
 */
public class MapFrame extends javax.swing.JFrame {
    //lists of geoPoint needed for download the maps
    List<PointF> geoPoint = new ArrayList<>();
    
    //lists of mercator geoPoint
    List<PointF> points_xy = new ArrayList();
    
    //list of cities
    List<PrimaParte.Citta> cities = new ArrayList<>();
    
    //weighted graph
    DefaultDirectedWeightedGraph<Citta, DefaultWeightedEdge> graphT = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
            //PrimaParte.First.graphWeighted;
    
    //list of urls for downloading the maps
    List<String> urls = new ArrayList<>(3);
    
    //list of maps needed for the merging
    List<Image> maps = new ArrayList<>(3);
    
    private final int nameAssigned = 0;
    
    //ipotetic screen size
    int mapSizePx = 1280;
    int mapSizePy= 720;
    
    //min max lat long
    double minLat, maxLat, minLong, maxLong = 0;
    
    //max x y
    double max_x, max_y = 0;
    
    JLabel picLabel;
    List<Object> vertexs = new ArrayList<>();
    List<Object> edges = new ArrayList<>();
    
    
    //save pictures of the maps on files 
    private void savePic(Image image,String type,String dst ){ 
        int width = image.getWidth(this); 
        int height = image.getHeight(this);
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics g = bi.getGraphics(); 
        try { 
            g.drawImage(image, 0, 0, null);
            ImageIO.write(bi,type,new File(dst)); 
        } catch (IOException e) {
            System.out.println("Exception saving picture on file.." + e.toString());
        } 
    }
    
    //copy the fields of the first program into the current one
    private void importingFields(){
        try {
            PrimaParte.First.main(new String[10]); //run the first program so it fill the city arrays
            
        } catch (IOException | ParseException ex) {
            Logger.getLogger(MapFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //move the array to a newone and add geoPoint to a list of geoPoint
        PrimaParte.First.cities.stream().map((city) -> {
            geoPoint.add(new PointF(city.getLat(), city.getLon()));
            return city;
        }).forEachOrdered((city) -> {
            cities.add(city);
        });
    }
    
    //set proxy settings
    private void setProxy(){
        
        System.setProperty("https.proxyHost", "proxy.intranet");
        System.setProperty("https.proxyPort", "3128");
    }
    
    //find  MIN_LAT, MAX_LAT, MIN_LNG, MAX_LNG
    private void calculateGeoPoints(){
        List<Double> latList = new ArrayList<>();
        List<Double> lonList = new ArrayList<>();
        cities.stream().map((city) -> {
            latList.add(city.getLat());
            return city;
        }).forEachOrdered((city) -> {
            lonList.add(city.getLon());
        });
        
        minLat = Collections.min(latList);
        maxLat = Collections.max(latList);
        minLong = Collections.min(lonList);
        maxLong = Collections.max(lonList);
        
        System.out.println(minLat + " " + maxLat + " " + minLong + " " + maxLong);
    }
    
    //calculate max_x max_y
    private void calculateMercators(){
        Mercator gmap = new Mercator();
        //convert from geo coordinates to mercator coordinates
        cities.forEach((city) -> {
            points_xy.add(gmap.fromLatLngToPoint(city.getLat(), city.getLon()));
        });
        
        List<Double> xList = new ArrayList<>();
        List<Double> yList = new ArrayList<>();
        
        points_xy.stream().map((p) -> {
            xList.add(p.x);
            return p;
        }).forEachOrdered((p) -> {
            yList.add(p.y);
        });
        
        max_x = Collections.max(xList);
        max_y = Collections.max(yList);
        
    }
    
    //download maps form google maps
    private void downloadMaps(){
        System.out.println(mapSizePx + " " + mapSizePy);
        int bestZoom = optimalZoom(mapSizePx, mapSizePy, minLat, maxLat, minLong, maxLong);
        
        List<PointF> somePoints = DownloaderMaps.get4SubCenters(mapSizePx, mapSizePy, minLat, maxLat, minLong, maxLong, bestZoom);
        
        somePoints.forEach((somePoint) -> {
            urls.add(Mercator.urlBuilder(somePoint.x, somePoint.y, bestZoom, 1, mapSizePx/2, mapSizePy/2, "roadmap", "png"));
        });
        
        urls.forEach((url) -> {
            maps.add(DownloaderMaps.getImageFromGoogleMaps(url));
            System.out.println(url);
        });
        
        
        maps.forEach((map) -> {
            savePic(map, String.valueOf(nameAssigned), "user.dir");
        });
        
        
        DownloaderMaps.save4MergedImagesToFile(maps, new File("Mappa.png"));
        System.out.println("Scaricato!");
    }
    
    //attach map to jpanel
    private void attach() throws IOException{
        BufferedImage pic = ImageIO.read(new File(System.getProperty("user.dir") + "\\Mappa.png"));
        picLabel = new JLabel(new ImageIcon(pic));
        add(picLabel);
    }
    
    private void drawPaths(){
        JGraphXAdapter applet = new JGraphXAdapter<>(new DefaultListenableGraph(graphT));
        
        mxGraphComponent mx = new mxGraphComponent(applet);
        List<mxGeometry> geometries = new ArrayList<>();
        mxCell[] vertex_mxs = (mxCell[]) applet.getChildVertices((mxCell)applet.getDefaultParent());
        for (PointF pointF : points_xy) {
            geometries.add(new mxGeometry(pointF.x, pointF.y, 10, 10));
        }
        applet.getModel().beginUpdate();
        try{
            int count = 0;
            for (mxCell cell : vertex_mxs) {
                mxGeometry g = (mxGeometry)cell.getGeometry().clone();
                cell.setGeometry(geometries.get(count));
                count++;
            }
            int counter = 0;
            for (mxCell cell : (mxCell[])applet.getChildVertices(applet.getDefaultParent())) {
                cell.setGeometry(vertex_mxs[counter].getGeometry());
                counter++;
            }
        }finally{
            applet.getModel().endUpdate();
        }
        
        mxCircleLayout layout = new mxCircleLayout(applet);
        layout.execute(applet.getDefaultParent());
        
        this.add(mx);
        //this.pack();
    }
    
    public MapFrame() throws IOException {
        initComponents();
        importingFields();
        graphT = PrimaParte.First.graphWeighted;
        Dimension DimMax = Toolkit.getDefaultToolkit().getScreenSize();
        JPanel mapContent = new JPanel();
        this.setMaximumSize(DimMax);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setLayout(new BorderLayout());
        this.pack();
        this.add(mapContent, BorderLayout.CENTER);
        
        
        //setProxy(); 
        calculateGeoPoints();
        //downloadMaps();    // already downloaded, for download again, remove comments 
        attach(); //paste maps to jpanel
        
        calculateMercators();
     
        drawPaths();
    }
  
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(153, 255, 204));
        setForeground(new java.awt.Color(0, 153, 255));
        setUndecorated(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 451, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]){
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MapFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MapFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MapFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MapFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run(){
                try {
                    new MapFrame().setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(MapFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
