package pl.edu.agh.firecell.model;

public record Index(int x, int y, int z) {

    public Index getUpIndex(){
        return new Index(x,y,z+1);
    }
    public Index getDownIndex(){
        return new Index(x,y,z-1);
    }
    public Index getNorthIndex(){
        return new Index(x+1,y,z);
    }
    public Index getSouthIndex(){
        return new Index(x-1,y,z);
    }
    public Index getEastIndex(){
        return new Index(x,y+1,z);
    }
    public Index getWestIndex(){
        return new Index(x,y-1,z);
    }
}
