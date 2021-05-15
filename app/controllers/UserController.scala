package controllers

import javax.inject.Inject
import models.db.{BallotRepository, SlateRepository}
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.ExecutionContext

//@Singleton
class UserController @Inject()(scc: SilhouetteControllerComponents, ballotRepo: BallotRepository, slateRepo: SlateRepository)
                              (implicit ex: ExecutionContext) extends AbstractAuthController(scc){

  def userInfo(): Action[AnyContent] = silhouette.SecuredAction.async {
    implicit request =>
      for{
        slates <- slateRepo.getSlatesMadeByUser(request.identity)
        ballots <- ballotRepo.getBallotsForUser(request.identity)
      } yield {
        Ok(views.html.userInfo(request.identity, slates, ballots))
      }
  }

}
