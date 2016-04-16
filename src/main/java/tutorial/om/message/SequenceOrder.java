package tutorial.om.message;

import com.google.common.base.MoreObjects;
import tutorial.om.Order;

public class SequenceOrder {
  public final Order order;

  public SequenceOrder(Order order) {
    this.order = order;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
            .add("order", order)
            .toString();
  }
}
