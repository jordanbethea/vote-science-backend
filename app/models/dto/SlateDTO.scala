package models.dto

import play.api.libs.json._
import models.User

import java.util.UUID

/**
  * Nested version of Slate data classes, for converting from json.
  */
/* For taking in new slates and then saving them to db */
case class SlateSaveNewDTO(title: String, creatorID: Option[UUID], anonCreator: Option[String],
                           questions: Seq[NewQuestionDTO])

case class NewQuestionDTO(text: String, candidates: Seq[NewCandidateDTO])

case class NewCandidateDTO(name: String, description: String)



/* For loading slates from the db and using them to populate pages and other things */
case class SlateLoadDTO (id: UUID, title: String, creator: Either[String, User],
                     questions: Seq[LoadQuestionDTO]) {
  def creatorName():String = {
    Console.println(s"creator name: ${creator}")
    creator match {
      case Left(name) => name
      case Right(user) => user.fullName.getOrElse("")
    }
  }

  def candidateName(candidateID:UUID):Option[String] = {
    questions.flatMap(_.candidates).find(_.id == candidateID).flatMap(s => Option(s.name))
  }
}

case class LoadQuestionDTO(id: UUID, text: String, candidates: Seq[LoadCandidateDTO])

case class LoadCandidateDTO(id: UUID, name: String, description: String)





object SlateDTO {

  //reads
  implicit val candidateReads: Reads[NewCandidateDTO] = Json.reads[NewCandidateDTO]
  implicit val questionReads: Reads[NewQuestionDTO] = Json.reads[NewQuestionDTO]
  implicit val slateSaveReads: Reads[SlateSaveNewDTO] = Json.reads[SlateSaveNewDTO]
  //implicit val slateLoadReads: Reads[SlateLoadDTO] = Json.reads[SlateLoadDTO]

  //writes
  implicit val candidateWrites: Writes[NewCandidateDTO] = Json.writes[NewCandidateDTO]
  implicit val questionWrites: Writes[NewQuestionDTO] = Json.writes[NewQuestionDTO]
  implicit val slateSaveWrites: Writes[SlateSaveNewDTO] = Json.writes[SlateSaveNewDTO]
  //implicit val slateLoadWrites: Writes[SlateLoadDTO] = Json.writes[SlateLoadDTO]
}