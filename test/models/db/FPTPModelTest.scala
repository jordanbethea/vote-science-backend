package models.db

import framework.DatabaseTemplate
import org.scalatest.Suite
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class FPTPModelTest extends PlaySpec with DatabaseTemplate with GuiceOneAppPerSuite {

  override def fakeApplication() = baseApplication

  val injector = app.injector
  val slates = injector.instanceOf[SlateRepository]
  val questions = injector.instanceOf[QuestionRepository]
  val candidates = injector.instanceOf[CandidateRepository]
  val ballots = injector.instanceOf[BallotRepository]
  val fptpModel = injector.instanceOf[FPTPRepository]

  val slate1 = new Slate(-1, "Slate 1", "Slate Maker", true)
  val question1 = (slateID:Long) => new Question(-1, slateID, "Pick a Candidate:")
  val candidate1 = (qID:Long, sID:Long) => new Candidate(-1, "Benjamin Franklin", "kite guy",sID, qID)


  "First past the post model schema" must {
    "insert a new set of fptp choices and ballot info" in {
        val slateID = exec(slates.add(slate1))
        val questionID = exec(questions.add(question1(slateID)))
        val candidate1ID = exec(candidates.add(Candidate(0, "Ben Franklin", "statesman", slateID, questionID)))
        val candidate2ID = exec(candidates.add(Candidate(0, "George Wash", "general", slateID, questionID)))

        val ballotID = exec(ballots.add(Ballot(0, slateID, "voter1", true)))
        val fptpChoice1 = exec(fptpModel.addRun(FPTPChoice(ballotID, questionID, candidate1ID)))
    }
  }

}
