package tutorial

import org.scalatest.FlatSpec
import org.springframework.boot.SpringApplication

class AppTest extends FlatSpec {
  val app = new SpringApplication(classOf[App])
  app.setWebEnvironment(false)
  val context = Option(app.run(Array.empty[String]: _*))

  behavior of "OrderGateway"

  it should "persist order with generated order id" in {
    /* TODO:
    1) Get OrderGateway and place an order
    2) Verify OrderProcessor is triggered with default order Id
    3) Verify OrderIdGenerator is triggered
    4) Verify OrderProcessor received order with order id generated
    5) Verify PersistenceActor is triggered
    6) Verify Dao is triggered with Prepared order.
    7) Mock Dao to avoid database usage
     */
    1 == 1
  }
}
