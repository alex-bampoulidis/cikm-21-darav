package test;

import datatypes.Location;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationPrivacy {

    private static String datasetID = "location";
    private static String userIDColumn = "userid";
    private static String dateColumn = "time";
    private static String dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static String latColumn = "lat";
    private static String lonColumn = "lon";
    private static String locationIDColumn = "locationid";

    private static int users = 1;
    private static double radius = 0.2;
    private static int time = 1;
    private static String timeframe = "hours";

    public static void main(String args[]) throws Exception {
        Connection c = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\alexb\\Desktop\\work\\trusts\\anonymisation_de-anonymisation_risk_analysis\\user_datasets\\db.db");
        c.setAutoCommit(false);

        if (!processedTableExists(c, datasetID)) {
            createProcessedDataset(c, datasetID, userIDColumn, dateColumn, dateFormat, latColumn, lonColumn, locationIDColumn);
            c.commit();
        }

        Map<String, Integer> locationIDTotals = createLocationIDTotals(c, datasetID);

        Map<String, String> locationIDCoords = createLocationIDCoords(c, datasetID);

        Map<String, Integer> locationIDUnsafe = new HashMap<>();

        Map<String, String> entryIDEntry = createEntryIDEntry(c, datasetID);

        Statement stmt = c.createStatement();

        double distance = Math.cos(radius / 6371);

        Calendar cal = Calendar.getInstance();

        for (Map.Entry<String, String> entries : entryIDEntry.entrySet()) {
            String entry[] = entries.getValue().split(",");

            String userID = entry[0];
            long unixTime = Long.parseLong(entry[1]);
            Double cosLat = Double.parseDouble(entry[2]);
            Double sinLat = Double.parseDouble(entry[3]);
            Double cosLon = Double.parseDouble(entry[4]);
            Double sinLon = Double.parseDouble(entry[5]);
            String locationID = entry[6];

            Date current = new Date(unixTime);
            cal.setTime(current);

            long minTime = getMinTime(cal, time, timeframe);

            cal.setTime(current);

            long maxTime = getMaxTime(cal, time, timeframe);

            String sql = "SELECT DISTINCT userID "
                    + "FROM " + datasetID + "_processed "
                    + "WHERE (" + sinLat + " * sin_lat + " + cosLat + " * cos_lat * "
                    + "(cos_lon * " + cosLon + " + sin_lon * " + sinLon + ") > " + distance + ") "
                    + "AND "
                    + "userid != \"" + userID + "\" "
                    + "AND "
                    + "(unixtime >= " + minTime + " AND unixtime <= " + maxTime + ");";

            int userIDs = 0;

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                userIDs++;
            }

            rs.close();

            if (userIDs < users) {
                if (!locationIDUnsafe.containsKey(locationID)) {
                    locationIDUnsafe.put(locationID, 1);
                } else {
                    locationIDUnsafe.put(locationID, locationIDUnsafe.get(locationID) + 1);
                }
            }
        }

        stmt.close();
        c.commit();
        c.close();

        Location output[] = new Location[locationIDCoords.size()];
        int index = 0;

        for (Map.Entry<String, String> entry : locationIDCoords.entrySet()) {
            String locationID = entry.getKey();
            double lat = Double.parseDouble(entry.getValue().split(",")[0]);
            double lon = Double.parseDouble(entry.getValue().split(",")[1]);

            int unsafe = 0;
            if (locationIDUnsafe.containsKey(locationID)) {
                unsafe = locationIDUnsafe.get(locationID);
            }

            int total = locationIDTotals.get(locationID);

            double risk = (unsafe / (double) total) * 100;

            String color = getColor(risk);

            output[index++] = new Location(lat, lon, risk, color);
        }        
    }

    private static boolean processedTableExists(Connection c, String datasetID) throws Exception {
        Statement stmt = c.createStatement();

        String sql = "SELECT name "
                + "FROM sqlite_master "
                + "WHERE type=\"table\" AND name=\"" + datasetID + "_processed\";";

        ResultSet rs = stmt.executeQuery(sql);

        boolean exists = false;
        if (rs.next()) {
            exists = true;
        }

        rs.close();
        stmt.close();

        return exists;
    }

    private static void createProcessedDataset(
            Connection c, String datasetID, String userIDColumn, String dateColumn,
            String dateFormat, String latColumn, String lonColumn, String locationIDColumn
    ) throws Exception {
        Statement stmt = c.createStatement();

        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

        String sql = "CREATE TABLE " + datasetID + "_processed("
                + "EntryID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "userid TEXT,"
                + "unixtime REAL,"
                + "lat REAL,"
                + "cos_lat REAL,"
                + "sin_lat REAL,"
                + "lon REAL,"
                + "cos_lon REAL,"
                + "sin_lon REAL,"
                + "locationid TEXT);";

        stmt.executeUpdate(sql);

        Map<String, String> coordsLocationID = new HashMap<>();

        int locationIDIndex = 0;

        sql = "SELECT * FROM " + datasetID + ";";

        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            String userID = rs.getString(userIDColumn);

            String date = rs.getString(dateColumn);
            long unixTime = sdf.parse(date).getTime();

            double lat = Double.parseDouble(rs.getString(latColumn));
            double cosLat = Math.cos(Math.toRadians(lat));
            double sinLat = Math.sin(Math.toRadians(lat));

            double lon = Double.parseDouble(rs.getString(lonColumn));
            double cosLon = Math.cos(Math.toRadians(lon));
            double sinLon = Math.sin(Math.toRadians(lon));

            String locationID = "";
            if (!locationIDColumn.equals("")) {
                locationID = rs.getString(locationIDColumn);
            } else {
                String coords = lat + "," + lon;
                if (coordsLocationID.containsKey(coords)) {
                    locationID = coordsLocationID.get(coords);
                } else {
                    locationID = (locationIDIndex++) + "";
                    coordsLocationID.put(coords, locationID);
                }
            }

            String sqlInsert = "INSERT INTO " + datasetID + "_processed "
                    + "(userid, unixtime, lat, cos_lat, sin_lat, lon, cos_lon, sin_lon, locationid) VALUES ("
                    + "\"" + userID + "\","
                    + unixTime + ","
                    + lat + "," + cosLat + "," + sinLat + ","
                    + lon + "," + cosLon + "," + sinLon + ","
                    + "\"" + locationID + "\""
                    + ");";

            Statement stmt2 = c.createStatement();
            stmt2.executeUpdate(sqlInsert);
            stmt2.close();
        }

        rs.close();
        stmt.close();
    }

    private static Map<String, Integer> createLocationIDTotals(Connection c, String datasetID) throws Exception {
        Map<String, Integer> locationIDTotals = new HashMap<>();

        Statement stmt = c.createStatement();

        String sql = "SELECT locationid, count(*) as c "
                + "FROM " + datasetID + "_processed "
                + "GROUP BY locationid;";

        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            locationIDTotals.put(rs.getString("locationid"), rs.getInt("c"));
        }

        rs.close();
        stmt.close();

        return locationIDTotals;
    }

    private static Map<String, String> createLocationIDCoords(Connection c, String datasetID) throws Exception {
        Map<String, String> locationIDCoords = new HashMap<>();

        Statement stmt = c.createStatement();

        String sql = "SELECT lat, lon, locationid "
                + "FROM " + datasetID + "_processed "
                + "GROUP BY lat, lon, locationid;";

        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            locationIDCoords.put(rs.getString("locationid"), rs.getDouble("lat") + "," + rs.getDouble("lon"));
        }

        rs.close();
        stmt.close();

        return locationIDCoords;
    }

    private static Map<String, String> createEntryIDEntry(Connection c, String datasetID) throws Exception {
        Map<String, String> entryIDEntry = new HashMap<>();

        Statement stmt = c.createStatement();

        String sql = "SELECT * FROM " + datasetID + "_processed;";

        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            String entryID = rs.getInt("EntryID") + "";
            String userID = rs.getString("userid");
            String unixTime = rs.getLong("unixtime") + "";
            String cosLat = rs.getDouble("cos_lat") + "";
            String sinLat = rs.getDouble("sin_lat") + "";
            String cosLon = rs.getDouble("cos_lon") + "";
            String sinLon = rs.getDouble("sin_lon") + "";
            String locationID = rs.getString("locationid");

            entryIDEntry.put(entryID, userID + "," + unixTime + "," + cosLat + ","
                    + sinLat + "," + cosLon + "," + sinLon + "," + locationID);
        }

        rs.close();
        stmt.close();

        return entryIDEntry;
    }

    private static List<String> createStack(int datasetSize) {
        List<String> stack = new ArrayList<>();

        for (int i = 1; i <= datasetSize; i++) {
            stack.add(i + "");
        }

        return stack;
    }

    private static long getMinTime(
            Calendar cal,
            int timeWithin,
            String timeframe
    ) {
        if (timeframe.equals("years")) {
            cal.add(Calendar.YEAR, -timeWithin);
        } else if (timeframe.equals("months")) {
            cal.add(Calendar.MONTH, -timeWithin);
        } else if (timeframe.equals("weeks")) {
            cal.add(Calendar.WEEK_OF_YEAR, -timeWithin);
        } else if (timeframe.equals("days")) {
            cal.add(Calendar.DAY_OF_MONTH, -timeWithin);
        } else if (timeframe.equals("hours")) {
            cal.add(Calendar.HOUR_OF_DAY, -timeWithin);
        } else if (timeframe.equals("minutes")) {
            cal.add(Calendar.MINUTE, -timeWithin);
        }

        return cal.getTimeInMillis();
    }

    private static long getMaxTime(
            Calendar cal,
            int timeWithin,
            String timeframe
    ) {
        if (timeframe.equals("years")) {
            cal.add(Calendar.YEAR, timeWithin);
        } else if (timeframe.equals("months")) {
            cal.add(Calendar.MONTH, timeWithin);
        } else if (timeframe.equals("weeks")) {
            cal.add(Calendar.WEEK_OF_YEAR, timeWithin);
        } else if (timeframe.equals("days")) {
            cal.add(Calendar.DAY_OF_MONTH, timeWithin);
        } else if (timeframe.equals("hours")) {
            cal.add(Calendar.HOUR_OF_DAY, timeWithin);
        } else if (timeframe.equals("minutes")) {
            cal.add(Calendar.MINUTE, timeWithin);
        }

        return cal.getTimeInMillis();
    }

    private static String getColor(double risk) {
        int R = (int) (255 * risk) / 100;
        int G = (int) (255 * (100 - risk)) / 100;
        int B = 0;

        return String.format("#%02x%02x%02x", R, G, B);
    }
}
