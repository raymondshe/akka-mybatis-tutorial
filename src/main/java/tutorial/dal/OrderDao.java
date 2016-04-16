package tutorial.dal;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import tutorial.om.Order;

import java.util.List;

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
}
