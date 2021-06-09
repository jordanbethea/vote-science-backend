package models.dto

import play.api.data.validation.{Invalid, Valid, ValidationResult}
import org.scalatestplus.play.PlaySpec

class RankingConstraintTest extends PlaySpec {

    "Ranked data constraint" must {
      "succeed with 4 choices with correct data" in {
        //data is correct because inner seq is all for same question, has all unique candidate IDs and Ranks,
        // and all ranks are sequential starting at 1
        val correctRankedData = RankedChoiceQuestionDTO(Seq(
            RankedChoiceDTO(2L, 41L, 2),
            RankedChoiceDTO(2L, 42L, 4),
            RankedChoiceDTO(2L, 43L, 1),
            RankedChoiceDTO(2L, 44L, 3)
        ))

        val result:ValidationResult = RankedChoiceQuestionDTO.rankedValuesConstraint(correctRankedData)
        result match {
          case Valid => succeed
          case Invalid(errors) => fail(s"Returned Error: ${errors.toString}")
        }
      }

//      "Succeed with 3 Questions with 3 choices apiece, correct data" in {
//        val correctRankedData = RankedModelDTO(Seq(Seq(
//          RankedChoiceDTO(1L, 41L, 2),
//          RankedChoiceDTO(1L, 42L, 3),
//          RankedChoiceDTO(1L, 43L, 1)),
//          Seq(RankedChoiceDTO(2L, 44L, 3),
//          RankedChoiceDTO(2L, 45L, 1),
//          RankedChoiceDTO(2L, 46L, 2)),
//          Seq(RankedChoiceDTO(3L, 47L, 3),
//          RankedChoiceDTO(3L, 48L, 2),
//          RankedChoiceDTO(3L, 49L, 1)
//        )))
//        val result:ValidationResult = RankedModelDTO.rankedValuesConstraint(correctRankedData)
//        result match {
//          case Valid => succeed
//          case Invalid(errors) => fail(s"Returned Error: ${errors.toString}")
//        }
//      }

      "Fail with duplicate ranks in single question result" in {
        val duplicateRankData = RankedChoiceQuestionDTO(Seq(
          RankedChoiceDTO( 2L, 41L, 1),
          RankedChoiceDTO(2L, 42L, 1),
          RankedChoiceDTO(2L, 43L, 3)
        ))
         val result: ValidationResult = RankedChoiceQuestionDTO.rankedValuesConstraint(duplicateRankData)
        result match{
          case Valid => fail
          case Invalid(errors) => {
            errors.length must be (1)
            errors.head.message must be ("Question 2 contains duplicate rank 1")
          }
        }
      }

      "Fail with out of range rank in single question result" in {
        val outOfRangeRankData = RankedChoiceQuestionDTO(Seq(
          RankedChoiceDTO(2L, 41L, 1),
          RankedChoiceDTO(2L, 42L, 2),
          RankedChoiceDTO(2L, 43L, 5)
        ))
        val result: ValidationResult = RankedChoiceQuestionDTO.rankedValuesConstraint(outOfRangeRankData)
        result match{
          case Valid => fail
          case Invalid(errors) => {
            errors.length must be (1)
            errors.head.message must be ("Question 2 had 3 ranks submitted. The submitted rank 5 is outside of that range.")
          }
        }
      }
      "Fail with duplicate candidate chosen in single question result" in {
        val duplicateQuestionData = RankedChoiceQuestionDTO(Seq(
          RankedChoiceDTO(2L, 41L, 1),
          RankedChoiceDTO(2L, 41L, 2),
          RankedChoiceDTO(2L, 43L, 3)
        ))
        val result: ValidationResult = RankedChoiceQuestionDTO.rankedValuesConstraint(duplicateQuestionData)
        result match{
          case Valid => fail
          case Invalid(errors) => {
            errors.length must be (1)
            errors.head.message must be ("Question 2 contains duplicate candidate 41")
          }
        }
      }

//      "Fail with duplicate ranks in one question of multiple" in {
//        val duplicateRankData = RankedModelDTO(Seq(Seq(
//          RankedChoiceDTO(1L, 41L, 2),
//          RankedChoiceDTO(1L, 42L, 3),
//          RankedChoiceDTO(1L, 43L, 1)),
//          Seq(RankedChoiceDTO(2L, 44L, 3),
//          RankedChoiceDTO(2L, 45L, 1),
//          RankedChoiceDTO(2L, 46L, 2)),
//          Seq(RankedChoiceDTO(3L, 47L, 3),
//          RankedChoiceDTO(3L, 48L, 3),
//          RankedChoiceDTO(3L, 49L, 1)
//        )))
//        val result: ValidationResult = RankedModelDTO.rankedValuesConstraint(duplicateRankData)
//        result match{
//          case Valid => fail
//          case Invalid(errors) => {
//            errors.length must be (1)
//            errors.head.message must be ("Question 3 contains duplicate rank 3")
//          }
//        }
//      }

//      "Fail with duplicate candidates in one question of multiple" in {
//        val duplicateCandidateData = RankedModelDTO(Seq(Seq(
//          RankedChoiceDTO(1L, 41L, 2),
//          RankedChoiceDTO(1L, 42L, 3),
//          RankedChoiceDTO(1L, 43L, 1)),
//          Seq(RankedChoiceDTO(2L, 44L, 3),
//          RankedChoiceDTO(2L, 46L, 1),
//          RankedChoiceDTO(2L, 46L, 2)),
//          Seq(RankedChoiceDTO(3L, 47L, 3),
//          RankedChoiceDTO(3L, 48L, 2),
//          RankedChoiceDTO(3L, 49L, 1)
//        )))
//        val result: ValidationResult = RankedModelDTO.rankedValuesConstraint(duplicateCandidateData)
//        result match{
//          case Valid => fail
//          case Invalid(errors) => {
//            errors.length must be (1)
//            errors.head.message must be ("Question 2 contains duplicate candidate 46")
//          }
//        }
//      }

//      "Fail with multiple errors from multiple questions" in {
//        val manyErrorsData = RankedModelDTO(Seq(
//          Seq(RankedChoiceDTO(1L, 41L, 2),
//            RankedChoiceDTO(1L, 42L, 2),
//            RankedChoiceDTO(1L, 41L, 1)),
//          Seq(RankedChoiceDTO(2L, 44L, 3),
//            RankedChoiceDTO(2L, 46L, 1),
//            RankedChoiceDTO(2L, 46L, 2)),
//          Seq(RankedChoiceDTO(3L, 47L, 7),
//            RankedChoiceDTO(3L, 48L, 7),
//            RankedChoiceDTO(3L, 49L, 1)
//          )))
//        val result: ValidationResult = RankedModelDTO.rankedValuesConstraint(manyErrorsData)
//        result match{
//          case Valid => fail
//          case Invalid(errors) => {
//            val errorStrings = errors.map(_.message)
//            val expectedStrings = Seq(
//              "Question 1 contains duplicate candidate 41",
//              "Question 1 contains duplicate rank 2",
//              "Question 3 contains duplicate rank 7",
//              "Question 3 had 3 ranks submitted. The submitted rank 7 is outside of that range.",
//              "Question 2 contains duplicate candidate 46")
//            errorStrings must contain theSameElementsAs (expectedStrings)
//          }
//        }
//      }


    }
}
