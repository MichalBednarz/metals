package tests.codeactions

import scala.meta.internal.metals.codeactions.ImportMissingSymbol
import scala.meta.internal.metals.codeactions.CreateNewSymbol

class ImportMissingSymbolLspSuite
    extends BaseCodeActionLspSuite("importMissingSymbol") {

  check(
    "basic",
    """|package a
       |
       |object A {
       |  val f = <<Future>>.successful(2)
       |}
       |""".stripMargin,
    s"""|${ImportMissingSymbol.title("Future", "scala.concurrent")}
        |${ImportMissingSymbol.title("Future", "java.util.concurrent")}
        |${CreateNewSymbol.title("Future")}
        |""".stripMargin,
    """|package a
       |
       |import scala.concurrent.Future
       |
       |object A {
       |  val f = Future.successful(2)
       |}
       |""".stripMargin
  )

  check(
    "enclosed-range",
    """|package a
       |
       |object A {
       |  val f = Fu<<tu>>re.successful(2)
       |}
       |""".stripMargin,
    s"""|${ImportMissingSymbol.title("Future", "scala.concurrent")}
        |${ImportMissingSymbol.title("Future", "java.util.concurrent")}
        |${CreateNewSymbol.title("Future")}
        |""".stripMargin,
    """|package a
       |
       |import scala.concurrent.Future
       |
       |object A {
       |  val f = Future.successful(2)
       |}
       |""".stripMargin
  )

  check(
    "multi-same-line-ambiguous",
    """|package a
       |
       |object A {
       |  val f = <<Future.successful(Instant.now)>>
       |  val b = ListBuffer.newBuilder[Int]
       |}
       |""".stripMargin,
    s"""|${ImportMissingSymbol.title("Future", "scala.concurrent")}
        |${ImportMissingSymbol.title("Future", "java.util.concurrent")}
        |${ImportMissingSymbol.title("Instant", "java.time")}
        |${CreateNewSymbol.title("Future")}
        |${CreateNewSymbol.title("Instant")}
        |""".stripMargin,
    """|package a
       |
       |import scala.concurrent.Future
       |
       |object A {
       |  val f = Future.successful(Instant.now)
       |  val b = ListBuffer.newBuilder[Int]
       |}
       |""".stripMargin,
    expectNoDiagnostics = false
  )

  check(
    "multi-across-lines-non-ambiguous",
    """|package a
       |
       |import scala.concurrent.Future
       |
       |object A {
       |  val f = Future.successful(<<Instant.now)
       |  val b = ListBuffer.newBuilder[Int]>>
       |}
       |""".stripMargin,
    s"""|${ImportMissingSymbol.allSymbolsTitle}
        |${ImportMissingSymbol.title("Instant", "java.time")}
        |${ImportMissingSymbol.title("ListBuffer", "scala.collection.mutable")}
        |${CreateNewSymbol.title("Instant")}
        |${CreateNewSymbol.title("ListBuffer")}
        |""".stripMargin,
    """|package a
       |
       |import scala.concurrent.Future
       |import java.time.Instant
       |import scala.collection.mutable
       |
       |object A {
       |  val f = Future.successful(Instant.now)
       |  val b = mutable.ListBuffer.newBuilder[Int]
       |}
       |""".stripMargin,
    expectNoDiagnostics = false
  )

}
