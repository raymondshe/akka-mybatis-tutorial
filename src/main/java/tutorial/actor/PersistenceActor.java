package tutorial.actor;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import tutorial.dal.OrderDao;
import tutorial.om.message.PersistedOrder;
import tutorial.om.message.PreparedOrder;

import javax.inject.Named;
import java.util.Random;

@Named("PersistenceActor")
@Scope("prototype")
public class PersistenceActor extends UntypedActor {
  private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  @Autowired
  private OrderDao orderDao;
  private Random random = new Random();

  @Override
  public void onReceive(Object message) throws Exception {
    randomFail(message);

    if (message instanceof PreparedOrder) {
      PreparedOrder preparedOrder = (PreparedOrder) message;
      log.info("Order to be persisted: {}", preparedOrder);
      orderDao.saveOrder(preparedOrder.order);
      getSender().tell(new PersistedOrder(preparedOrder.order, preparedOrder.deliveryId), getSelf());
    } else {
      unhandled(message);
    }
  }

  private void randomFail(Object msg) {
    random.ints(1).forEach(i -> { if (i % 2 == 0) throw new RuntimeException("random fail: error happened: " + msg);});
  }
}
