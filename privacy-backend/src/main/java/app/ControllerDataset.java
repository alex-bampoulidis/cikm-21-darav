package app;

import static app.Application.client;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Map;
import java.util.Random;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
public class ControllerDataset {

    @PostMapping("/dataset")
    @CrossOrigin(origins = "http://localhost")
    public String importDataset(@RequestParam("datasetid") String datasetID) throws Exception {
        SearchHits searchHits = getSearchHits(datasetID);

        if (searchHits.getTotalHits().value != 0) {
            File localFile = getLocalFile(searchHits.getAt(0));
            char separator = getSeparator(searchHits.getAt(0));

            String internalDatasetID = generateRandomString();

            writeDatasetIDMapping(datasetID, internalDatasetID);

            if (localFile.exists()) {
                importDataToSQLite(localFile, separator, internalDatasetID);

                updateImportProgress(datasetID);

                return "{\"msg\" : \"Import completed\"}"; // the return value is now on JSON format
            } else {
                return "{\"msg\" : \"File not found\"}"; // the return value is now on JSON format
            }
        } else {
            return "{\"msg\" : \"DatasetID not found\"}"; // the return value is now on JSON format
        }
    }

    private SearchHits getSearchHits(String datasetID) throws IOException {
        SearchRequest searchRequest = new SearchRequest("datasets_info");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("id.keyword", datasetID));

        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        return searchResponse.getHits();
    }

    private char getSeparator(SearchHit hit) {
        Map<String, Object> sourceAsMap = hit.getSourceAsMap();

        String separator = (String) sourceAsMap.get("datasetSeparator");

        if (separator.equals("tab")) {
            return '\t';
        } else if (separator.equals(";")) {
            return ';';
        } else {
            return ',';
        }
    }

    private File getLocalFile(SearchHit hit) {
        Map<String, Object> sourceAsMap = hit.getSourceAsMap();

        String localFileName = (String) sourceAsMap.get("datasetLocalFileName");

        return new File("./user_datasets/" + localFileName);
    }

    private String generateRandomString() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }

    private void writeDatasetIDMapping(String datasetID, String internalDatasetID) throws Exception {
        Connection c = DriverManager.getConnection(Application.dbPath);
        c.setAutoCommit(false);
        Statement stmt = c.createStatement();

        String sql = "INSERT INTO datasetIDMapping VALUES ("
                + "'" + datasetID + "'" + "," + "'" + internalDatasetID + "');";

        stmt.executeUpdate(sql);

        stmt.close();
        c.commit();
        c.close();
    }

    private void importDataToSQLite(File file, char separator, String datasetID) throws Exception {
        Connection c = DriverManager.getConnection(Application.dbPath);
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
                sqlInsert += "'" + field.replace("'","''") + "'" + ",";
            }
            sqlInsert = sqlInsert.substring(0, sqlInsert.length() - 1) + ");";
            
            stmt.executeUpdate(sqlInsert);
        }

        reader.close();
        stmt.close();
        c.commit();
        c.close();
    }

    private void updateImportProgress(String datasetID) throws IOException {
        UpdateRequest request = new UpdateRequest();
        request.index("datasets_info");
        request.type("_doc");
        request.id(datasetID);
        request.doc("datasetUploadStatusPercent", 100);

        UpdateResponse updateResponse = client.update(request, RequestOptions.DEFAULT);
    }
}
