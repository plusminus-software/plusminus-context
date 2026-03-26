package software.plusminus.http.context.fixtures;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/ok")
    public String ok() {
        return "ok";
    }

    @GetMapping("/exception")
    public String exception() {
        throw new IllegalStateException("Test exception");
    }
}
