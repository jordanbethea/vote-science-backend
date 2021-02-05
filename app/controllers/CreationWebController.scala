package controllers

import javax.inject._
import models.db.SlateRepository
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class CreationWebController @Inject()(slatesRepo: SlateRepository,
                                       val controllerComponents: ControllerComponents)
                                     (implicit ex: ExecutionContext) extends BaseController {
  /**
   * Renders the form to create a new slate
   */
  def createSlateForm() = Action {

    Ok(views.html.createSlateForm())
  }

}
