package tutorial.dal;

import tutorial.om.Order;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class OrderDao {
  @Autowired
  private SqlSession sqlSession;

  public void saveOrder(Order order) {
    sqlSession.insert("tutorial.dal.OrderDao.saveOrder", order);
  }
}
