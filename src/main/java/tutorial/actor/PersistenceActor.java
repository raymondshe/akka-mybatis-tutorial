package tutorial.actor;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import org.springframework.context.annotation.Scope;
import tutorial.dal.OrderDao;
import tutorial.om.message.PersistedOrder;
import tutorial.om.message.PreparedOrder;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Random;

@Named("Persistence")
@Scope("prototype")
public class PersistenceActor extends UntypedActor {
  private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  private OrderDao orderDao;
  private Random random = new Random();

  @Inject
  public PersistenceActor(OrderDao orderDao) {
    this.orderDao = orderDao;
  }

  public static Props props(final OrderDao orderDao) {
    return Props.create(new Creator<PersistenceActor>() {
      private static final long serialVersionUID = 1L;

      @Override
      public PersistenceActor create() throws Exception {
        return new PersistenceActor(orderDao);
      }
    });
  }

  @Override
  public void onReceive(Object message) throws Exception {
    //randomFail(message);

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
    random.ints(1).forEach(i -> {
      if (i % 2 == 0) throw new RuntimeException("random fail: error happened: " + msg);
    });
  }
}
