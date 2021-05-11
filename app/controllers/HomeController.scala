package controllers

import com.mohiva.play.silhouette.api.LogoutEvent
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import javax.inject._
import play.api.mvc._

import scala.concurrent.ExecutionContext


@Singleton
class HomeController @Inject() (scc: SilhouetteControllerComponents)
                                 (implicit ex: ExecutionContext) extends AbstractAuthController(scc) {

  /**
   * Site Home page
   */
  def index() = silhouette.UserAwareAction{ implicit request =>
    Ok(views.html.home(request.identity))
  }

  def signOut = silhouette.SecuredAction.async { implicit request: SecuredRequest[EnvType, AnyContent] =>
    val result = Redirect(utils.CommonRoutes.home)
    eventBus.publish(LogoutEvent(request.identity, request))
    authenticatorService.discard(request.authenticator, result)
  }
}
