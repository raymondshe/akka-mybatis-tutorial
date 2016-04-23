package tutorial.actor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import tutorial.dal.OrderDao
import tutorial.gateway.OrderUtil
import tutorial.om.message.{PersistedOrder, PreparedOrder}

class ITPersistenceActor extends TestKit(ActorSystem("AkkaJavaSpring")) with FlatSpecLike with ImplicitSender
  with BeforeAndAfterAll with Matchers with MockFactory {
  behavior of "PersistenceActor"

  it should "persist order" in {
    //given
    val orderDao = mock[OrderDao]
    val persistenceActor = system.actorOf(Props(classOf[PersistenceActor], orderDao), "persistenceActor")
    val order = OrderUtil.generateRandomOrder()
    //when
    orderDao.saveOrder _ expects order
    persistenceActor ! new PreparedOrder(1L, order)
    //then
    val persistedOrder = expectMsgAnyClassOf(classOf[PersistedOrder])
    persistedOrder.order should be(order)
  }
}
