package tutorial.actor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import tutorial.gateway.OrderUtil._

import scala.language.postfixOps
import scala.util.Random

class ITOrderProcessorActor extends TestKit(ActorSystem("AkkaJavaSpring")) with FlatSpecLike with ImplicitSender
  with BeforeAndAfterAll with Matchers {

  it should "generate id and persist incoming order" in {
    //given
    val orderIdGenerator = TestProbe()
    val persistence = TestProbe()
    val execution = TestProbe()
    val orderProcessor = orderProcessorActor(orderIdGenerator, persistence, execution)
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
  }

  it should "complete batch" in {
    //given
    val orderIdGenerator = TestProbe()
    val persistence = TestProbe()
    val execution = TestProbe()
    val orderProcessor = orderProcessorActor(orderIdGenerator, persistence, execution)
    //when
    orderProcessor ! new CompleteBatch
    //then
    orderIdGenerator.expectMsgAnyClassOf(classOf[GetCurrentOrderId])

    //when
    orderIdGenerator.reply(new CurrentOrderId(10))
    //then
    val completeBatchForId = persistence.expectMsgAnyClassOf(classOf[CompleteBatchForId])
    completeBatchForId.id should be(10)
  }

  def orderProcessorActor(orderIdGenerator: TestProbe, persistence: TestProbe, execution: TestProbe) =
    system.actorOf(Props(classOf[OrderProcessorActor], orderIdGenerator.ref, persistence.ref.path, execution.ref, false),
      "orderProcessor" + Random.nextInt)

  override def afterAll = TestKit.shutdownActorSystem(system)
}
