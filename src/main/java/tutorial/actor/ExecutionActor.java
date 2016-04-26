package tutorial.actor;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.common.base.MoreObjects;
import org.springframework.context.annotation.Scope;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Named("Execution")
@Scope("prototype")
public class ExecutionActor extends UntypedActor {
  private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  private ActorRef persistence;
  private Random random = new Random();

  @Inject
  public ExecutionActor(final @Named("Persistence") ActorRef persistence) {
    this.persistence = persistence;
  }

  @Override
  public void onReceive(Object msg) throws Exception {
    if (msg instanceof ExecuteOrder) {
      log.info("Execute order: {}", msg);
      //it has to be some gateway call to execute the quantity and then persist
      ExecuteOrder executeOrder = (ExecuteOrder) msg;
      List<Integer> quantities = distributeQuantities(executeOrder.quantity);
      quantities
              .parallelStream()
              .forEach(q -> persistence.tell(new ExecutedQuantity(executeOrder.orderId, q), self()));

    } else {
      unhandled(msg);
    }
  }

  private List<Integer> distributeQuantities(int totalQuantity) {
    List<Integer> result = new ArrayList<>();
    int currentSum = 0;

    while (currentSum < totalQuantity) {
      int quantity = random.nextInt(totalQuantity - currentSum);
      result.add(quantity);
      currentSum += quantity;
    }

    return result;
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
    return MoreObjects.toStringHelper(this)
            .add("orderId", orderId)
            .add("quantity", quantity)
            .toString();
  }
}


class ExecutedQuantity {
  public final long orderId;
  public final int quantity;

  public ExecutedQuantity(long orderId, int quantity) {
    this.orderId = orderId;
    this.quantity = quantity;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
            .add("orderId", orderId)
            .add("quantity", quantity)
            .toString();
  }
}
