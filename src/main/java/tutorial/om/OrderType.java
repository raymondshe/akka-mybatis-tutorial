package tutorial.om;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum OrderType {
  MARKET("Market"), LIMIT("Limit"), STOP("Stop"), STOP_LIMIT("Stop Limit"), WITH_OR_WITHOUT("With or Without");

  private String name;
  private static final Map<String, OrderType> enums = new HashMap<>();

  static {
    Arrays.stream(OrderType.values()).forEach(e -> enums.put(e.getName(), e));
  }

  OrderType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public static OrderType getEnum(String name) {
    return enums.get(name);
  }
}
