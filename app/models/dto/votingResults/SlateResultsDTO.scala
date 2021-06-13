package models.dto.votingResults

case class SlateResultsDTO (slateID: Long, fptpResults: NonscoredResultsDTO, approvalResults: NonscoredResultsDTO,
                            rankedResults: ScoredRankResultsDTO, irvResult: RankedChoiceIRVData, rangeResult: RangeResultBySlate)

