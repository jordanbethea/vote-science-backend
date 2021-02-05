package models.db

import framework.DatabaseTemplate
import models.dto._
import org.scalatest.Suite
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class BallotTest extends PlaySpec with DatabaseTemplate with GuiceOneAppPerSuite {

  override def fakeApplication() = baseApplication

  val injector = app.injector
  val ballots = injector.instanceOf[BallotRepository]
  val fptpRepo = injector.instanceOf[FPTPRepository]
  val slates = injector.instanceOf[SlateRepository]
  val questions = injector.instanceOf[QuestionRepository]
  val candidates = injector.instanceOf[CandidateRepository]

  val slateDTOInsert:SlateDTO = new SlateDTO(None, "Slate 1", "Slate Maker", Seq(
    new QuestionDTO(None, "How you doing?", Seq(
      new CandidateDTO(None, "Good", "Actually it should be well"),
      new CandidateDTO(None, "Bad", "Poorly, actually"))),
    new QuestionDTO(None, "It's Pie time: ", Seq(
      new CandidateDTO(None, "Blueberry", "Good choice"),
      new CandidateDTO(None, "Sweet Potato", "Best choice")))
  ))

  val ballotStandaloneInsert = (sID: Long) => BallotDetailsDTO(None, "The Phantom Stranger", sID)

  "Ballots table schema" must {
    "create without error" in {
      exec(ballots.listAll)
    }

    "add raw ballot data" in {
      val insertSlateResult = exec(slates.fullAdd(slateDTOInsert))
      exec(ballots.add(ballotStandaloneInsert(insertSlateResult)))
    }

    "save ballot with fptp vote model" in {
      //have to insert test slate to use with ballot
      //val insertSlateResult = exec(slates.fullAdd(slateDTOInsert))
      val slateID = exec(slates.add(Slate(0, "Test Slate 2", "Slate Maker")))
      val q1ID = exec(questions.add(Question(0, slateID, "Pick a number")))
      val q2ID = exec(questions.add(Question(0, slateID, "Pick a pie")))

      val q1c1ID = exec(candidates.add(Candidate(0, "3", "is a magic number", slateID, q1ID)))
      val q1c2ID = exec(candidates.add(Candidate(0, "4", "is a square", slateID, q1ID)))
      val q1c3ID = exec(candidates.add(Candidate(0, "234213", "is way too big", slateID, q1ID)))

      val q2c1ID = exec(candidates.add(Candidate(0, "blueberry", "is good", slateID, q2ID)))
      val q2c2ID = exec(candidates.add(Candidate(0, "sweet potato", "is good", slateID, q2ID)))
      val q2c3ID = exec(candidates.add(Candidate(0, "boston creme", "is good", slateID, q2ID)))

      val ballotDTOInsert: BallotDetailsDTO = BallotDetailsDTO(None, "Benjamin Franklin", 1l)
      val fptpDTOInsert: FPTPModelDTO = FPTPModelDTO(Seq(
        FPTPChoiceDTO(0, q1ID, q1c2ID),
        FPTPChoiceDTO(0, q2ID, q2c3ID)
      ))

      val insertBallotResult = exec(ballots.addBallotAndModelData(ballotDTOInsert, Option(fptpDTOInsert)))

      val ballot = exec(ballots.get(insertBallotResult))
      ballot.isDefined must be (true)

      val numFptpChoices = exec(fptpRepo.getChoicesForBallot(insertBallotResult))
      numFptpChoices.length must be (2)
    }
  }

}
