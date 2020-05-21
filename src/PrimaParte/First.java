package PrimaParte;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

/**
 *
 * @author Giuseppe
 */


public class First {
    private static final int EARTH_RADIUS = 6371; // Approx Earth radius in KM
    
    public static double distance(double startLat, double startLong, double endLat, double endLong) {
            double dLat = Math.toRadians((endLat - startLat));
            double dLong = Math.toRadians((endLong - startLong));
            startLat = Math.toRadians(startLat);
            endLat = Math.toRadians(endLat);
            double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            return EARTH_RADIUS * c; // <-- d
    }
    
    public static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
    

    private static final NumberFormat f = NumberFormat.getInstance(Locale.ITALY); //set the comma instead of the point for double values
        
    private static final List<String[]> temp = new ArrayList<>(100); //array to paste the csv values
    public static final List<Citta> cities = new ArrayList<>(100); //array with cities
    public static DefaultDirectedWeightedGraph<Citta, DefaultWeightedEdge> graphWeighted;
        
    public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
        BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\citta_csv.csv"));//reader of the csv
        String line = br.readLine();
        
        while ((line = br.readLine()) !=null) {
            String[] s = line.split(";"); //split the rows of the csv and put results in the array
            temp.add(s);
        }
        
        //populate the cities array with values read before
        for(int i = 0; i < temp.size(); i++){
            String id, nome = null; Double lat, lon = null;
            temp.get(i).toString().split(" ");
            id = temp.get(i)[0];
            nome = temp.get(i)[1]; 
            lat  = Double.parseDouble(f.parse(temp.get(i)[2]).toString()); 
            lon = Double.parseDouble(f.parse(temp.get(i)[3]).toString()); 
            cities.add(new Citta(id, nome, lat, lon));
        }
                
        br.close();
        
        //open the file with adiacences
        BufferedReader br2 = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\MatriceAdiacenze_csv.csv"));
        String line2 = "";     
        String[][] adjacences = new String[20][20]; //adjacences adjacences
      
        List<String[]> t = new ArrayList<>();
        
        while((line2 = br2.readLine()) != null){
            String[] c = new String[20];
            for(int i = 0; i < 20; i++){
                String[] v = line2.split(";", 20);
                c[i] = v[i];
            }
            t.add(c);
        }
        
        //fill the array with adjacences
        for(int i = 0; i < 20; i++){
            System.arraycopy(t.get(i), 0, adjacences[i], 0, 20);
        }
        
        graphWeighted = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        
        cities.forEach((city) -> {
            graphWeighted.addVertex(city);
        });
        
        for(int i = 0; i < 20; i++)
            for(int j = 0; j < 20; j++)
                if(adjacences[i][j].equals("1")){
                    DefaultWeightedEdge e = graphWeighted.addEdge(cities.get(i), cities.get(j));
                    graphWeighted.setEdgeWeight(e, distance(cities.get(i).getLat(), cities.get(i).getLon(), cities.get(j).getLat(), cities.get(j).getLon()));
                    System.out.println(graphWeighted.getEdgeWeight(e) + " km");
                }
            
        
        
        graphWeighted.edgeSet().forEach((defaultWeightedEdge) -> {
            System.out.println("From: " + graphWeighted.getEdgeSource(defaultWeightedEdge ) + " to: " + graphWeighted.getEdgeTarget(defaultWeightedEdge)); 
        });  
    }
}
