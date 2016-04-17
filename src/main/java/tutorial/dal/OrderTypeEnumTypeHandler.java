package tutorial.dal;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;
import tutorial.om.OrderType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(OrderType.class)
public class OrderTypeEnumTypeHandler implements TypeHandler<OrderType> {

  @Override
  public void setParameter(PreparedStatement preparedStatement, int i, OrderType orderType, JdbcType jdbcType) throws SQLException {
    preparedStatement.setString(i, orderType.getName());
  }

  @Override
  public OrderType getResult(ResultSet resultSet, String param) throws SQLException {
    return OrderType.getEnum(resultSet.getString(param));
  }

  @Override
  public OrderType getResult(ResultSet resultSet, int col) throws SQLException {
    return OrderType.getEnum(resultSet.getString(col));
  }

  @Override
  public OrderType getResult(CallableStatement callableStatement, int i) throws SQLException {
    return null;
  }
}
