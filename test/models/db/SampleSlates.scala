package models.db

import models.dto.{NewCandidateDTO, NewQuestionDTO, SlateSaveNewDTO}

trait SampleSlates {
  val slateDTOInsert:SlateSaveNewDTO = SlateSaveNewDTO("Slate 1", None, Option("Slate Maker"), Seq(
    NewQuestionDTO("How you doing?", Seq(
      NewCandidateDTO("Good", "Actually it should be well"),
      NewCandidateDTO("Bad", "Poorly, actually"))),
    NewQuestionDTO("It's Pie time: ", Seq(
      NewCandidateDTO("Blueberry", "Good choice"),
      NewCandidateDTO("Sweet Potato", "Best choice")))
  ))

  val pizzaSlate = SlateSaveNewDTO("Questions about pizza", None, Option("Pizza Fan"), Seq(
    NewQuestionDTO("How many pepperonis go on a pizza?", Seq(
      NewCandidateDTO("0", "I don't like pepperoni"),
      NewCandidateDTO("1", "Aesthetically Pleasing"),
      NewCandidateDTO("2", "Symmetry"),
      NewCandidateDTO("3", "Low in saturated fat"),
      NewCandidateDTO("5", "Can appreciate each one"),
      NewCandidateDTO("10", "An even spread"),
      NewCandidateDTO("20", "Just pile them up!!")
    )),
    NewQuestionDTO("Hawaiian pizza ok?", Seq(
      NewCandidateDTO("Yes", ""),
      NewCandidateDTO("No", ""),
      NewCandidateDTO("Only in Hawaii", "")
    ))
  ))
}
