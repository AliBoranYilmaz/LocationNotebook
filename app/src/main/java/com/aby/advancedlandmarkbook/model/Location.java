package com.aby.advancedlandmarkbook.model;

public class Location
{
    public int id;
    public String name;
    public double latitude;
    public double longitude;

    public Location(int id, String name, double latitude, double longitude)
    {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}