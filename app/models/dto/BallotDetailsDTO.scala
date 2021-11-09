package models.dto

import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.libs.json._

import java.util.UUID

case class BallotDTO(details: BallotDetailsDTO, fptpModel: Option[FPTPModelDTO] = None, approvalModel: Option[ApprovalModelDTO] = None,
                     rankedModel: Option[RankedModelDTO] = None, rangeModel: Option[RangeModelDTO] = None)

object BallotDTO {
  implicit val ballotWrites: Writes[BallotDTO] = Json.writes[BallotDTO]
  implicit val ballotReads: Reads[BallotDTO] = Json.reads[BallotDTO]
}

case class BallotDetailsDTO(id: Option[UUID], voterID: Option[UUID], slateID: UUID, anonVoter: Option[String])

object BallotDetailsDTO {
  implicit val ballotDetailsWrites: Writes[BallotDetailsDTO] = Json.writes[BallotDetailsDTO]
  implicit val ballotDetailsReads: Reads[BallotDetailsDTO] = Json.reads[BallotDetailsDTO]
}

case class FPTPModelDTO (choices: Seq[FPTPChoiceDTO])

object FPTPModelDTO {
  implicit val fptpModelWrites: Writes[FPTPModelDTO] = Json.writes[FPTPModelDTO]
  implicit val fptpModelReads: Reads[FPTPModelDTO] = Json.reads[FPTPModelDTO]
}

case class FPTPChoiceDTO (questionID: UUID, candidateID: UUID)

object FPTPChoiceDTO {
  implicit val fptpChoiceWrites: Writes[FPTPChoiceDTO] = Json.writes[FPTPChoiceDTO]
  implicit val fptpChoiceReads: Reads[FPTPChoiceDTO] = Json.reads[FPTPChoiceDTO]
}

case class ApprovalModelDTO (choices: Seq[Seq[ApprovalChoiceDTO]])

object ApprovalModelDTO {
  implicit val approvalModelWrites: Writes[ApprovalModelDTO] = Json.writes[ApprovalModelDTO]
  implicit val approvalModelReads: Reads[ApprovalModelDTO] = Json.reads[ApprovalModelDTO]
}

case class ApprovalChoiceDTO (questionID:UUID, candidateID:UUID, approved:Boolean)

object ApprovalChoiceDTO{
  implicit val approvalChoiceWrites: Writes[ApprovalChoiceDTO] = Json.writes[ApprovalChoiceDTO]
  implicit val approvalChoiceReads: Reads[ApprovalChoiceDTO] = Json.reads[ApprovalChoiceDTO]
}

case class RankedModelDTO (questions: Seq[RankedChoiceQuestionDTO])

object RankedModelDTO {
  implicit val rankedModelWrites: Writes[RankedModelDTO] = Json.writes[RankedModelDTO]
  implicit val rankedModelReads: Reads[RankedModelDTO] = Json.reads[RankedModelDTO]
}

case class RankedChoiceQuestionDTO(choices: Seq[RankedChoiceDTO])

object RankedChoiceQuestionDTO {
  implicit val rankedChoiceQuestionWrites: Writes[RankedChoiceQuestionDTO] = Json.writes[RankedChoiceQuestionDTO]
  implicit val rankedChoiceQuestionReads: Reads[RankedChoiceQuestionDTO] = Json.reads[RankedChoiceQuestionDTO]

  val rankedValuesConstraint: Constraint[RankedChoiceQuestionDTO] = Constraint("constraints.rankedCheck")({
    qChoices =>
      def checkDuplicates(remainingChoices: Seq[RankedChoiceDTO],
                          ranksUsed: Set[Int], candidatesUsed: Set[UUID],
                          accruedErrors: Seq[ValidationError]): Seq[ValidationError] = {
        if (remainingChoices == Nil) {
          val expectedRanks = 1 to qChoices.choices.size
          val unexpectedRankErrors = for (
            rank <- ranksUsed
            if !expectedRanks.contains(rank))
          yield
            ValidationError("voting.error.rankingExcessRank", qChoices.choices.size, rank)

          unexpectedRankErrors.toSeq ++ accruedErrors
        }
        else {
          val duplicateRank = if (ranksUsed.contains(remainingChoices.head.rank)) {
            Option(ValidationError("voting.error.rankingDuplicateRank", remainingChoices.head.rank))
          } else None
          val duplicateCandidate = if (candidatesUsed.contains(remainingChoices.head.candidateID)) {
            Option(ValidationError("voting.error.rankingDuplicateCandidate", remainingChoices.head.candidateID))
          } else None
          val updatedErrors = accruedErrors ++ duplicateRank ++ duplicateCandidate
          checkDuplicates(remainingChoices.tail, ranksUsed + remainingChoices.head.rank,
            candidatesUsed + remainingChoices.head.candidateID, updatedErrors)
        }
      }

      val errors = checkDuplicates(qChoices.choices, Set(), Set(), Nil)

      if (errors.isEmpty) {
        Valid
      } else {
        Invalid(errors)
      }
  })
}

case class RankedChoiceDTO (questionID:UUID, candidateID:UUID, rank:Int)

object RankedChoiceDTO {
  implicit val rankedChoiceWrites: Writes[RankedChoiceDTO] = Json.writes[RankedChoiceDTO]
  implicit val rankedChoiceReads: Reads[RankedChoiceDTO] = Json.reads[RankedChoiceDTO]
}

case class RangeModelDTO (choices: Seq[Seq[RangeChoiceDTO]])

object RangeModelDTO {
  implicit val rangeModelWrites: Writes[RangeModelDTO] = Json.writes[RangeModelDTO]
  implicit val rangeModelReads: Reads[RangeModelDTO] = Json.reads[RangeModelDTO]
}

case class RangeChoiceDTO (questionID:UUID, candidateID:UUID, score:Int)

object RangeChoiceDTO {
  implicit val rangeChoiceWrites: Writes[RangeChoiceDTO] = Json.writes[RangeChoiceDTO]
  implicit val rangeChoiceReads: Reads[RangeChoiceDTO] = Json.reads[RangeChoiceDTO]
}