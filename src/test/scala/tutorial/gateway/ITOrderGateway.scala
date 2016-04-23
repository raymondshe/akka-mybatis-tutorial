package tutorial.gateway

import javax.inject.{Inject, Named}

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestKitBase}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike}
import org.springframework.context.annotation._
import tutorial.Config
import tutorial.dal.OrderDao
import tutorial.spring.SpringExtension._

class ITOrderGateway extends TestKitBase with FlatSpecLike with ImplicitSender with BeforeAndAfterAll with MockFactory {
  lazy val ctx: AnnotationConfigApplicationContext = new AnnotationConfigApplicationContext() {
    register(classOf[TestConfig])
    refresh()
  }
  implicit lazy val system = ctx.getBean(classOf[ActorSystem])

  override def afterAll = TestKit.shutdownActorSystem(system)

  behavior of "OrderGateway"

  it should "persist order with generated order id" in {
    //given
    val orderDao = ctx.getBean(classOf[OrderDao])
    val orderGateway = ctx.getBean(classOf[OrderGateway])
    //when
    orderDao.saveOrder _ expects * atLeastOnce()
    orderGateway.placeOrder()
    //then
    Thread.sleep(3000)
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
  @Named("OrderProcessor")
  def orderProcessor = system.actorOf(SpringExtProvider.get(system).props("OrderProcessor"), "orderProcessor")
}
