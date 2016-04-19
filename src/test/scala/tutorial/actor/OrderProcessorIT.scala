package tutorial.actor

import javax.inject.{Inject, Named}

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestKitBase}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike}
import org.springframework.context.annotation._
import tutorial.Config
import tutorial.dal.OrderDao
import tutorial.gateway.OrderGateway
import tutorial.spring.SpringExtension._

class OrderProcessorIT extends TestKitBase with FlatSpecLike with ImplicitSender with BeforeAndAfterAll with MockFactory {
  implicit lazy val system = OrderProcessorIT.ctx.getBean(classOf[ActorSystem])

  val orderDao: OrderDao = OrderProcessorIT.ctx.getBean(classOf[OrderDao])

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  behavior of "OrderGateway"

  it should "persist order with generated order id" in {
    /* TODO:
    2) Verify OrderProcessor is triggered with default order Id
    3) Verify OrderIdGenerator is triggered
    4) Verify OrderProcessor received order with order id generated
    5) Verify PersistenceActor is triggered
    6) Verify Dao is triggered with Prepared order.
     */
    //given
    val orderGateway = OrderProcessorIT.ctx.getBean(classOf[OrderGateway])
    //when
    orderDao.saveOrder _ expects * atLeastOnce()
    val order = orderGateway.placeOrder()
    //then
    Thread.sleep(5000)
  }
}

object OrderProcessorIT {
  lazy val ctx: AnnotationConfigApplicationContext = new AnnotationConfigApplicationContext() {
    register(classOf[TestConfig])
    refresh()
  }
}

@Import(Array(classOf[Config]))
@Configuration
class TestConfig extends FlatSpecLike with MockFactory {
  @Inject
  implicit var system: ActorSystem = null

  @Bean
  def orderDao: OrderDao = mock[OrderDao]

  @Bean
  @Named("OrderProcessorActor")
  def orderProcessor = TestActorRef(SpringExtProvider.get(system).props("OrderProcessorActor"), "orderProcessor")
}
