package models.dto

import play.api.libs.json._
import models.User

/**
  * Nested version of Slate data classes, for converting from json.
  */
/* For taking in new slates and then saving them to db */
case class SlateSaveDTO(id: Option[Long], title: String, creator: String, anonymous: Boolean,
                        questions: Seq[QuestionDTO])

case class SlateLoadDTO (id: Option[Long], title: String, creator: Either[String, User],
                     questions: Seq[QuestionDTO]) {
  def creatorName():String = {
    Console.println(s"creator name: ${creator}")
    creator match {
      case Left(name) => name
      case Right(user) => user.fullName.getOrElse("")
    }
  }
}

case class QuestionDTO  (id: Option[Long], text: String, candidates: Seq[CandidateDTO])

case class CandidateDTO (id: Option[Long], name: String, description: String)



object SlateDTO {

  //reads
  implicit val candidateReads: Reads[CandidateDTO] = Json.reads[CandidateDTO]
  implicit val questionReads: Reads[QuestionDTO] = Json.reads[QuestionDTO]
  implicit val slateSaveReads: Reads[SlateSaveDTO] = Json.reads[SlateSaveDTO]
  //implicit val slateLoadReads: Reads[SlateLoadDTO] = Json.reads[SlateLoadDTO]

  //writes
  implicit val candidateWrites: Writes[CandidateDTO] = Json.writes[CandidateDTO]
  implicit val questionWrites: Writes[QuestionDTO] = Json.writes[QuestionDTO]
  implicit val slateSaveWrites: Writes[SlateSaveDTO] = Json.writes[SlateSaveDTO]
  //implicit val slateLoadWrites: Writes[SlateLoadDTO] = Json.writes[SlateLoadDTO]
}