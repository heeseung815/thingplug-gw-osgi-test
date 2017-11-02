package module.main
/*
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.inject.{AbstractModule, Guice, Inject, Module}

/**
  * Created by mrhjkim on 2017. 8. 3..
  */
object GuiceTest extends App{

  class root extends AbstractModule {
    implicit val actorSystem = ActorSystem("test")
    implicit val m = ActorMaterializer()
    override def configure(): Unit = {
      bind(classOf[ActorSystem]).toInstance(actorSystem)
      bind(classOf[ActorMaterializer]).toInstance(m)
    }
  }

  trait Atrait {
    def name = "default"
  }
  class Aclass @Inject()(implicit a:ActorSystem, m:ActorMaterializer) extends Atrait{
    println(s"$a, $m")

    override def name: String = "Aclass"
  }
  class AChild extends AbstractModule {
    override def configure(): Unit = {

      bind(classOf[Atrait]).to(classOf[Aclass])
    }
  }
  val buf = Array[Module](new root(), new AChild())
  val injector = Guice.createInjector(buf:_*)
  val a = injector.getInstance(classOf[Atrait])

}
*/