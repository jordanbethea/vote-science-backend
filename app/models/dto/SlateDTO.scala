package models.dto

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._


/**
  * Nested version of Slate data classes, for converting from json.
  */
case class SlateDTO (id: Option[Long], title: String, creator: String, anonymous: Boolean, questions: Seq[QuestionDTO])

case class QuestionDTO  (id: Option[Long], text: String, candidates: Seq[CandidateDTO])

case class CandidateDTO (id: Option[Long], name: String, description: String)



object SlateDTO {

  implicit val candidateReads: Reads[CandidateDTO] = (
    (JsPath \ "id").readNullable[Long] and
      (JsPath \ "name").read[String] and
      (JsPath \ "description").read[String]
    )(CandidateDTO.apply _)

  implicit val questionReads: Reads[QuestionDTO] = (
    (JsPath \ "id").readNullable[Long] and
      (JsPath \ "text").read[String] and
      (JsPath \ "candidates").read[Seq[CandidateDTO]]
    )(QuestionDTO.apply _ )

  implicit val slateReads: Reads[SlateDTO] = (
    (JsPath \ "id").readNullable[Long] and
      (JsPath \ "title").read[String] and
      (JsPath \ "creator").read[String] and
      (JsPath \ "anonymous").read[Boolean] and
      (JsPath \ "questions").read[Seq[QuestionDTO]]
    )(SlateDTO.apply _)

  implicit val candidateWrites: Writes[CandidateDTO] = Json.writes[CandidateDTO]

  implicit val questionWrites: Writes[QuestionDTO] = Json.writes[QuestionDTO]

  implicit val slateWrites: Writes[SlateDTO] = Json.writes[SlateDTO]
}