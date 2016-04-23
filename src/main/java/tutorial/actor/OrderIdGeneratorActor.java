package tutorial.actor;

import akka.actor.UntypedActor;
import org.springframework.context.annotation.Scope;
import tutorial.om.Order;
import tutorial.om.message.CurrentOrderId;
import tutorial.om.message.GetCurrentOrderId;
import tutorial.om.message.SequenceOrder;

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
      getSender().tell(new SequenceOrder(order), getSelf());

    } else if (message instanceof GetCurrentOrderId) {
      getSender().tell(new CurrentOrderId(seqNo), getSelf());

    } else {
      unhandled(message);
    }
  }

  private long nextSeqNo() {
    return ++seqNo;
  }
}
