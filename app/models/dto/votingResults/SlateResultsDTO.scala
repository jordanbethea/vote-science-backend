package models.dto.votingResults

import java.util.UUID

case class SlateResultsDTO (slateID: UUID, fptpResults: NonscoredResultsDTO, approvalResults: NonscoredResultsDTO,
                            rankedResults: ScoredRankResultsDTO, irvResult: RankedChoiceIRVData, rangeResult: RangeResultBySlate)

