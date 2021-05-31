package controllers

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent}
import services.{BallotService, SlateService}

import scala.concurrent.ExecutionContext

//@Singleton
class UserController @Inject()(scc: SilhouetteControllerComponents,
                               ballotService: BallotService,
                               slateService: SlateService)
                              (implicit ex: ExecutionContext) extends AbstractAuthController(scc){

  def userInfo(): Action[AnyContent] = silhouette.SecuredAction.async {
    implicit request =>
      for{
        createdSlates <- slateService.slatesByUser(request.identity)
        ballots <- ballotService.ballotsByVoter(request.identity)
        votedSlates <- slateService.slatesFromList(ballots.map(_.details.slateID))
      } yield {
        Ok(views.html.userInfo(request.identity, createdSlates, votedSlates, ballots))
      }
  }

}
