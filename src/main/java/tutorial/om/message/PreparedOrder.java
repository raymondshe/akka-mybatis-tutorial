package tutorial.om.message;

import com.google.common.base.MoreObjects;
import tutorial.om.Order;

public class PreparedOrder {
  public final Long deliveryId;
  public final Order order;

  public PreparedOrder(Long deliveryId, Order order) {
    this.deliveryId = deliveryId;
    this.order = order;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
            .add("deliveryId", deliveryId)
            .add("order", order)
            .toString();
  }
}
