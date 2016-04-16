package tutorial.om.message;

import com.google.common.base.MoreObjects;
import tutorial.om.Order;

public class PersistedOrder {
  public final Order order;
  public final long deliveryId;

  public PersistedOrder(Order order, long deliveryId) {
    this.order = order;
    this.deliveryId = deliveryId;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
            .add("order", order)
            .add("deliveryId", deliveryId)
            .toString();
  }
}
