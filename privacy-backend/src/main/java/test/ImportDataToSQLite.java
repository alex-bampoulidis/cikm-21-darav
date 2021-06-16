package test;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class ImportDataToSQLite {

    private static File file = new File("../user_datasets/invoices.csv");
    private static char separator = '\t';
    private static String datasetID = "test3";

    public static void main(String args[]) throws Exception {
        Connection c = DriverManager.getConnection("jdbc:sqlite:db.db");
        c.setAutoCommit(false);
        Statement stmt = c.createStatement();

        CSVParser csvParser = new CSVParserBuilder().withSeparator(separator).withQuoteChar('"').build();
        CSVReader reader = new CSVReaderBuilder(new FileReader(file)).withCSVParser(csvParser).withSkipLines(0).build();

        String header[] = reader.readNext();

        String sqlCreateTable = "CREATE TABLE " + datasetID + "("
                + "EntryID INTEGER PRIMARY KEY AUTOINCREMENT, ";
        for (String field : header) {
            sqlCreateTable += "'" + field + "'" + " TEXT,";
        }
        sqlCreateTable = sqlCreateTable.substring(0, sqlCreateTable.length() - 1) + ");";

        stmt.executeUpdate(sqlCreateTable);

        String headerSQLString = "(";
        for (String field : header) {
            headerSQLString += "'" + field + "'" + ",";
        }
        headerSQLString = headerSQLString.substring(0, headerSQLString.length() - 1) + ")";

        String line[];
        while ((line = reader.readNext()) != null) {
            String sqlInsert = "INSERT INTO " + datasetID + " " + headerSQLString + " VALUES (";
            for (String field : line) {
                sqlInsert += "'" + field + "'" + ",";
            }
            sqlInsert = sqlInsert.substring(0, sqlInsert.length() - 1) + ");";

            stmt.executeUpdate(sqlInsert);
        }

        reader.close();        
        stmt.close();
        c.commit();
        c.close();
    }
}
