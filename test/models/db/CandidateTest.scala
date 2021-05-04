package models.db

import framework.DatabaseTemplate
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class CandidateTest extends PlaySpec with DatabaseTemplate with GuiceOneAppPerSuite {

  override def fakeApplication() = baseApplication

  val slates = app.injector.instanceOf[SlateRepository]
  val questions = app.injector.instanceOf[QuestionRepository]
  val candidates = app.injector.instanceOf[CandidateRepository]

  val slate1 = new Slate(-1, "Slate 1", "Slate Maker", true)
  val question1 = (slateID:Long) => new Question(-1, slateID, "Pick a Candidate:")
  val candidate1 = (qID:Long, sID:Long) => new Candidate(-1, "Benjamin Franklin", "kite guy",sID, qID)

  "Candidates" must {
    "create without error" in {}

    "require a question to insert a new candidate" in {
      an[JdbcSQLIntegrityConstraintViolationException] must be thrownBy (exec(candidates.add(candidate1(30, 30))))
    }

    "create a candidate for a question" in {
      val slateID = exec(slates.add(slate1))
      val questionID = exec(questions.add(question1(slateID)))

      val candidateID = exec(candidates.add(candidate1(questionID, slateID)))

      candidateID must not be (None)
    }

    "get existing candidate" in {
      val slateID = exec(slates.add(slate1))
      val questionID = exec(questions.add(question1(slateID)))
      val candidateID = exec(candidates.add(candidate1(questionID, slateID)))

      val result = exec(candidates.get(candidateID)).getOrElse(null)
      result must not be (null)
      result.name must be ("Benjamin Franklin")
    }

    "delete existing candidate" in {
      val slateID = exec(slates.add(slate1))
      val questionID = exec(questions.add(question1(slateID)))
      val candidateID = exec(candidates.add(candidate1(questionID, slateID)))

      val deleteResult = exec(candidates.delete(candidateID))
      deleteResult must be (1)

      val getResult = exec(candidates.get(candidateID))
      getResult must be (None)
    }

  }
}