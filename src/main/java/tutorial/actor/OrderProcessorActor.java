package tutorial.actor;

import akka.actor.ActorPath;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import akka.japi.Function;
import akka.persistence.UntypedPersistentActorWithAtLeastOnceDelivery;
import org.springframework.context.annotation.Scope;
import tutorial.om.Order;
import tutorial.om.message.NewOrder;
import tutorial.om.message.PersistedOrder;
import tutorial.om.message.PreparedOrder;
import tutorial.om.message.SequenceOrder;

import javax.inject.Inject;
import javax.inject.Named;

@Named("OrderProcessorActor")
@Scope("prototype")
public class OrderProcessorActor extends UntypedPersistentActorWithAtLeastOnceDelivery {
  private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  private ActorRef orderIdGenerator;
  private ActorPath persistenceRouter;

  @Inject
  public OrderProcessorActor(
          @Named("OrderIdGenerator") ActorRef orderIdGenerator, @Named("PersistenceActor") ActorPath persistenceRouter) {
    this.orderIdGenerator = orderIdGenerator;
    this.persistenceRouter = persistenceRouter;
  }

  public static Props props(final ActorRef orderIdGenerator, final ActorPath persistenceRouter) {
    return Props.create(new Creator<OrderProcessorActor>() {
      private static final long serialVersionUID = 1L;

      @Override
      public OrderProcessorActor create() throws Exception {
        return new OrderProcessorActor(orderIdGenerator, persistenceRouter);
      }
    });
  }

  @Override
  public void onReceiveCommand(Object msg) throws Exception {
    if (msg instanceof NewOrder) {
      log.info("New order received: {}", ((NewOrder) msg).order);
      NewOrder newOrder = (NewOrder) msg;
      orderIdGenerator.tell(newOrder.order, getSelf());

    } else if (msg instanceof SequenceOrder) {
      Order order = ((SequenceOrder) msg).order;
      log.info("Order id generated: {}, for order: {}", order.getOrderId(), order);
      persist(order, this::updateState);

    } else if (msg instanceof PersistedOrder) {
      updateState(msg);
      log.info("Order with id = '{}' has been successfully persisted.", ((PersistedOrder) msg).order.getOrderId());

    } else {
      unhandled(msg);
    }
  }

  @Override
  public void onReceiveRecover(Object msg) throws Exception {
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
    return "order-processor-persistence";
  }
}
