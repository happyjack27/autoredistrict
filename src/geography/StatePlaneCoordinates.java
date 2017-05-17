package geography;

// https://www.expertgps.com/spcs/wisconsin.asp

/*
 * Copyright (C) 2007 Mark Thomas. All rights reserved.
 *
 * 2013 Dave Wilson : added support for Transverse Mercator Projection
 *
 * $Id: StatePlaneCoordinates.java 224 2013-06-20 14:08:36Z mark $
 */

import java.util.Map;
import java.util.WeakHashMap;

/**
 * This class is a port of the SPCS83 program that converts state plane
 * coordinates to latitude/longitude and vice versa; however this class
 * currently only supports converting from state plane coordinates to
 * latitude/longitude.<br/>
 * <p>The original FORTRAN code may be obtained
 * <a href="http://www.ngs.noaa.gov/PC_PROD/SPCS83/">here</a>
 * </p>
 */
public class StatePlaneCoordinates {

    private static final double RAD = (180d / Math.PI);
    private static final double ER  = 6378137d;                 // semi-major axis for GRS-80
    private static final double RF  = 298.257222101d;           // reciprocal flattening for GRS-80
    private static final double F   = (1d / RF);                // flattening for GRS-80
    private static final double ESQ = (F + F - F * F);
    private static final double E   = StrictMath.sqrt(ESQ);

    private static Map<Integer, double[]> ZONES = new WeakHashMap<Integer, double[]>();

    static {
        // ****     403      California III (LAMBERT CONIC PROJECTION)
        ZONES.put(403, new double[] {
          120.5d,               // 120.5D0
          2000000.0001016d,     // 2000000.DO
          500000.0001016001d,   // 500000.DO
          37.06666666666667d,   // 37.D0, 4.D0
          38.43333333333333d,   // 38.D0, 26.D0
          36.5d                 // 36.5DO
        });

        // ****     901      FL EAST    (TRANSVERSION MERCATOR PROJECTION)
        ZONES.put(901, new double[] {
          81.0d,                // CENTRAL MERIDIAN (CM) or Origin Longitude (OM)
          200000.0d,            // FALSE EASTING VALUE AT THE CM (METERS)
          24.3333333333333d,    // SOUTHERNMOST PARALLEL or Origin Latitude
          17000.0d,             // SCALE FACTOR (converts to 0.999941177 )
          0.0d,                 // FALSE NORTHING VALUE AT SOUTHERMOST PARALLEL (METERS)
        });

        // ****     902      FL WEST    (TRANSVERSION MERCATOR PROJECTION)
        ZONES.put(902, new double[] {
          82.0d,                // CENTRAL MERIDIAN (CM) or Origin Longitude (OM)
          200000.0d,            // FALSE EASTING VALUE AT THE CM (METERS)
          24.3333333333d,       // SOUTHERNMOST PARALLEL or Origin Latitude
          17000.0d,             // SCALE FACTOR (converts to 0.999941177 )
          0.0d,                 // FALSE NORTHING VALUE AT SOUTHERMOST PARALLEL (METERS)
        });

        // ****     903      FL NORTH   (LAMBERT CONIC PROJECTION)
        ZONES.put(903, new double[] {
          84.5d,                // T(84.D0,30.D0)
          600000.0d,            // 600000.D0
          0.0d,                 // 0.D0
          29.5833333333333d,    // T(29.D0,35.D0)
          30.75d,               // T(30.D0,45.D0)
          29.0d                 // 29.D0
        });
    }

    /**
     * This method converts state plane coordinates in meters from state plane to latitude and longitude.
     * @param easting   Easting in meters
     * @param northing  Northing in meters
     * @param zone      zone the coordinates are in
     * @return          array of double, containing longitude and latitude
     */
    public static double[] toLatLng(final double easting, final double northing, final int zone) {
        final double[] SPCC = ZONES.get(zone);
        double[] result = null;
        if (SPCC == null || SPCC.length != 5 && SPCC.length != 6) {
            result = new double[] { 0, 0 };
        } else if (SPCC.length == 6) {
            result = toLatLngLambertConic(easting, northing, zone);
        } else if (SPCC.length == 5) {
            result = toLatLngFromTransverseMercator(easting, northing, zone);
        }
        return result;
    }

    private static double[] toLatLngLambertConic(final double easting, final double northing, final int zone) {
        final double[] SPCC = ZONES.get(zone);

        final double CM = SPCC[0] / RAD;            // CENTRAL MERIDIAN (CM)
        final double EO = SPCC[1];                  // FALSE EASTING VALUE AT THE CM (METERS)
        final double NB = SPCC[2];                  // FALSE NORTHING VALUE AT SOUTHERMOST PARALLEL (METERS), (USUALLY ZERO)
        final double FIS = SPCC[3] / RAD;           // LATITUDE OF SO. STD. PARALLEL
        final double FIN = SPCC[4] / RAD;           // LATITUDE OF NO. STD. PARALLEL
        final double FIB = SPCC[5] / RAD;           // LATITUDE OF SOUTHERNMOST PARALLEL
        final double SINFS = StrictMath.sin(FIS);
        final double COSFS = StrictMath.cos(FIS);
        final double SINFN = StrictMath.sin(FIN);
        final double COSFN = StrictMath.cos(FIN);
        final double SINFB = StrictMath.sin(FIB);
        final double QS = q(E, SINFS);
        final double QN = q(E, SINFN);
        final double QB = q(E, SINFB);
        final double W1 = StrictMath.sqrt(1d - ESQ * SINFS * SINFS);
        final double W2 = StrictMath.sqrt(1d - ESQ * SINFN * SINFN);
        final double SINFO =
          (StrictMath.log(W2 * COSFS / (W1 * COSFN)) / (QN - QS));
        final double K =
          (ER * COSFS * StrictMath.exp(QS * SINFO) / (W1 * SINFO));
        final double RB = (K / StrictMath.exp(QB * SINFO));
        double NPR = RB - northing + NB;
        double EPR = easting - EO;
        double GAM = StrictMath.atan(EPR / NPR);
        double LON = CM - (GAM / SINFO);
        double RPT = StrictMath.sqrt(NPR * NPR + EPR * EPR);
        double Q = StrictMath.log(K / RPT) / SINFO;
        double TEMP = StrictMath.exp(Q + Q);
        double SINE = (TEMP - 1d) / (TEMP + 1d);
        double F1, F2;
        for (int i = 0; i < 2; i++) {
            F1 = ((StrictMath.log((1d + SINE) / (1d - SINE)) - E *
              StrictMath.log((1d + E * SINE) / (1d - E * SINE))) / 2d) - Q;
            F2 = (1d / (1d - SINE * SINE) - ESQ / (1d - ESQ * SINE * SINE));
            SINE -= (F1 / F2);
        }
        return new double[] { StrictMath.toDegrees(LON) * - 1, StrictMath.toDegrees(StrictMath.asin(SINE)) };
    }

    private static double[] toLatLngFromTransverseMercator(final double easting, final double northing, final int zone) {
        final double[] SPCC = ZONES.get(zone);

        // get values for this zone
        final double CM = SPCC[0] / RAD;            // CENTRAL MERIDIAN (CM)
        final double FE = SPCC[1];                  // FALSE EASTING VALUE AT THE CM (METERS)
        final double OR = SPCC[2] / RAD;            // origin latitude
        final double SF = 1.0d - 1.0d / SPCC[3];    // scale factor
        final double FN = SPCC[4];                  // false northing

        // translated from TCONPC subroutine
        double EPS = ESQ / (1.0d - ESQ);
        double PR = (1.0d - F) * ER;
        double EN = (ER - PR) / (ER + PR);
        double EN2 = EN * EN;
        double EN3 = EN * EN * EN;
        double EN4 = EN2 * EN2;

        double C2 = -3.0d * EN / 2.0d + 9.0d * EN3 / 16.0d;
        double C4 = 15.0d * EN2 / 16.0d - 15.0d * EN4 /32.0d;
        double C6 = -35.0d * EN3 / 48.0d;
        double C8 = 315.0d * EN4 / 512.0d;
        double U0 = 2.0d * (C2 - 2.0d * C4 + 3.0d * C6 - 4.0d * C8);
        double U2 = 8.0d * (C4 - 4.0d * C6 + 10.0d * C8);
        double U4 = 32.0d * (C6 - 6.0d * C8);
        double U6 = 129.0d * C8;

        C2 = 3.0d * EN / 2.0d - 27.0d * EN3 / 32.0d;
        C4 = 21.0d * EN2 / 16.0d - 55.0d * EN4 / 32.0d;
        C6 = 151.0d * EN3 / 96.0d;
        C8 = 1097.0d * EN4 / 512.0d;
        double V0 = 2.0d * (C2 - 2.0d * C4 + 3.0d * C6 - 4.0d * C8);
        double V2 = 8.0d * (C4 - 4.0d * C6 + 10.0d * C8);
        double V4 = 32.0d * (C6 - 6.0d * C8);
        double V6 = 128.0d * C8;

        double R = ER * (1.0d - EN) * (1.0d - EN * EN) * (1.0d + 2.25d * EN * EN + (225.0d / 64.0d) * EN4);
        double COSOR = StrictMath.cos(OR);
        double OMO = OR + StrictMath.sin(OR) * COSOR * (U0 + U2 * COSOR * COSOR + U4 * StrictMath.pow(COSOR, 4) + U6 * StrictMath.pow(COSOR, 6));
        double SO = SF * R * OMO;

        // translated from TMGEOD subroutine
        double OM = (northing - FN + SO) / (R * SF);
        double COSOM = StrictMath.cos(OM);
        double FOOT = OM + StrictMath.sin(OM) * COSOM * (V0 + V2 * COSOM * COSOM + V4 * StrictMath.pow(COSOM, 4) + V6 * StrictMath.pow(COSOM, 6));
        double SINF = StrictMath.sin(FOOT);
        double COSF = StrictMath.cos(FOOT);
        double TN = SINF / COSF;
        double TS = TN * TN;
        double ETS = EPS * COSF * COSF;
        double RN = ER * SF / StrictMath.sqrt(1.0d - ESQ * SINF * SINF);
        double Q = (easting - FE) / RN;
        double QS = Q * Q;
        double B2 = -TN * (1.0d + ETS) / 2.0d;
        double B4 = -(5.0d + 3.0d * TS + ETS * (1.0d - 9.0d * TS) - 4.0d * ETS * ETS) / 12.0d;
        double B6 = (61.0d + 45.0d * TS * (2.0d + TS) + ETS * (46.0d - 252.0d * TS -60.0d * TS * TS)) / 360.0d;
        double B1 = 1.0d;
        double B3 = -(1.0d + TS + TS + ETS) / 6.0d;
        double B5 = (5.0d + TS * (28.0d + 24.0d * TS) + ETS * (6.0d + 8.0d * TS)) / 120.0d;
        double B7 = -(61.0d + 662.0d * TS + 1320.0d * TS * TS + 720.0d * StrictMath.pow(TS, 3)) / 5040.0d;
        double LAT = FOOT + B2 * QS * (1.0d + QS * (B4 + B6 * QS));
        double L = B1 * Q * (1.0d + QS * (B3 + QS * (B5 + B7 * QS)));
        double LON = -L / COSF + CM;

        return new double[] { StrictMath.toDegrees(LON) * - 1, StrictMath.toDegrees(LAT) };
    }

    private static double q(double e, double s) {
        return ((StrictMath.log((1 + s) / (1 - s)) - e *
          StrictMath.log((1 + e * s) / (1 - e * s))) / 2);
    }

    /**
     * check calculations and output with http://www.earthpoint.us/StatePlane.aspx
     */
    public static void main(String[] a) {

        // x and y given in US Survey Feet (3937 yards = 3600 meters)
        final double feet2Meters = 0.3048006096012192d;

        // somewhere in california
        System.out.println("California");
        double d1[] = toLatLng(feet2Meters * 6351504, feet2Meters * 2153727, 403);
        System.out.println("lat should be 37.9075149470656, and is " + d1[1]);
        System.out.println("lon should be -121.2284594353128, and is " + d1[0]);

        // G-1502
        // Grossman Hammock
        // XCOORD 793261
        // YCOORD 466320.406
        // lat 25.6159392
        // lon -080.5839482
        System.out.println("Grossman Hammock");
        double d2[] = toLatLng(feet2Meters * 793261, feet2Meters * 466320.406, 901);
        System.out.println("lat should be 25.6159392, and is " + d2[1]);
        System.out.println("lon should be -080.5839482, and is " + d2[0]);

        // somewhere in gainesville
        // XCOORD 2668020.416
        // YCOORD 229201.881
        // lat 29.611670
        // lon -82.298584
        System.out.println("Gainesville");
        double d3[] = toLatLng(feet2Meters * 2668020.416, feet2Meters * 229201.881, 903);
        System.out.println("lat should be 29.611670, and is " + d3[1]);
        System.out.println("lon should be -82.298584, and is " + d3[0]);
    }
}