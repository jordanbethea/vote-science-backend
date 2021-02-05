package models.db

import framework.DatabaseTemplate
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class QuestionTest extends PlaySpec with DatabaseTemplate with GuiceOneAppPerSuite {

  val slate1 = new Slate(-1, "Slate 1", "Slate Maker")

  val question1 = (slateID:Long) => new Question(-20, slateID, "Pick a Candidate:")
  val question2 = (slateID:Long) => new Question(-21, slateID, "Pick another one:")

  override def fakeApplication() = baseApplication

  val injector = app.injector
  lazy val slates = injector.instanceOf[SlateRepository]
  lazy val questions = injector.instanceOf[QuestionRepository]
  lazy val candidates = injector.instanceOf[CandidateRepository]

  "Questions" must {
    "create without error" in {
    }

    "require a slate to insert a new question" in {
      an[JdbcSQLIntegrityConstraintViolationException] must be thrownBy (exec(questions.add(question1(30))))
    }

    "add a question to an existing slate" in {
      val slateID = exec(slates.add(slate1))
      val questionID:Long = exec(questions.add(question1(slateID)))

      questionID must not equal (null)
    }

    "get existing question" in {
      val slateID = exec(slates.add(slate1))
      val questionID = exec(questions.add(question1(slateID)))

      val result = exec(questions.get(questionID)).getOrElse(null)
      result.text must be ("Pick a Candidate:")
    }

    "delete existing question" in {
      val slateID = exec(slates.add(slate1))
      val questionID = exec(questions.add(question1(slateID)))

      val result = exec(questions.delete(questionID))
      result must be (1)

      val getResult = exec(questions.get(questionID))
      getResult must be (None)
    }

  }
}