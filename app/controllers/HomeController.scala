package controllers

import com.mohiva.play.silhouette.api.LogoutEvent
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import javax.inject._
import play.api._
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
//class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  class HomeController @Inject() (scc: SilhouetteControllerComponents)
                                 (implicit ex: ExecutionContext) extends AbstractAuthController(scc) {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  //def index() = Action { implicit request: Request[AnyContent] =>
  def index() = silhouette.UserAwareAction{ implicit request =>
    Ok(views.html.home(request.identity))
  }

  def signOut = silhouette.SecuredAction.async { implicit request: SecuredRequest[EnvType, AnyContent] =>
    val result = Redirect(utils.CommonRoutes.home)
    eventBus.publish(LogoutEvent(request.identity, request))
    authenticatorService.discard(request.authenticator, result)
  }
}
