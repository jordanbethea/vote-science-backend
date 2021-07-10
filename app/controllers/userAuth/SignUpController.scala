package controllers.userAuth

import java.util.UUID

import com.mohiva.play.silhouette.api.{LoginInfo, SignUpEvent}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import controllers.{AbstractAuthController, AssetsFinder, SilhouetteControllerComponents}
import forms.SignUpForm
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AnyContent, Request}
import models.User
import play.api.i18n.Messages

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SignUpController @Inject() (scc: SilhouetteControllerComponents)
                                 (implicit ex: ExecutionContext, assets: AssetsFinder) extends AbstractAuthController(scc) {

  def view = UnsecuredAction.async {
    implicit request: Request[AnyContent] =>
    Future.successful(Ok(views.html.userAuth.signUpPage(SignUpForm.form)))
  }

  def submit = silhouette.UnsecuredAction.async {
    implicit request: Request[AnyContent] =>
      SignUpForm.form.bindFromRequest.fold(
        form => Future.successful(BadRequest(views.html.userAuth.signUpPage(form))),
        data => {
          val result = Redirect(routes.SignUpController.view).flashing("info" -> Messages("signup.email.sent", data.email))  //display temporary message, need to add support
          //val result = Redirect(controllers.routes.HomeController.index())
          val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
          userService.retrieve(loginInfo).flatMap {
            case Some(user) =>
              //user already exists. seed project sends email. implement some action later
              Future.successful(result)
            case None =>
              val authInfo = passwordHasherRegistry.current.hash(data.password)
              val user = User(
                userID = UUID.randomUUID(),
                loginInfo = loginInfo,
                firstName = Some(data.firstName),
                lastName = Some(data.lastName),
                fullName = Some(data.firstName + " " + data.lastName),
                email = Some(data.email),
                avatarURL = None
              )
              for {
                //avatar <- avatarService.retrieveURL(data.email)   //avatar service...maybe later
                user <- userService.save(user.copy(avatarURL = None))
                authInfo <- authInfoRepository.add(loginInfo, authInfo)
                authToken <- authTokenService.create(user.userID)
              } yield {
                // val url = routes.ActivateAccountController.activate(authToken.id).absoluteURL()
                // get url from activation controller, send email with link to activate

                eventBus.publish(SignUpEvent(user, request))
                result
              }
          }
        }
      )
  }
}
