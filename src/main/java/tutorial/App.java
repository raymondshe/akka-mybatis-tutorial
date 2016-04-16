package tutorial;

import akka.actor.ActorSystem;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import scala.concurrent.ExecutionContextExecutor;
import tutorial.dal.OrderDao;
import tutorial.gateway.OrderGateway;

import java.util.Optional;
import java.util.stream.IntStream;

import static akka.dispatch.Futures.future;

@ComponentScan
public class App {
  private static final int ORDER_COUNT = 10;
  private static final int MAX_DELAY = 30_000;

  public static void main(String[] args) throws InterruptedException {
    SpringApplication app = new SpringApplication(App.class);
    app.setWebEnvironment(false);
    Optional<ConfigurableApplicationContext> context = Optional.empty();

    try {
      context = Optional.ofNullable(app.run(args));
      generateRequests(context.orElseThrow(() -> new RuntimeException("Spring context is unavailable")));
      waitForPersistence(context.get());
    } finally {
      context.map(c -> c.getBean(ActorSystem.class)).ifPresent(ActorSystem::shutdown);
    }
  }

  private static void waitForPersistence(ConfigurableApplicationContext context) throws InterruptedException {
    OrderDao orderDao = context.getBean(OrderDao.class);

    int step = 5_000;
    int delay = step;
    while (true) {
      Thread.sleep(delay);

      if (delay >= MAX_DELAY || orderDao.getOrders().size() == ORDER_COUNT) break;
      delay += step;
    }

    System.out.println("Orders in db: ");
    orderDao.getOrders().forEach(o -> System.out.println(o.toString()));
  }

  private static void generateRequests(ConfigurableApplicationContext context) {
    ExecutionContextExecutor executor = context.getBean(ActorSystem.class).dispatcher();

    OrderGateway orderGateway = context.getBean(OrderGateway.class);
    IntStream.range(0, ORDER_COUNT).forEach(i ->
            future(() -> {
              orderGateway.placeOrder();
              return 1;
            }, executor));
  }
}
