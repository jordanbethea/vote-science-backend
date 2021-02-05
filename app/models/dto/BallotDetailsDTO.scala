package models.dto

import play.api.libs.json._

case class BallotDTO(details: BallotDetailsDTO, fptpModel: Option[FPTPModelDTO])

object BallotDTO {
  implicit val ballotWrites: Writes[BallotDTO] = Json.writes[BallotDTO]
  implicit val ballotReads: Reads[BallotDTO] = Json.reads[BallotDTO]
}

case class BallotDetailsDTO(id: Option[Long], voter: String, slateID: Long)

object BallotDetailsDTO {
  implicit val ballotDetailsWrites: Writes[BallotDetailsDTO] = Json.writes[BallotDetailsDTO]
  implicit val ballotDetailsReads: Reads[BallotDetailsDTO] = Json.reads[BallotDetailsDTO]
}

case class FPTPModelDTO (choices: Seq[FPTPChoiceDTO])

object FPTPModelDTO {
  implicit val fptpModelWrites: Writes[FPTPModelDTO] = Json.writes[FPTPModelDTO]
  implicit val fptpModelReads: Reads[FPTPModelDTO] = Json.reads[FPTPModelDTO]
}

case class FPTPChoiceDTO (ballotID: Long, questionID: Long, candidateID: Long)

object FPTPChoiceDTO {
  implicit val fptpChoiceWrites: Writes[FPTPChoiceDTO] = Json.writes[FPTPChoiceDTO]
  implicit val fptpChoiceReads: Reads[FPTPChoiceDTO] = Json.reads[FPTPChoiceDTO]
}