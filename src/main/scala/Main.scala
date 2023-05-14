import zio._

object Main extends ZIOAppDefault {
  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] =
    DoobieExample.wat.flatMap(res => Console.printLine(res))
}