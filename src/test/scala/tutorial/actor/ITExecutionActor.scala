package tutorial.actor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpecLike
import scala.language.postfixOps
import org.scalatest.matchers.should.Matchers

class ITExecutionActor extends TestKit(ActorSystem("AkkaJavaSpring")) with AnyFlatSpecLike with ImplicitSender
  with BeforeAndAfterAll with Matchers {

  it should "persist execution" in {
    //given
    val persistence = TestProbe()
    val execution = system.actorOf(Props(classOf[ExecutionActor], persistence.ref.path), "execution")
    //when
    execution ! new ExecuteOrder(111, 300)
    //then
    1 to 3 foreach { _ => persistence.expectMsgAnyClassOf(classOf[ExecutedQuantity]).orderId should be(111) }
  }

  override def afterAll = TestKit.shutdownActorSystem(system)
}
