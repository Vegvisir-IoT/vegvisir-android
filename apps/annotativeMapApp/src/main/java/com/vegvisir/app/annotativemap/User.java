package com.vegvisir.app.annotativemap;

public class User {
    private String username;
    private String password;

    public User(String u, String p) {
        this.username = u;
        this.password = p;
    }

    public String getUsername(){
        return this.username;
    }

    public String getPassword(){
        return this.password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        else if (o == null) {
            return false;
        }
        else if (getClass() != o.getClass()) {
            return false;
        }
        else {
            User u = (User) o;
            return (username.equals(u.getUsername()) && password.equals(u.getPassword()));
        }
    }

    @Override
    public int hashCode() {
        return (username.hashCode() ^ password.hashCode());
    }

    @Override
    public String toString() {
        return (username + ": " + password);
    }
}