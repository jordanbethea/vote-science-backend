package models.dto

import play.api.data.validation.{Invalid, Valid, ValidationResult}
import org.scalatestplus.play.PlaySpec

import java.util.UUID

class RankingConstraintTest extends PlaySpec {

    "Ranked data constraint" must {
      val questionID = UUID.randomUUID()
      "succeed with 4 choices with correct data" in {
        //data is correct because inner seq is all for same question, has all unique candidate IDs and Ranks,
        // and all ranks are sequential starting at 1
        val correctRankedData = RankedChoiceQuestionDTO(Seq(
            RankedChoiceDTO(questionID, UUID.randomUUID(), 2),
            RankedChoiceDTO(questionID, UUID.randomUUID(), 4),
            RankedChoiceDTO(questionID, UUID.randomUUID(), 1),
            RankedChoiceDTO(questionID, UUID.randomUUID(), 3)
        ))

        val result:ValidationResult = RankedChoiceQuestionDTO.rankedValuesConstraint(correctRankedData)
        result match {
          case Valid => succeed
          case Invalid(errors) => fail(s"Returned Error: ${errors.toString}")
        }
      }

      "Fail with duplicate ranks in single question result" in {
        val duplicateRankData = RankedChoiceQuestionDTO(Seq(
          RankedChoiceDTO( questionID, UUID.randomUUID(), 1),
          RankedChoiceDTO(questionID, UUID.randomUUID(), 1),
          RankedChoiceDTO(questionID, UUID.randomUUID(), 3)
        ))
         val result: ValidationResult = RankedChoiceQuestionDTO.rankedValuesConstraint(duplicateRankData)
        result match{
          case Valid => fail
          case Invalid(errors) => {
            errors.length must be (1)
            errors.head.message must be ("voting.error.rankingDuplicateRank")
            errors.head.args.head must be (1)
          }
        }
      }

      "Fail with out of range rank in single question result" in {
        val outOfRangeRankData = RankedChoiceQuestionDTO(Seq(
          RankedChoiceDTO(questionID, UUID.randomUUID(), 1),
          RankedChoiceDTO(questionID, UUID.randomUUID(), 2),
          RankedChoiceDTO(questionID, UUID.randomUUID(), 5)
        ))
        val result: ValidationResult = RankedChoiceQuestionDTO.rankedValuesConstraint(outOfRangeRankData)
        result match{
          case Valid => fail
          case Invalid(errors) => {
            errors.length must be (1)
            errors.head.message must be ("voting.error.rankingExcessRank")
            errors.head.args.head must be (3)
            errors.head.args.tail.head must be (5)
          }
        }
      }
      "Fail with duplicate candidate chosen in single question result" in {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val duplicateQuestionData = RankedChoiceQuestionDTO(Seq(
          RankedChoiceDTO(questionID, id1, 1),
          RankedChoiceDTO(questionID, id1, 2),
          RankedChoiceDTO(questionID, id2, 3)
        ))
        val result: ValidationResult = RankedChoiceQuestionDTO.rankedValuesConstraint(duplicateQuestionData)
        result match{
          case Valid => fail
          case Invalid(errors) => {
            errors.length must be (1)
            errors.head.message must be ("voting.error.rankingDuplicateCandidate")
            errors.head.args.head must be (id1)
          }
        }
      }
    }
}
