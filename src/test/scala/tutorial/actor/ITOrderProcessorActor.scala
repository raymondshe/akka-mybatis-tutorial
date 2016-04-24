package tutorial.actor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import tutorial.gateway.OrderUtil._
import tutorial.om.message.{NewOrder, PreparedOrder, SequenceOrder}

import scala.language.postfixOps

class ITOrderProcessorActor extends TestKit(ActorSystem("AkkaJavaSpring")) with FlatSpecLike with ImplicitSender
  with BeforeAndAfterAll with Matchers {

  it should "generate id and persist incoming order" in {
    //given
    val orderIdGenerator = TestProbe()
    val persistence = TestProbe()
    val orderProcessor = system.actorOf(Props(classOf[OrderProcessorActor], orderIdGenerator.ref, persistence.ref.path),
      "orderProcessor")
    val order = generateRandomOrder
    //when
    orderProcessor ! new NewOrder(order)
    //then
    val receivedOrder = orderIdGenerator.expectMsg(order)
    receivedOrder should be(order)

    //given
    order.setOrderId(1)
    //when
    orderProcessor ! new SequenceOrder(order)
    //then
    val preparedOrder = persistence.expectMsgAnyClassOf(classOf[PreparedOrder])
    preparedOrder.order.getOrderId should be(1)

    //TODO: test CompleteBatch command
  }

  override def afterAll = TestKit.shutdownActorSystem(system)
}
