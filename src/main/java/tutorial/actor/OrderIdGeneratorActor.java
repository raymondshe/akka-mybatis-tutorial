package tutorial.actor;

import akka.actor.UntypedActor;
import tutorial.om.Order;
import tutorial.om.message.SequenceOrder;
import org.springframework.context.annotation.Scope;

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
    } else {
      unhandled(message);
    }
  }

  private long nextSeqNo() {
    return ++seqNo;
  }
}
