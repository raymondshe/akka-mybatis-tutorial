package tutorial.om;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

final public class Order implements Serializable {
  private long orderId = -1;
  private LocalDateTime executionDate;
  private OrderType orderType;
  private BigDecimal executionPrice;
  private String symbol;
  private int userId;

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

  public OrderType getOrderType() {
    return orderType;
  }

  public void setOrderType(OrderType orderType) {
    this.orderType = orderType;
  }

  public BigDecimal getExecutionPrice() {
    return executionPrice;
  }

  public void setExecutionPrice(BigDecimal executionPrice) {
    this.executionPrice = executionPrice;
  }

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
            .add("orderId", orderId)
            .add("executionDate", executionDate)
            .add("orderType", orderType)
            .add("executionPrice", executionPrice)
            .add("symbol", symbol)
            .add("userId", userId)
            .toString();
  }
}
