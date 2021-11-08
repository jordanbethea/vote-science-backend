package models.db

import framework.DatabaseTemplate
import models.dto.{NewCandidateDTO, NewQuestionDTO, SlateDTO, SlateSaveNewDTO}
import org.scalatest.Suite
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class SlateTest extends PlaySpec with DatabaseTemplate with GuiceOneAppPerSuite with SampleSlates {

  override def fakeApplication() = baseApplication
  val injector = app.injector
  val slates = injector.instanceOf[SlateRepository]

  "Slate table schema" must {
    "create without error" in {
      exec(slates.listAll)
    }

    "insert an entire nested slate with data" in {
      val insertResult = exec(slates.fullAdd(slateDTOInsert))

      val getSlate = exec(slates.getSingleSlate(insertResult))
      getSlate must not be (None)
      getSlate.get.questions.length must be (2)
      getSlate.get.equals(slateDTOInsert)
    }
  }
}