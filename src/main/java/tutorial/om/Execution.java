package tutorial.om;

import com.google.common.base.MoreObjects;

import java.time.LocalDateTime;

public class Execution {
  private long executionId;
  private long orderId;
  private LocalDateTime executionDate;
  private int quantity;

  public long getExecutionId() {
    return executionId;
  }

  public void setExecutionId(long executionId) {
    this.executionId = executionId;
  }

  public long getOrderId() {
    return orderId;
  }

  public void setOrderId(long orderId) {
    this.orderId = orderId;
  }

  public LocalDateTime getExecutionDate() {
    return executionDate;
  }

  public void setExecutionDate(LocalDateTime executionDate) {
    this.executionDate = executionDate;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
            .add("executionId", executionId)
            .add("orderId", orderId)
            .add("executionDate", executionDate)
            .add("quantity", quantity)
            .toString();
  }
}
