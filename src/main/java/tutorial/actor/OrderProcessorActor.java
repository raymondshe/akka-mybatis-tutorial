package tutorial.actor;

import akka.actor.ActorPath;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import akka.japi.Function;
import akka.pattern.Patterns;
import akka.persistence.UntypedPersistentActorWithAtLeastOnceDelivery;
import akka.util.Timeout;
import com.google.common.base.MoreObjects;
import org.springframework.context.annotation.Scope;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import tutorial.om.Order;

import javax.inject.Inject;
import javax.inject.Named;

@Named("OrderProcessor")
@Scope("prototype")
public class OrderProcessorActor extends UntypedPersistentActorWithAtLeastOnceDelivery {
  private static final Timeout _5_SECONDS = new Timeout(Duration.create(5, "seconds"));
  private boolean recovery;
  private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  private ActorRef orderIdGenerator;
  private ActorPath persistenceRouter;

  @Inject
  public OrderProcessorActor(
          final @Named("OrderIdGenerator") ActorRef orderIdGenerator,
          final @Named("Persistence") ActorPath persistenceRouter,
          final @Named("Recovery") boolean recovery) {
    this.orderIdGenerator = orderIdGenerator;
    this.persistenceRouter = persistenceRouter;
    this.recovery = recovery;
  }

  public static Props props(final ActorRef orderIdGenerator, final ActorPath persistenceRouter, boolean recovery) {
    return Props.create(new Creator<OrderProcessorActor>() {
      private static final long serialVersionUID = 1L;

      @Override
      public OrderProcessorActor create() throws Exception {
        return new OrderProcessorActor(orderIdGenerator, persistenceRouter, recovery);
      }
    });
  }

  @Override
  public void onReceiveCommand(Object msg) throws Exception {
    if (msg instanceof NewOrder) {
      log.info("New order received: {}", ((NewOrder) msg).order);
      NewOrder newOrder = (NewOrder) msg;
      orderIdGenerator.tell(newOrder.order, self());

    } else if (msg instanceof SequenceOrder) {
      Order order = ((SequenceOrder) msg).order;
      log.info("Order id generated: {}, for order: {}", order.getOrderId(), order);
      persist(order, this::updateState);

    } else if (msg instanceof PersistedOrder) {
      updateState(msg);
      log.info("Order with id = '{}' has been successfully persisted.", ((PersistedOrder) msg).order.getOrderId());

    } else if (msg instanceof CompleteBatch) {
      Future<Object> f = Patterns.ask(orderIdGenerator, new GetCurrentOrderId(), _5_SECONDS);

      ExecutionContextExecutor ec = getContext().system().dispatcher();
      f.onSuccess(new OnSuccess<Object>() {
        public void onSuccess(Object result) {
          long id = ((CurrentOrderId) result).id;
          getContext().actorSelection(persistenceRouter).tell(new CompleteBatchForId(id), self());
        }
      }, ec);
      f.onFailure(new OnFailure() {
        @Override
        public void onFailure(Throwable failure) throws Throwable {
          getSender().tell(new CompleteBatchFailed(), self());
        }
      }, ec);

    } else if (msg instanceof BatchCompleted) {
      log.info("Batch has been completed. Id = '{}'", ((BatchCompleted) msg).id);
    } else {
      unhandled(msg);
    }
  }

  @Override
  public void onReceiveRecover(Object msg) throws Exception {
    if (!recovery) return;

    log.info("recover journal message: {}", msg);
    updateState(msg);
  }

  private void updateState(Object event) {
    if (event instanceof Order) {
      final Order order = (Order) event;
      deliver(persistenceRouter, (Function<Long, Object>) deliveryId -> new PreparedOrder(deliveryId, order));

    } else if (event instanceof PersistedOrder) {
      final PersistedOrder order = (PersistedOrder) event;
      confirmDelivery(order.deliveryId);
    }
  }

  @Override
  public String persistenceId() {
    return "persistenceId";
  }
}

class CompleteBatchFailed {
}

class CompleteBatchForId {
  public final long id;

  public CompleteBatchForId(long id) {
    this.id = id;
  }
}

class GetCurrentOrderId {
}

class PreparedOrder {
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
