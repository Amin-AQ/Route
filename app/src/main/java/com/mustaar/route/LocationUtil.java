package com.mustaar.route;

public class LocationUtil {
    private static final double EARTH_RADIUS=6371000.0;  // METERS
    public static Boolean distMoreThanHundredMeters(double oldLat, double oldLong, double newLat, double newLong){
        double dLat=Math.toRadians(newLat-oldLat);
        double dLong=Math.toRadians(newLong-oldLong);
        double a =Math.sin(dLat/2)*Math.sin(dLat/2)+Math.cos(Math.toRadians(oldLat))*Math.cos(Math.toRadians(newLat))*Math.sin(dLong/2)*Math.sin(dLong/2);
        double c =2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
        double dist=EARTH_RADIUS*c;
        return dist > 100.0;
    }
}
