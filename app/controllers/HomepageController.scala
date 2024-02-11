package controllers

import actions.{AuthAction, UserRequest}
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._
import services.HomepageService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class HomepageController @Inject() (
    val controllerComponents: ControllerComponents,
    val authAction: AuthAction,
    homepageService: HomepageService
)(implicit
    execution: ExecutionContext
) extends BaseController {

  val logger: Logger = Logger(this.getClass)

  def generateHomepage(userId: Long, limit: Int, offset: Int): Action[AnyContent] = authAction.async { implicit request: UserRequest[AnyContent] =>
    logger.info("limit - " + limit.toString + " offset - " + offset.toString)
    logger.info(request.username)
    homepageService.generateHomePage(userId, limit, offset).map(results => Ok(Json.toJson(results)))
  }
}
