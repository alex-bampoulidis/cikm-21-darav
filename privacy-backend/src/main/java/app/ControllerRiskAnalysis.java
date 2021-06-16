package app;

import static app.Application.client;
import datatypes.Tabular;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import algorithms.*;
import datatypes.Aggregated;
import datatypes.Invoices;
import datatypes.Location;
import datatypes.Textual;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
public class ControllerRiskAnalysis {

    @PostMapping("/risk_analysis")
    @CrossOrigin(origins = "http://localhost")
    public String riskAnalysis(@RequestParam("processid") String processID) throws IOException, Exception {
        SearchHits searchHits = getRiskAnalysisInfo(processID);

        if (searchHits.getTotalHits().value != 0) {
            SearchHit hit = searchHits.getAt(0);

            Map<String, Object> sourceAsMap = hit.getSourceAsMap();

            String datasetID = (String) sourceAsMap.get("datasetId");
            String method = (String) sourceAsMap.get("method");
            Map<String, String> parameters = (Map<String, String>) sourceAsMap.get("parameters");
            Map<String, String> columnTypes = (Map<String, String>) sourceAsMap.get("columnTypes");

            if (method.equals("k-anonymity")) {
                kAnonymity(datasetID, parameters, columnTypes, processID);
            } else if (method.equals("Invoices")) {
                invoices(datasetID, parameters, columnTypes, processID);
            } else if (method.equals("Aggregated")) {
                aggregated(datasetID, parameters, columnTypes, processID);
            } else if (method.equals("l-diversity")) {
                lDiversity(datasetID, parameters, columnTypes, processID);
            } else if (method.equals("location") || method.equals("Location")) {
                location(datasetID, parameters, columnTypes, processID);
            } else if (method.equals("textual") || method.equals("Textual")) {
                textual(datasetID, parameters, columnTypes, processID);
            }

            updateProgress(processID);

            return "{\"msg\": \"Process completed\"}"; // the return value is now on JSON format
        } else {
            return "{\"msg\": \"Process not found\"}"; // the return value is now on JSON format
        }
    }

    private SearchHits getRiskAnalysisInfo(String processID) throws IOException {
        SearchRequest searchRequest = new SearchRequest("risk_analysis_info");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("id.keyword", processID));

        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        return searchResponse.getHits();
    }

    private void kAnonymity(String datasetID, Map<String, String> parameters, Map<String, String> columnTypes, String processID) throws IOException, Exception {
        Map<String, Object> datasetInfo = getDatasetInfo(datasetID);

        String input = "./user_datasets/" + (String) datasetInfo.get("datasetLocalFileName");

        String separator = (String) datasetInfo.get("datasetSeparator");

        List<String> QIs = new ArrayList<>();
        for (Map.Entry<String, String> entry : columnTypes.entrySet()) {
            if (entry.getValue().equals("QI")) {
                QIs.add(entry.getKey());
            }
        }

        int k = Integer.parseInt(parameters.get("k"));

        Tabular[] output = TabularAlgorithm.analyse(input, separator, QIs, k);

        String indexName = "a" + processID.toLowerCase() + "_results";

        CreateIndexRequest request = new CreateIndexRequest(indexName);

        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);

        for (int i = 0; i < output.length; i++) {
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("combination", output[i].getCombination());
            jsonMap.put("QIs", output[i].getQIs() + "");
            jsonMap.put("risk", output[i].getRisk() + "");

            IndexRequest indexRequest = new IndexRequest(indexName).id(i + "").source(jsonMap);

            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        }
    }

    private void invoices(String datasetID, Map<String, String> parameters, Map<String, String> columnTypes, String processID) throws IOException, Exception {
        Map<String, Object> datasetInfo = getDatasetInfo(datasetID);

        String input = "./user_datasets/" + (String) datasetInfo.get("datasetLocalFileName");

        String separator = (String) datasetInfo.get("datasetSeparator");
        if (separator.equals("tab")) {
            separator = "\t";
        }

        String idColumn = "";
        String dateColumn = "";
        String amountColumn = "";

        for (Map.Entry<String, String> entry : columnTypes.entrySet()) {
            if (entry.getValue().equals("individualId")) {
                idColumn = entry.getKey();
            } else if (entry.getValue().equals("invoiceDate")) {
                dateColumn = entry.getKey();
            } else if (entry.getValue().equals("invoiceAmount")) {
                amountColumn = entry.getKey();
            }
        }

        String dateFormat = parameters.get("dateFormat");
        String users = parameters.get("noOfOtherIndividuals");
        String amount = parameters.get("invoiceAmount");
        String time = parameters.get("timeframe_value");
        String timeframe = parameters.get("timeframe_type");

        Invoices[] output = InvoicesAlgorithm.analyse(input, separator, idColumn, dateColumn, dateFormat, amountColumn, users, amount, time, timeframe);

        String indexName = "a" + processID.toLowerCase() + "_results";

        CreateIndexRequest request = new CreateIndexRequest(indexName);

        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);

        for (int i = 0; i < output.length; i++) {
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("millis", output[i].getMillis() + "");
            jsonMap.put("amount", output[i].getAmount() + "");
            jsonMap.put("date", output[i].getDate() + "");
            jsonMap.put("safe", output[i].getSafe() + "");

            IndexRequest indexRequest = new IndexRequest(indexName).id(i + "").source(jsonMap);

            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        }
    }

    private void aggregated(String datasetID, Map<String, String> parameters, Map<String, String> columnTypes, String processID) throws IOException, Exception {
        Map<String, Object> datasetInfo = getDatasetInfo(datasetID);

        String input = "./user_datasets/" + (String) datasetInfo.get("datasetLocalFileName");

        String separator = (String) datasetInfo.get("datasetSeparator");
        if (separator.equals("tab")) {
            separator = "\t";
        }

        List<String> QIs = new ArrayList<>();
        for (Map.Entry<String, String> entry : columnTypes.entrySet()) {
            if (entry.getValue().equals("QI")) {
                QIs.add(entry.getKey());
            }
        }

        Map<String, List<Aggregated>> output = AggregatedAlgorithm.analyse(QIs, separator, input);

        String indexName = "a" + processID.toLowerCase() + "_results";

        CreateIndexRequest request = new CreateIndexRequest(indexName);

        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);

        for (Map.Entry<String, List<Aggregated>> entry : output.entrySet()) {
            for (Aggregated agg : entry.getValue()) {
                Map<String, Object> jsonMap = new HashMap<>();
                jsonMap.put("event", agg.getEvent());
                jsonMap.put("count", agg.getCount() + "");
                jsonMap.put("QI", entry.getKey());

                IndexRequest indexRequest = new IndexRequest(indexName).source(jsonMap);

                IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
            }
        }
    }

    private void lDiversity(String datasetID, Map<String, String> parameters, Map<String, String> columnTypes, String processID) throws Exception {
        String internalDatasetID = getInternalDatasetID(datasetID);

        List<String> QIs = new ArrayList<>();
        List<String> sensitiveAttributes = new ArrayList<>();
        for (Map.Entry<String, String> entry : columnTypes.entrySet()) {
            if (entry.getValue().equals("QI")) {
                QIs.add(entry.getKey());
            } else if (entry.getValue().equals("sensitive")) {
                sensitiveAttributes.add(entry.getKey());
            }
        }

        int l = Integer.parseInt(parameters.get("l"));

        Map<String, Tabular[]> response = LDiversityAlgorithm.analyse(internalDatasetID, QIs, sensitiveAttributes, l);

        String indexName = "a" + processID.toLowerCase() + "_results";

        CreateIndexRequest request = new CreateIndexRequest(indexName);

        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);

        for (Map.Entry<String, Tabular[]> entry : response.entrySet()) {
            String sensitiveAttribute = entry.getKey();

            Map<String, Map<String, Object>[]> jsonMap = new HashMap<>();

            Map<String, Object> temp[] = new HashMap[entry.getValue().length];
            int index = 0;
            for (Tabular tab : entry.getValue()) {
                temp[index] = new HashMap<>();

                temp[index].put("combination", tab.getCombination());
                temp[index].put("QIs", tab.getQIs() + "");
                temp[index++].put("risk", tab.getRisk() + "");
            }
            jsonMap.put(sensitiveAttribute, temp);

            IndexRequest indexRequest = new IndexRequest(indexName).source(jsonMap);

            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        }

    }

    private void location(String datasetID, Map<String, String> parameters, Map<String, String> columnTypes, String processID) throws Exception {
        String internalDatasetID = getInternalDatasetID(datasetID);

        String useridColumn = "";
        String timeColumn = "";
        String latColumn = "";
        String lonColumn = "";
        String locationIDColumn = "";

        for (Map.Entry<String, String> entry : columnTypes.entrySet()) {
            String column = entry.getKey();
            String type = entry.getValue();

            if (type.equals("userid")) {
                useridColumn = column;
            } else if (type.equals("latitude")) {
                latColumn = column;
            } else if (type.equals("longtitude")) {
                lonColumn = column;
            } else if (type.equals("time")) {
                timeColumn = column;
            } else if (type.equals("locationid")) {
                locationIDColumn = column;
            }
        }

        String timeFormat = parameters.get("timeformat");
        int users = Integer.parseInt(parameters.get("users"));
        double radius = Double.parseDouble(parameters.get("radius"));
        int time = Integer.parseInt(parameters.get("timewithin"));
        String timeframe = parameters.get("timeframe");

        Location output[] = LocationAlgorithm.analyse(internalDatasetID, useridColumn, timeColumn, timeFormat, latColumn, lonColumn, locationIDColumn, users, radius, time, timeframe);

        String indexName = "a" + processID.toLowerCase() + "_results";

        CreateIndexRequest request = new CreateIndexRequest(indexName);

        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);

        for (int i = 0; i < output.length; i++) {
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("lat", output[i].getLat() + "");
            jsonMap.put("lon", output[i].getLon() + "");
            jsonMap.put("risk", output[i].getRisk() + "");
            jsonMap.put("color", output[i].getColor());

            IndexRequest indexRequest = new IndexRequest(indexName).id(i + "").source(jsonMap);

            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        }
    }

    private void textual(String datasetID, Map<String, String> parameters, Map<String, String> columnTypes, String processID) throws Exception {
        String internalDatasetID = getInternalDatasetID(datasetID);

        String userIDColumn = "";
        String textColumn = "";

        for (Map.Entry<String, String> entry : columnTypes.entrySet()) {
            String column = entry.getKey();
            String type = entry.getValue();

            if (type.equals("userid")) {
                userIDColumn = column;
            } else if (type.equals("textcolumn")) {
                textColumn = column;
            }
        }

        Textual output = TextualAlgorithm.analyse(internalDatasetID, userIDColumn, textColumn);

        String indexName = "a" + processID.toLowerCase() + "_results";

        CreateIndexRequest request = new CreateIndexRequest(indexName);

        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);

        String x[] = output.getX();
        String y[] = output.getY();
        double z[][] = output.getZ();

        Map<String, String[]> jsonMap = new HashMap<>();

        jsonMap.put("x", x);

        IndexRequest indexRequest = new IndexRequest(indexName).id("0").source(jsonMap);

        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);

        jsonMap = new HashMap<>();

        jsonMap.put("y", y);

        indexRequest = new IndexRequest(indexName).id("1").source(jsonMap);

        indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);

        for (int i = 0; i < z.length; i++) {
            Map<String, double[]> jsonMap2 = new HashMap<>();

            jsonMap2.put("z_" + i, z[i]);

            indexRequest = new IndexRequest(indexName).id("z_" + i).source(jsonMap2);

            indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        }
    }

    private Map<String, Object> getDatasetInfo(String datasetID) throws IOException {
        SearchRequest searchRequest = new SearchRequest("datasets_info");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("id.keyword", datasetID));

        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        SearchHit hit = searchResponse.getHits().getAt(0);

        return hit.getSourceAsMap();
    }

    private static String getInternalDatasetID(String datasetID) throws Exception {
        String internalDatasetID = "";

        Connection c = DriverManager.getConnection(Application.dbPath);
        c.setAutoCommit(false);
        Statement stmt = c.createStatement();

        String sql = "SELECT internalDatasetID FROM datasetIDMapping WHERE datasetID = \"" + datasetID + "\";";

        ResultSet rs = stmt.executeQuery(sql);

        if (rs.next()) {
            internalDatasetID = rs.getString("internalDatasetID");
        }

        rs.close();
        stmt.close();
        c.commit();
        c.close();

        return internalDatasetID;
    }

    private void updateProgress(String processID) throws IOException {
        UpdateRequest request = new UpdateRequest();
        request.index("risk_analysis_info");
        request.type("_doc");
        request.id(processID);
        request.doc("ended", new Date().toString(),
                "status", "Completed",
                "resultsIndexId", "a" + processID.toLowerCase() + "_results");

        UpdateResponse updateResponse = client.update(request, RequestOptions.DEFAULT);
    }
}
