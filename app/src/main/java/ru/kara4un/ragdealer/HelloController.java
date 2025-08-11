package ru.kara4un.ragdealer;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import ru.kara4un.ragdealer.core.GreetingService;

@Controller("/")
public class HelloController {
    private final GreetingService greetingService;

    public HelloController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @Get(produces = MediaType.TEXT_HTML)
    public String index() {
        return greetingService.greeting();
    }
}
