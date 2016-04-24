package tutorial.actor;

import akka.actor.UntypedActor;
import com.google.common.base.MoreObjects;
import org.springframework.context.annotation.Scope;
import tutorial.om.Order;

import javax.inject.Named;

@Named("OrderIdGenerator")
@Scope("prototype")
public class OrderIdGeneratorActor extends UntypedActor {
  private long seqNo;

  @Override
  public void onReceive(Object message) throws Exception {
    if (message instanceof Order) {
      Order order = (Order) message;
      order.setOrderId(nextSeqNo());
      getSender().tell(new SequenceOrder(order), self());

    } else if (message instanceof GetCurrentOrderId) {
      getSender().tell(new CurrentOrderId(seqNo), self());

    } else {
      unhandled(message);
    }
  }

  private long nextSeqNo() {
    return ++seqNo;
  }
}

class SequenceOrder {
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

class CurrentOrderId {
  public final long id;

  public CurrentOrderId(long id) {
    this.id = id;
  }
}
