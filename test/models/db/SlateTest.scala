package models.db

import framework.DatabaseTemplate
import models.dto.{CandidateDTO, QuestionDTO, SlateDTO}
import org.scalatest.Suite
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class SlateTest extends PlaySpec with DatabaseTemplate with GuiceOneAppPerSuite {


  val insert1:Slate = new Slate(-20, "Slate 1", "Slate Maker", true)
  val insert2:Slate = new Slate(-21, "Slate 2", "Slate Maker", true)
  val insert3:Slate = new Slate(-22, "Slate 3", "Other Slate Maker", true)
  val insert4:Slate = new Slate(-23, "Slate 4", "Other Slate Maker", true)

  val insertResult = (slateID:Long) => new Slate(slateID, "Slate 1", "Slate Maker", true)

  val slateDTOInsert:SlateDTO = new SlateDTO(None, "Slate 1", "Slate Maker", true, Seq(
    new QuestionDTO(None, "How you doing?", Seq(
      new CandidateDTO(None, "Good", "Actually it should be well"),
      new CandidateDTO(None, "Bad", "Poorly, actually"))),
    new QuestionDTO(None, "It's Pie time: ", Seq(
      new CandidateDTO(None, "Blueberry", "Good choice"),
      new CandidateDTO(None, "Sweet Potato", "Best choice")))
  ))

  override def fakeApplication() = baseApplication
  val injector = app.injector
  val slates = injector.instanceOf[SlateRepository]
  val questions = injector.instanceOf[QuestionRepository]
  val candidates = injector.instanceOf[CandidateRepository]

  "Slate table schema" must {
    "create without error" in {
      exec(slates.listAll)
    }

    "insert a new slate" in {
      val slateID = exec(slates.add(insert1))

      val result:Seq[Slate] = exec(slates.listAll)
      result must contain (insertResult(slateID))
    }

    "insert multiple slates" in {
      val inserts = Seq(insert1, insert2, insert3, insert4)

      val insertIDs = exec(slates.addAll(inserts))

      insertIDs must not contain(None)

      val getResults = exec(slates.get(insertIDs.head))
      getResults must not be (None)
    }

    "get an existing slate by id" in {
      val inserts = Seq(insert1, insert2, insert3, insert4)

      val results = exec(slates.addAll(inserts))

      val result:Slate = exec(slates.get(results(0))).getOrElse(null)
      result must be (insertResult(results(0)))
    }

    "delete an existing slate" in {
      val inserts = Seq(insert1, insert2, insert3, insert4)

      val results: Seq[Long] = exec(slates.addAll(inserts))

      slates.delete(results(0))

      val resultSingle = exec(slates.get(results(0)))
      resultSingle mustBe empty
    }

    "insert an entire nested slate with data" in {
      val insertResult = exec(slates.fullAdd(slateDTOInsert))

      val getSlate = exec(slates.get(insertResult))
      getSlate must not be (None)

      val getQs = exec(questions.getForSlate(insertResult))
      getQs.length must be (2)

      val getCs = exec(candidates.getForQuestions(getQs.map(_.id)))
      getCs.length must be (4)
    }
  }
}