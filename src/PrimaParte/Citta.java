package PrimaParte;

import java.util.Objects;

/**
 *
 * @author Giuseppe
 */
public class Citta {
    private final String id;
    private final String name;
    private final Double lat;
    private final Double lon;
    
    public Citta(String id, String name, Double lat, Double lon){
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
   }

    @Override
    public String toString() {
        return "[ id= " + id + ", name= " + name + ", lat= " + lat + ", lon= " + lon + " ]";
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.name);
        hash = 97 * hash + Objects.hashCode(this.lat);
        hash = 97 * hash + Objects.hashCode(this.lon);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Citta other = (Citta) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.lat, other.lat)) {
            return false;
        }
        return Objects.equals(this.lon, other.lon);
    }
    
    
}
