package tutorial.actor;

import akka.actor.ActorPath;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.dispatch.OnComplete;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import akka.japi.Function;
import akka.pattern.Patterns;
import akka.util.Timeout;
import akka.persistence.AbstractPersistentActorWithAtLeastOnceDelivery;
import com.google.common.base.MoreObjects;
import org.springframework.context.annotation.Scope;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import tutorial.om.Order;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDateTime;

@Named("OrderProcessor")
@Scope("prototype")
public class OrderProcessorActor extends AbstractPersistentActorWithAtLeastOnceDelivery {
  private static final Timeout _5_SECONDS = new Timeout(Duration.create(5, "seconds"));
  private boolean recovery;
  private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  private ActorRef orderIdGenerator;
  private ActorPath persistenceRouter;
  private ActorRef orderExecution;

  @Inject
  public OrderProcessorActor(
      final @Named("OrderIdGenerator") ActorRef orderIdGenerator,
      final @Named("Persistence") ActorPath persistenceRouter,
      final @Named("Execution") ActorRef orderExecution,
      final @Named("Recovery") boolean recovery) {
    this.orderIdGenerator = orderIdGenerator;
    this.persistenceRouter = persistenceRouter;
    this.orderExecution = orderExecution;
    this.recovery = recovery;
  }

  public static Props props(final ActorRef orderIdGenerator, final ActorPath persistenceRouter,
      final ActorRef orderExecution, boolean recovery) {
    return Props.create(new Creator<OrderProcessorActor>() {
      private static final long serialVersionUID = 1L;

      @Override
      public OrderProcessorActor create() throws Exception {
        return new OrderProcessorActor(orderIdGenerator, persistenceRouter, orderExecution, recovery);
      }
    });
  }

  @Override

  public Receive createReceive() {
    return receiveBuilder()
        .match(
            NewOrder.class,
            msg -> {
              log.info("New order received: {}", ((NewOrder) msg).order);
              NewOrder newOrder = (NewOrder) msg;
              orderIdGenerator.tell(newOrder.order, self());
            })
        .match(
            SequenceOrder.class,
            msg -> {
              Order order = ((SequenceOrder) msg).order;
              log.info("Order id generated: {}, for order: {}", order.getOrderId(), order);
              order.setExecutionDate(LocalDateTime.now());
              persist(order, this::updateState);

            })
        .match(PersistedOrder.class,
            msg -> {
              updateState(msg);
              Order order = ((PersistedOrder) msg).order;
              log.info("Order with id = '{}' has been successfully persisted.", order.getOrderId());
              orderExecution.tell(new ExecuteOrder(order.getOrderId(), order.getQuantity()), self());
            })
        .match(
            CompleteBatch.class,
            msg -> completeBatch())
        .match(
            BatchCompleted.class,
            msg -> log.info("Batch has been completed. Id = '{}'", ((BatchCompleted) msg).id))
        .build();
  }

  private void completeBatch() {
    Future<Object> f = Patterns.ask(orderIdGenerator, new GetCurrentOrderId(), _5_SECONDS);
    ExecutionContextExecutor ec = getContext().system().dispatcher();
    f.onComplete(new OnComplete<Object>() {
      @Override
      public void onComplete(Throwable failure, Object success) {
        if (failure != null) {
          getSender().tell(new CompleteBatchFailed(), self());
        } else {
          long id = ((CurrentOrderId) success).id;
          getContext().actorSelection(persistenceRouter).tell(new CompleteBatchForId(id), self());
          }
      }
    }, ec);
  }

  @Override
  public Receive createReceiveRecover() {
    // public void onReceiveRecover(Object msg) throws Exception {
    if (!recovery)
      receiveBuilder().build();

    return receiveBuilder()
        .match(
            Order.class,
            msg -> {
              log.info("recover journal message: {}", msg);
              updateState(msg);
            })
        .match(
            PersistedOrder.class,
            msg -> {
              log.info("recover journal message: {}", msg);
              updateState(msg);
            })
        .build();

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
