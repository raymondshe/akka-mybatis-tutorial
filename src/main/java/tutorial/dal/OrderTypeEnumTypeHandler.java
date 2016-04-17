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
  public void setParameter(PreparedStatement preparedStatement, int parameterIndex, OrderType orderType, JdbcType jdbcType) throws SQLException {
    preparedStatement.setString(parameterIndex, orderType.getName());
  }

  @Override
  public OrderType getResult(ResultSet resultSet, String columnName) throws SQLException {
    return OrderType.getEnum(resultSet.getString(columnName));
  }

  @Override
  public OrderType getResult(ResultSet resultSet, int columnIndex) throws SQLException {
    return OrderType.getEnum(resultSet.getString(columnIndex));
  }

  @Override
  public OrderType getResult(CallableStatement callableStatement, int columnIndex) throws SQLException {
    return OrderType.getEnum(callableStatement.getString(columnIndex));
  }
}
