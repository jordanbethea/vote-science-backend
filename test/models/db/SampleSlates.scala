package models.db

import models.dto.{NewCandidateDTO, NewQuestionDTO, SlateSaveNewDTO}

trait SampleSlates {
  val slateDTOInsert:SlateSaveNewDTO = SlateSaveDTO(None, "Slate 1", "Slate Maker", true, Seq(
    NewQuestionDTO(None, "How you doing?", Seq(
      NewCandidateDTO(None, "Good", "Actually it should be well"),
      NewCandidateDTO(None, "Bad", "Poorly, actually"))),
    NewQuestionDTO(None, "It's Pie time: ", Seq(
      NewCandidateDTO(None, "Blueberry", "Good choice"),
      NewCandidateDTO(None, "Sweet Potato", "Best choice")))
  ))

  val pizzaSlate = SlateSaveDTO(None, "Questions about pizza", "Pizza Fan", true, Seq(
    NewQuestionDTO(None, "How many pepperonis go on a pizza?", Seq(
      NewCandidateDTO(None, "0", "I don't like pepperoni"),
      NewCandidateDTO(None, "1", "Aesthetically Pleasing"),
      NewCandidateDTO(None, "2", "Symmetry"),
      NewCandidateDTO(None, "3", "Low in saturated fat"),
      NewCandidateDTO(None, "5", "Can appreciate each one"),
      NewCandidateDTO(None, "10", "An even spread"),
      NewCandidateDTO(None, "20", "Just pile them up!!")
    )),
    NewQuestionDTO(None, "Hawaiian pizza ok?", Seq(
      NewCandidateDTO(None, "Yes", ""),
      NewCandidateDTO(None, "No", ""),
      NewCandidateDTO(None, "Only in Hawaii", "")
    ))
  ))
}
