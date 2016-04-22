package tutorial.actor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import tutorial.gateway.OrderUtil._
import tutorial.om.message.{NewOrder, PreparedOrder}

class OrderProcessorActorTest extends TestKit(ActorSystem("MySpec")) with FlatSpecLike with ImplicitSender
  with BeforeAndAfterAll with Matchers {

  it should "process incoming order" in {
    //given
    val orderIdGenerator = TestProbe()
    val persistence = TestProbe()
    val orderProcessor = system.actorOf(Props(new OrderProcessorActor(orderIdGenerator.ref, persistence.ref.path)))
    val order = generateRandomOrder
    //when
    orderProcessor ! new NewOrder(order)
    //then
    val receivedOrder = orderIdGenerator.expectMsg(order)
    receivedOrder should be (order)
    val preparedOrder = persistence.expectMsgAnyClassOf(classOf[PreparedOrder])
    preparedOrder.order.getOrderId shouldNot be(-1)
  }

  override def afterAll = TestKit.shutdownActorSystem(system)
}
