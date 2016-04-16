package tutorial.gateway;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tutorial.om.Order;
import tutorial.om.OrderType;
import tutorial.om.message.NewOrder;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static tutorial.spring.SpringExtension.SpringExtProvider;

@Service
public class OrderGateway {
  private static final List<String> SYMBOLS = Arrays.asList("APPL", "GOOG", "IBM", "YAH");

  private Random random = new Random();
  @Autowired
  private ActorSystem system;
  private ActorRef orderProcessor;

  @PostConstruct
  public void createActors() {
    orderProcessor = system.actorOf(SpringExtProvider.get(system).props("OrderProcessorActor"), "orderProcessor");
  }

  public void placeOrder() {
    Order order = new Order();
    order.setExecutionDate(LocalDateTime.now().plus(random.nextInt(25), ChronoUnit.HOURS));
    order.setExecutionPrice(BigDecimal.valueOf(random.nextDouble() * 100));
    order.setOrderType(OrderType.values()[random.nextInt(OrderType.values().length)]);
    order.setSymbol(SYMBOLS.get(random.nextInt(SYMBOLS.size())));
    order.setUserId(Math.abs(random.nextInt()));

    orderProcessor.tell(new NewOrder(order), ActorRef.noSender());
  }
}
