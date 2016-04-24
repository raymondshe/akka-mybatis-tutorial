package tutorial.actor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import tutorial.gateway.OrderUtil

class ITOrderIdGeneratorActor extends TestKit(ActorSystem("AkkaJavaSpring")) with FlatSpecLike with ImplicitSender
  with BeforeAndAfterAll with Matchers {
  behavior of "OrderIdGeneratorActor"

  it should "generate next order" in {
    //given
    val orderIdGenerator = system.actorOf(Props(classOf[OrderIdGeneratorActor]), "orderIdGenerator")
    val order = OrderUtil.generateRandomOrder()
    //when
    orderIdGenerator ! order
    //then
    val sequenceOrder = expectMsgAnyClassOf(classOf[SequenceOrder])
    sequenceOrder.order.getOrderId should be(1)

    //when
    orderIdGenerator ! order
    //then
    val sequenceOrder2 = expectMsgAnyClassOf(classOf[SequenceOrder])
    sequenceOrder2.order.getOrderId should be(2)
  }
}
