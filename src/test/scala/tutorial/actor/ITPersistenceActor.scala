package tutorial.actor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import tutorial.dal.OrderDao
import tutorial.gateway.OrderUtil

import scala.util.Random

class ITPersistenceActor extends TestKit(ActorSystem("AkkaJavaSpring")) with FlatSpecLike with ImplicitSender
  with BeforeAndAfterAll with Matchers with MockFactory {
  behavior of "PersistenceActor"

  it should "persist order" in {
    //given
    val orderDao = mock[OrderDao]
    val persistenceActor = actor(orderDao)
    val order = OrderUtil.generateRandomOrder()
    //when
    orderDao.saveOrder _ expects order
    persistenceActor ! new PreparedOrder(1L, order)
    //then
    val persistedOrder = expectMsgAnyClassOf(classOf[PersistedOrder])
    persistedOrder.order should be(order)
  }

  it should "complete batch" in {
    //given
    val orderDao = mock[OrderDao]
    val persistenceActor = actor(orderDao)
    //when
    orderDao.completeBatch _ expects 1L
    persistenceActor ! new CompleteBatchForId(1L)
    //then
    val batchCompleted = expectMsgAnyClassOf(classOf[BatchCompleted])
    batchCompleted.id should be(1)
  }

  def actor(orderDao: OrderDao) = system.actorOf(Props(classOf[PersistenceActor], orderDao, false), "persist" + Random.nextInt())
}
