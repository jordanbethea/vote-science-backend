package controllers

import com.mohiva.play.silhouette.api.{EventBus, Silhouette}
import com.mohiva.play.silhouette.api.actions.{SecuredActionBuilder, UnsecuredActionBuilder}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util.{Clock, PasswordHasherRegistry}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider

import javax.inject.Inject
import play.api.Logging
import play.api.http.FileMimeTypes
import play.api.i18n.{I18nSupport, Langs, MessagesApi}
import play.api.libs.mailer.MailerClient
import play.api.mvc.{AnyContent, DefaultActionBuilder, MessagesAbstractController, MessagesActionBuilder, MessagesControllerComponents, PlayBodyParsers}
import services.{AuthTokenService, MailService, UserService}
import utils.DefaultEnv

import scala.concurrent.duration.FiniteDuration

abstract class SilhouetteController(override protected val controllerComponents: SilhouetteControllerComponents)
  extends MessagesAbstractController(controllerComponents) with SilhouetteComponents with I18nSupport with Logging {

  def SecuredAction: SecuredActionBuilder[EnvType, AnyContent] = controllerComponents.silhouette.SecuredAction
  def UnsecuredAction: UnsecuredActionBuilder[EnvType, AnyContent] = controllerComponents.silhouette.UnsecuredAction

  def userService: UserService = controllerComponents.userService
  def authInfoRepository: AuthInfoRepository = controllerComponents.authInfoRepository
  def passwordHasherRegistry: PasswordHasherRegistry = controllerComponents.passwordHasherRegistry
  def authTokenService: AuthTokenService = controllerComponents.authTokenService
  def rememberMeConfig: RememberMeConfig = controllerComponents.rememberMeConfig
  def clock: Clock = controllerComponents.clock
  def credentialsProvider: CredentialsProvider = controllerComponents.credentialsProvider
  def mailerClient: MailerClient = controllerComponents.mailerClient
  def mailService: MailService = controllerComponents.mailService

  def silhouette: Silhouette[EnvType] = controllerComponents.silhouette
  def authenticatorService: AuthenticatorService[AuthType] = silhouette.env.authenticatorService
  def eventBus: EventBus = silhouette.env.eventBus
}

trait SilhouetteComponents {
  type EnvType = DefaultEnv
  type AuthType = EnvType#A
  type IdentityType = EnvType#I

  def userService: UserService
  def authInfoRepository: AuthInfoRepository
  def passwordHasherRegistry: PasswordHasherRegistry
  def authTokenService: AuthTokenService
  def rememberMeConfig: RememberMeConfig
  def clock: Clock
  def credentialsProvider: CredentialsProvider
  def mailerClient: MailerClient
  def mailService: MailService

  def silhouette: Silhouette[EnvType]
}

trait SilhouetteControllerComponents extends MessagesControllerComponents with SilhouetteComponents

final case class DefaultSilhouetteControllerComponents @Inject()
  (
    silhouette: Silhouette[DefaultEnv],
    userService: UserService,
    authInfoRepository: AuthInfoRepository,
    passwordHasherRegistry: PasswordHasherRegistry,
    authTokenService: AuthTokenService,
    rememberMeConfig: RememberMeConfig,
    clock: Clock,
    credentialsProvider: CredentialsProvider,
    actionBuilder: DefaultActionBuilder,
    messagesActionBuilder: MessagesActionBuilder,
    parsers: PlayBodyParsers,
    langs: Langs,
    fileMimeTypes: FileMimeTypes,
    messagesApi: MessagesApi,
    executionContext: scala.concurrent.ExecutionContext,
    mailerClient: MailerClient,
    mailService: MailService
  ) extends SilhouetteControllerComponents

trait RememberMeConfig {
  def expiry: FiniteDuration
  def idleTimeout: Option[FiniteDuration]
  def cookieMaxAge: Option[FiniteDuration]
}

final case class DefaultRememberMeConfig
(
  expiry: FiniteDuration,
  idleTimeout: Option[FiniteDuration],
  cookieMaxAge: Option[FiniteDuration]
) extends RememberMeConfig

