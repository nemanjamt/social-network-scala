package controllers

import dto.{NewUser, UpdateUser, UserInfo}
import exceptions.UserExceptions.{UserNotFoundException, UserWithUsernameAlreadyExist}
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._
import services.UserService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class UserController @Inject() (val controllerComponents: ControllerComponents, userService: UserService)(implicit execution: ExecutionContext)
    extends BaseController {

  private val logger: Logger = Logger(this.getClass)
  def createUser(): Action[NewUser] = Action.async(parse.json[NewUser]) { request =>
    val newUser: NewUser = request.body
    logger.info(newUser.toString)
    userService
      .addUser(newUser)
      .map { user: UserInfo =>
        Created(Json.toJson(user))
      }
      .recover { case e: UserWithUsernameAlreadyExist =>
        logger.info(e.getMessage)
        Conflict(Json.obj("message" -> e.getMessage))
      }
  }
  def readUser(userId: Long): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    userService
      .findUser(userId)
      .map(user => Ok(Json.toJson(user)))
      .recover { case e: UserNotFoundException =>
        NotFound(Json.obj("message" -> e.getMessage))
      }
  }

  def updateUser(userId: Long): Action[UpdateUser] = Action.async(parse.json[UpdateUser]) { request =>
    val updateUser: UpdateUser = request.body
    logger.info(updateUser.toString)
    userService.updateUser(userId, updateUser).map(user => Ok(Json.toJson(user))).recover { case e: UserNotFoundException =>
      NotFound(Json.obj("message" -> e.getMessage))
    }

  }

  def deleteUser(userId: Long): Action[AnyContent] = Action.async { _ =>
    userService
      .deleteUser(userId)
      .map {
        case Some(_) =>
          Ok(Json.obj("message" -> "User successful deleted"))
        case None => NotFound(Json.obj("message" -> "User with specified id does not exist"))
      }
      .recover { case e: UserNotFoundException =>
        NotFound(Json.obj("message" -> e.getMessage))
      }
  }
}
