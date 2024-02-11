package controllers

import dto.Credentials
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, Result}

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import java.time.Clock
import pdi.jwt._
import services.AuthService

import scala.util.{Failure, Success}

//implicit val clock: Clock = Clock.systemUTC
class AuthController @Inject() (val controllerComponents: ControllerComponents, val authService: AuthService)(implicit
    executionContext: ExecutionContext
) extends BaseController {
  val logger: Logger = Logger(this.getClass)

  def login(): Action[Credentials] = Action.async(parse.json[Credentials]) { implicit request =>
    val credentials: Credentials = request.body
    authService.getToken(credentials).map {
      case Some(tokenValue) => Ok(Json.obj("token" -> tokenValue))
      case None             => Unauthorized(Json.obj("message" -> "invalid username or password"))
    }

  }
}
