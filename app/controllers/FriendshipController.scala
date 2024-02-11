package controllers

import actions.AuthAction
import dto.NewFriendship
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.FriendshipService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class FriendshipController @Inject() (
    val controllerComponents: ControllerComponents,
    val authAction: AuthAction,
    friendshipService: FriendshipService
)(implicit
    execution: ExecutionContext
) extends BaseController {

  val logger: Logger = Logger(this.getClass)

  def createFriendship(): Action[NewFriendship] = authAction.async(parse.json[NewFriendship]) { implicit request =>
    val newFriendship = request.body
    friendshipService.createFriendship(newFriendship).map {
      case Right(friendship) => Created(Json.toJson(friendship))
      case Left(message)     => Conflict(Json.obj("message" -> message))
    }
  }

  def deleteFriendship(friendshipId: Long): Action[AnyContent] = authAction.async { implicit request =>
    friendshipService.deleteFriendship(friendshipId).map {
      case Some(_) => Ok(Json.obj("message" -> "Friendship successful deleted"))
      case None    => NotFound(Json.obj("message" -> "Friendship with specified id does not exist"))
    }
  }

  def findFriendshipByUsers(firstUserId: Long, secondUserId: Long): Action[AnyContent] = authAction.async {
    friendshipService.findFriendshipBetweenUsers(firstUserId, secondUserId).map {
      case None          => NotFound(Json.obj("message" -> "Friendship  between users does not exist"))
      case Some(request) => Ok(Json.toJson(request))
    }
  }

  def findUserFriendships(userId: Long): Action[AnyContent] = authAction.async {
    friendshipService.findUserFriendships(userId).map(friendships => Ok(Json.toJson(friendships)))
  }
}
