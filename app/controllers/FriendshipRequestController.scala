package controllers

import dto.NewFriendshipRequest
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.{FriendshipRequestService, UserService}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FriendshipRequestController @Inject() (val controllerComponents: ControllerComponents, friendshipRequestService: FriendshipRequestService)(
    implicit execution: ExecutionContext
) extends BaseController {

  val logger: Logger = Logger(this.getClass)
  def createFriendshipRequest(): Action[NewFriendshipRequest] = Action.async(parse.json[NewFriendshipRequest]) { implicit request =>
    val newFriendshipRequest = request.body
    friendshipRequestService.createFriendshipRequest(newFriendshipRequest).map {
      case Right(friendshipRequest) => Created(Json.toJson(friendshipRequest))
      case Left(message)            => Conflict(Json.obj("message" -> message))
    }

  }

  def deleteFriendshipRequest(friendshipRequestId: Long): Action[AnyContent] = Action.async { implicit request =>
    friendshipRequestService.deleteFriendRequest(friendshipRequestId).map {
      case Some(_) => Ok(Json.obj("message" -> "Friendship request successful deleted"))
      case None    => NotFound(Json.obj("message" -> "Friendship request with specified id does not exist"))
    }
  }

  def findUserReceivedFriendshipRequest(userId: Long): Action[AnyContent] = Action.async {
    logger.info("find user received friendship requests" + userId)
    friendshipRequestService.findUserReceivedFriendshipRequest(userId).map(requests => Ok(Json.toJson(requests)))
  }

  def findUserSentFriendshipRequest(userId: Long): Action[AnyContent] = Action.async {
    logger.info("find user sent friendship requests" + userId)
    friendshipRequestService.findUserSentFriendshipRequest(userId).map(requests => Ok(Json.toJson(requests)))
  }

  def findFriendshipRequestByUsers(firstUserId: Long, secondUserId: Long): Action[AnyContent] = Action.async {
    friendshipRequestService.findFriendshipRequestBetweenUsers(firstUserId, secondUserId).map {
      case None          => NotFound(Json.obj("message" -> "Friendship request between users does not exist"))
      case Some(request) => Ok(Json.toJson(request))
    }
  }

}
