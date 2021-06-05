package models.dto

import play.api.data.validation.{Invalid, Valid, ValidationResult}
import org.scalatestplus.play.PlaySpec

class RankingConstraintTest extends PlaySpec {

    "Ranked data constraint" must {
      "succeed with 4 choices with correct data" in {
        //data is correct because inner seq is all for same question, has all unique candidate IDs and Ranks,
        // and all ranks are sequential starting at 1
        val correctRankedData = RankedModelDTO(Seq(Seq(
          RankedChoiceDTO(2L, 41L, 2),
          RankedChoiceDTO(2L, 42L, 4),
          RankedChoiceDTO(2L, 43L, 1),
          RankedChoiceDTO(2L, 44L, 3)
        )))

        val result:ValidationResult = RankedModelDTO.rankedValuesConstraint(correctRankedData)
        result match {
          case Valid => succeed
          case Invalid(errors) => fail(s"Returned Error: ${errors.toString}")
        }
      }
    }



}
