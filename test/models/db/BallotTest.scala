package models.db

import framework.DatabaseTemplate
import models.dto._
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import java.util.UUID

class BallotTest extends PlaySpec with DatabaseTemplate with GuiceOneAppPerSuite with SampleSlates {

  override def fakeApplication() = baseApplication

  val injector = app.injector
  val ballots = injector.instanceOf[BallotRepository]
  val slates = injector.instanceOf[SlateRepository]

  val ballotStandaloneInsert = (sID: UUID) => BallotDTO(BallotDetailsDTO(None, None, sID, Option("The Phantom Stranger")))

  "Ballots table schema" must {
    "create without error" in {
      exec(ballots.listAll())
    }

    "add raw ballot data" in {
      val insertSlateResult = exec(slates.fullAdd(slateDTOInsert))
      exec(ballots.saveBallot(ballotStandaloneInsert(insertSlateResult)))
    }

    "save ballot with fptp vote model" in {
      //have to insert test slate to use with ballot
      val insertSlateResult = exec(slates.fullAdd(slateDTOInsert))
      /*val slateID = exec(slates.addDTO(SlateSaveDTO(Option(0), "Test Slate 2", "Slate Maker", false)))
      val q1ID = exec(questions.addDTO(QuestionDTO(Option(0), slateID, "Pick a number")))
      val q2ID = exec(questions.addDTO(QuestionDTO(Option(0), slateID, "Pick a pie")))

      val q1c1ID = exec(candidates.add(Candidate(0, "3", "is a magic number", slateID, q1ID)))
      val q1c2ID = exec(candidates.add(Candidate(0, "4", "is a square", slateID, q1ID)))
      val q1c3ID = exec(candidates.add(Candidate(0, "234213", "is way too big", slateID, q1ID)))

      val q2c1ID = exec(candidates.add(Candidate(0, "blueberry", "is good", slateID, q2ID)))
      val q2c2ID = exec(candidates.add(Candidate(0, "sweet potato", "is good", slateID, q2ID)))
      val q2c3ID = exec(candidates.add(Candidate(0, "boston creme", "is good", slateID, q2ID))) */

      //TODO - find some workaround for needing the individual question IDs here
      /* val ballotDetailsDTOInsert: BallotDetailsDTO = BallotDetailsDTO(None, "Benjamin Franklin", 1L, false)
      val fptpDTOInsert: FPTPModelDTO = FPTPModelDTO(Seq(
        FPTPChoiceDTO(q1ID, q1c2ID),
        FPTPChoiceDTO(q2ID, q2c3ID)
      ))
      val ballotDTO: BallotDTO = BallotDTO(ballotDetailsDTOInsert, Option(fptpDTOInsert))

      val insertBallotResult = exec(ballots.saveBallot(ballotDTO))

      val ballot = exec(ballots.get(insertBallotResult))
      ballot.isDefined must be (true)

      val numFptpChoices = exec(fptpRepo.getChoicesForBallot(insertBallotResult))
      numFptpChoices.length must be (2) */
    }
  }

}
