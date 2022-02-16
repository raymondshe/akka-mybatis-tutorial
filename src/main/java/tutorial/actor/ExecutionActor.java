package tutorial.actor;

import akka.actor.ActorPath;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import com.google.common.base.MoreObjects;
import org.springframework.context.annotation.Scope;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Named("Execution")
@Scope("prototype")
public class ExecutionActor extends UntypedAbstractActor {
  private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  private ActorPath persistence;
  private Random random = new Random();

  @Inject
  public ExecutionActor(final @Named("Persistence") ActorPath persistence) {
    this.persistence = persistence;
  }

  public static Props props(final ActorPath persistenceRouter) {
    return Props.create(new Creator<ExecutionActor>() {
      private static final long serialVersionUID = 1L;

      @Override
      public ExecutionActor create() throws Exception {
        return new ExecutionActor(persistenceRouter);
      }
    });
  }

  @Override
  public void onReceive(Object msg) throws Exception {
    if (msg instanceof ExecuteOrder) {
      log.info("Going to execute: {}", msg);
      //it has to be some gateway call to execute the quantity and only then persist
      ExecuteOrder executeOrder = (ExecuteOrder) msg;
      List<Integer> quantities = distributeQuantity(executeOrder.quantity);

      quantities
              .parallelStream()
              .forEach(q -> getContext().actorSelection(persistence)
                      .tell(new ExecutedQuantity(executeOrder.orderId, q, new Date()), self()));

    } else {
      unhandled(msg);
    }
  }

  private List<Integer> distributeQuantity(int totalQuantity) {
    return random.ints(1, totalQuantity / 3).limit(3).boxed().collect(Collectors.toList());
  }
}

class ExecuteOrder {
  public final long orderId;
  public final int quantity;

  public ExecuteOrder(long orderId, int quantity) {
    this.orderId = orderId;
    this.quantity = quantity;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
            .add("orderId", orderId)
            .add("quantity", quantity)
            .toString();
  }
}

class ExecutedQuantity {
  public final long orderId;
  public final int quantity;
  public final Date executionDate;

  public ExecutedQuantity(long orderId, int quantity, Date executionDate) {
    this.orderId = orderId;
    this.quantity = quantity;
    this.executionDate = executionDate;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
            .add("orderId", orderId)
            .add("quantity", quantity)
            .add("executionDate", executionDate)
            .toString();
  }
}