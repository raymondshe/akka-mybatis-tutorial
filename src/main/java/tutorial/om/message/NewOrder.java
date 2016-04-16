package tutorial.om.message;

import com.google.common.base.MoreObjects;
import tutorial.om.Order;

public class NewOrder {
  public final Order order;

  public NewOrder(Order order) {
    this.order = order;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
            .add("order", order)
            .toString();
  }
}
