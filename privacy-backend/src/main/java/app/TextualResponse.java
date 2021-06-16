package app;

import algorithms.TextualAlgorithm;
import static app.Application.client;
import datatypes.Textual;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TextualResponse {

    @GetMapping("/textualreviewstest")
    public String test() throws Exception {
        return new String(Files.readAllBytes(Paths.get("./user_datasets/textual_reviews_test.txt")));
    }

    @GetMapping("/textualqueriestest")
    public String test2() throws Exception {
        return new String(Files.readAllBytes(Paths.get("./user_datasets/textual_queries_test.txt")));
    }

    @GetMapping("/test")
    public String test3() throws Exception {
        Textual output = TextualAlgorithm.analyse("textual_reviews_summary", "userid", "review");

        String indexName = "a_results";

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

        return "Done";
    }
}
