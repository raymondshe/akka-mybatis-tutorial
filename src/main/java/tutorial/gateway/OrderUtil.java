package tutorial.gateway;

import tutorial.om.Order;
import tutorial.om.OrderType;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class OrderUtil {
  private static final List<String> SYMBOLS = Arrays.asList("APPL", "GOOG", "IBM", "YAH");
  private static final Random random = new Random();

  public static Order generateRandomOrder() {
    Order order = new Order();
    order.setExecutionPrice(BigDecimal.valueOf(random.nextDouble() * 100));
    order.setOrderType(OrderType.values()[random.nextInt(OrderType.values().length)]);
    order.setSymbol(SYMBOLS.get(random.nextInt(SYMBOLS.size())));
    order.setUserId(Math.abs(random.nextInt()));
    return order;
  }
}
