package services

import dto.{FriendshipRequestDetailed, NewFriendshipRequest, UserInfo}
import exceptions.UserExceptions.UserNotFoundException
import models.FriendshipRequest
import play.api.Logger
import repositories.{FriendshipRepository, FriendshipRequestRepository, UserRepository}

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FriendshipRequestService @Inject() (
    val userRepository: UserRepository,
    val userService: UserService,
    val friendshipRequestRepository: FriendshipRequestRepository,
    val friendshipRepository: FriendshipRepository
)(implicit
    execution: ExecutionContext
) {
  val logger: Logger = Logger(this.getClass)
  def createFriendshipRequest(request: NewFriendshipRequest): Future[Either[String, FriendshipRequest]] = {
    if (request.senderId == request.receiverId) {
      return Future.successful(Left("User can not send yourself friendship request"))
    }
    friendshipRepository.checkIfExistFriendshipBetweenUsers(request.senderId, request.receiverId).flatMap {
      case true =>
        logger.info("EXIST!")
        Future.successful(Left("Friendship already exist"))
      case false =>
        userRepository.checkIfExistsById(request.receiverId).flatMap {
          case false =>
            Future.successful(Left("User with specified id does not exist"))
          case true =>
            userRepository.checkIfExistsById(request.senderId).flatMap {
              case false =>
                Future.successful(Left("User with specified id does not exist"))
              case true =>
                friendshipRequestRepository.checkIfExistFriendshipRequestBetweenUsers(request.receiverId, request.senderId).flatMap {
                  case true => Future.successful(Left("Friendship requests is already created"))
                  case false =>
                    val newFriendshipRequest =
                      FriendshipRequest(id = 0, receiverId = request.receiverId, senderId = request.senderId, createdDate = LocalDateTime.now())
                    friendshipRequestRepository.createFriendshipRequest(newFriendshipRequest).flatMap { request =>
                      Future.successful(Right(request))
                    }
                }
            }
        }
    }

  }

  def deleteFriendRequest(id: Long): Future[Option[Int]] = friendshipRequestRepository.deleteFriendshipRequest(id)

  def findUserReceivedFriendshipRequest(userId: Long): Future[Seq[FriendshipRequestDetailed]] = {
    friendshipRequestRepository
      .findReceivedUsersFriendshipRequests(userId)
      .flatMap { requests =>
        Future
          .sequence(requests.map { request =>
            userService.findUser(request.senderId).map(senderInfo => Some(FriendshipRequestDetailed(request, senderInfo))).recover {
              case e: UserNotFoundException => None
            }
          })
          .map(details => details.flatten)
      }
  }

  def findUserSentFriendshipRequest(userId: Long): Future[Seq[FriendshipRequest]] =
    friendshipRequestRepository.findSentUsersFriendshipRequests(userId)

  def findFriendshipRequestBetweenUsers(firstUserId: Long, secondUserId: Long): Future[Option[FriendshipRequest]] =
    friendshipRequestRepository.findFriendshipRequestBetweenUsers(firstUserId, secondUserId)

  def deleteFriendshipRequestsBetweenUsers(firstUserId: Long, secondUserId: Long): Future[Option[Int]] =
    friendshipRequestRepository.deleteFriendshipRequestsBetweenUsers(firstUserId, secondUserId)

}
