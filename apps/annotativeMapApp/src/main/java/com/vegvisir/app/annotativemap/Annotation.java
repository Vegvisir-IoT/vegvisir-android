package com.vegvisir.app.annotativemap;

public class Annotation {
    private String annotation = "";
    private Boolean shouldRemove = false;
    private Boolean alreadyAdded = false;

    public Annotation (String anno) {
        annotation = anno;
    }

    public String getAnnotation() {
        return annotation;
    }

    public Boolean getShouldRemove() {
        return shouldRemove;
    }

    public Boolean getAlreadyAdded() {
        return alreadyAdded;
    }

    public void setAnnotation(String anno) {
        annotation = anno;
    }

    public void setShouldRemove(Boolean flag) {
        shouldRemove = flag;
    }

    public void setAlreadyAdded(Boolean aa) {
        alreadyAdded = aa;
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
            Annotation a = (Annotation) o;
            return (annotation.equals(a.getAnnotation()));
        }
    }

    @Override
    public int hashCode() {
        return (annotation.hashCode() );
    }

    @Override
    public String toString() {
        return annotation + ", should remove: " + shouldRemove + ", already added: " + alreadyAdded;
    }

}
