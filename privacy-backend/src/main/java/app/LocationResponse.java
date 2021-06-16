package app;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LocationResponse {

    @GetMapping("/locationtest")
    public String test() throws Exception {
        return new String(Files.readAllBytes(Paths.get("./user_datasets/location_test.txt")));
    }
}
