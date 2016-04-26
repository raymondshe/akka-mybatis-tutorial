package tutorial.dal;

import com.google.common.collect.ImmutableMap;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import tutorial.om.Order;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public class OrderDao {
  @Autowired
  private SqlSession sqlSession;

  public void saveOrder(Order order) {
    sqlSession.insert("tutorial.dal.OrderDao.saveOrder", order);
  }

  public List<Order> getOrders() {
    return sqlSession.selectList("tutorial.dal.OrderDao.getOrders");
  }

  public void completeBatch(long id) {
    Map<String, Object> params = ImmutableMap.of("id", id, "date", new Date());
    sqlSession.update("tutorial.dal.OrderDao.completeBatch", params);
  }

  public void insertExecution(long orderId, int quantity) {
    Map<String, Object> params = ImmutableMap.of("orderId", orderId, "quantity", quantity, "date", new Date());
    sqlSession.insert("tutorial.dal.OrderDao.insertExecution", params);
  }
}
