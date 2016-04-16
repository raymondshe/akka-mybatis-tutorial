package tutorial;

import akka.actor.ActorSystem;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import scala.concurrent.ExecutionContextExecutor;
import tutorial.gateway.OrderGateway;

import java.util.Optional;
import java.util.stream.IntStream;

import static akka.dispatch.Futures.future;

@ComponentScan
public class App {

  public static void main(String[] args) throws InterruptedException {
    SpringApplication app = new SpringApplication(App.class);
    app.setWebEnvironment(false);
    Optional<ConfigurableApplicationContext> context = Optional.empty();

    try {
      context = Optional.ofNullable(app.run(args));
      generateRequests(context.orElseThrow(() -> new RuntimeException("Spring context is unavailable")));
      Thread.sleep(30_000);
    } finally {
      context.map(c -> c.getBean(ActorSystem.class)).ifPresent(ActorSystem::shutdown);
    }
  }

  private static void generateRequests(ConfigurableApplicationContext context) {
    ExecutionContextExecutor executor = context.getBean(ActorSystem.class).dispatcher();

    OrderGateway orderGateway = context.getBean(OrderGateway.class);
    IntStream.range(0, 10).forEach(i ->
            future(() -> {
              orderGateway.placeOrder();
              return 1;
            }, executor));
  }
}
