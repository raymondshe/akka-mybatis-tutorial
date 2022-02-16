package tutorial.actor;

import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import com.google.common.base.MoreObjects;
import org.springframework.context.annotation.Scope;
import tutorial.dal.OrderDao;
import tutorial.om.Order;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Random;

@Named("Persistence")
@Scope("prototype")
public class PersistenceActor extends UntypedAbstractActor {
  private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  private OrderDao orderDao;
  private boolean randomFail;
  private Random random = new Random();

  @Inject
  public PersistenceActor(OrderDao orderDao, @Named("PersistenceRandomFail") boolean randomFail) {
    this.orderDao = orderDao;
    this.randomFail = randomFail;
  }

  public static Props props(final OrderDao orderDao, boolean randomFail) {
    return Props.create(new Creator<PersistenceActor>() {
      private static final long serialVersionUID = 1L;

      @Override
      public PersistenceActor create() throws Exception {
        return new PersistenceActor(orderDao, randomFail);
      }
    });
  }

  @Override
  public void onReceive(Object msg) throws Exception {
    if (msg instanceof PreparedOrder) {
      randomFail(msg);
      PreparedOrder preparedOrder = (PreparedOrder) msg;
      log.info("Order to be persisted: {}", preparedOrder);
      orderDao.saveOrder(preparedOrder.order);
      getSender().tell(new PersistedOrder(preparedOrder.order, preparedOrder.deliveryId), self());

    } else if (msg instanceof CompleteBatchForId) {
      CompleteBatchForId batchForId = (CompleteBatchForId) msg;
      orderDao.completeBatch(batchForId.id);
      getSender().tell(new BatchCompleted(batchForId.id), self());

    } else if (msg instanceof ExecutedQuantity) {
      log.info("ExecutedQuantity: {}", msg);
      ExecutedQuantity executedQuantity = (ExecutedQuantity) msg;
      orderDao.insertExecution(executedQuantity.orderId, executedQuantity.quantity, new Date());

    } else {
      unhandled(msg);
    }
  }

  private void randomFail(Object msg) {
    if (!randomFail) return;

    random.ints(1).forEach(i -> {
      if (i % 2 == 0) throw new RuntimeException("random fail on message: " + msg);
    });
  }
}

class BatchCompleted {
  public final long id;

  public BatchCompleted(long id) {
    this.id = id;
  }
}

class PersistedOrder {
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
