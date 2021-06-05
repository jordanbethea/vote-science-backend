package models.db

import models.dto.{CandidateDTO, QuestionDTO, SlateSaveDTO}

trait SampleSlates {
  val slateDTOInsert:SlateSaveDTO = SlateSaveDTO(None, "Slate 1", "Slate Maker", true, Seq(
    QuestionDTO(None, "How you doing?", Seq(
      CandidateDTO(None, "Good", "Actually it should be well"),
      CandidateDTO(None, "Bad", "Poorly, actually"))),
    QuestionDTO(None, "It's Pie time: ", Seq(
      CandidateDTO(None, "Blueberry", "Good choice"),
      CandidateDTO(None, "Sweet Potato", "Best choice")))
  ))

  val pizzaSlate = SlateSaveDTO(None, "Questions about pizza", "Pizza Fan", true, Seq(
    QuestionDTO(None, "How many pepperonis go on a pizza?", Seq(
      CandidateDTO(None, "0", "I don't like pepperoni"),
      CandidateDTO(None, "1", "Aesthetically Pleasing"),
      CandidateDTO(None, "2", "Symmetry"),
      CandidateDTO(None, "3", "Low in saturated fat"),
      CandidateDTO(None, "5", "Can appreciate each one"),
      CandidateDTO(None, "10", "An even spread"),
      CandidateDTO(None, "20", "Just pile them up!!")
    )),
    QuestionDTO(None, "Hawaiian pizza ok?", Seq(
      CandidateDTO(None, "Yes", ""),
      CandidateDTO(None, "No", ""),
      CandidateDTO(None, "Only in Hawaii", "")
    ))
  ))
}
