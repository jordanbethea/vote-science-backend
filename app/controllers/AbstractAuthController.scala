package controllers

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api.LoginEvent
import com.mohiva.play.silhouette.api.services.AuthenticatorResult
import models.User
import play.api.mvc.RequestHeader
import scala.concurrent.{ExecutionContext, Future}

abstract class AbstractAuthController(scc: SilhouetteControllerComponents)(implicit ex: ExecutionContext)
  extends SilhouetteController(scc) {

  protected def authenticateUser(user: User, rememberMe: Boolean)(implicit request: RequestHeader): Future[AuthenticatorResult] = {
    val result = Redirect(utils.CommonRoutes.home)
    authenticatorService.create(user.loginInfo).map {
      case authenticator if rememberMe =>
        authenticator.copy(
          expirationDateTime = clock.now + scc.rememberMeConfig.expiry,
          idleTimeout = scc.rememberMeConfig.idleTimeout,
          cookieMaxAge = scc.rememberMeConfig.cookieMaxAge
        )
      case authenticator => authenticator
    }.flatMap { authenticator =>
      eventBus.publish(LoginEvent(user, request))
      authenticatorService.init(authenticator).flatMap { v =>
        authenticatorService.embed(v, result)
      }
    }
  }

}
