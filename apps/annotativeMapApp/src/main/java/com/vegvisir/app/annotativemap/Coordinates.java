package com.vegvisir.app.annotativemap;

public class Coordinates {
    private int x;
    private int y;

    public Coordinates(int xCoord, int yCoord) {
        this.x = xCoord;
        this.y = yCoord;
    }

    public int getX() {
        return (this.x);
    }

    public int getY() {
        return (this.y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        else{
            Coordinates t = (Coordinates) o;
            if ((this.x == t.getX()) && (this.y == t.getY())){
                return true;
            }
            return false;
        }
    }

    @Override
    public int hashCode() {
        return ((new Integer(this.x).hashCode()) ^ (new Integer(this.y).hashCode()));
    }

    @Override
    public String toString() {
        return ("" + x + "," + y);
    }
}
