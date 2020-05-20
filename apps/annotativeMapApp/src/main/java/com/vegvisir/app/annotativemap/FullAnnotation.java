package com.vegvisir.app.annotativemap;

public class FullAnnotation {
    private Coordinates coords = null;
    private String annotation = "";

    public FullAnnotation(Coordinates c, String anno) {
        coords = c;
        annotation = anno;
    }

    public Coordinates getCoords() {
        return coords;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setCoords(Coordinates c) {
        coords = c;
    }

    public void setAnnotation (String anno){
        annotation = anno;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        FullAnnotation fa = (FullAnnotation) o;
        if(coords.equals(fa.getCoords())) { //&& (annotation == fa.getAnnotation())) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return coords.hashCode();
    }

    @Override
    public String toString() {
        return annotation + ", coordinates: " + coords.toString();
    }

}
