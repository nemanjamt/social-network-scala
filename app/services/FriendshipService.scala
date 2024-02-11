package services

import dto.{FriendshipDetailed, NewFriendship}
import exceptions.UserExceptions.UserNotFoundException
import models.Friendship
import play.api.Logger
import repositories.{FriendshipRepository, UserRepository}

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FriendshipService @Inject() (
    val userRepository: UserRepository,
    val userService: UserService,
    val friendshipRepository: FriendshipRepository,
    val friendshipRequestService: FriendshipRequestService
)(implicit
    execution: ExecutionContext
) {

  val logger: Logger = Logger(this.getClass)

  def createFriendship(newFriendship: NewFriendship): Future[Either[String, Friendship]] = {
    if (newFriendship.firstUserId == newFriendship.secondUserId) {
      return Future.successful(Left("Friendship can not be created between same user"))
    }
    userRepository.checkIfExistsById(newFriendship.firstUserId).flatMap {
      case false =>
        Future.successful(Left("User with specified id does not exist"))
      case true =>
        userRepository.checkIfExistsById(newFriendship.secondUserId).flatMap {
          case false =>
            Future.successful(Left("User with specified id does not exist"))
          case true =>
            friendshipRepository.checkIfExistFriendshipBetweenUsers(newFriendship.firstUserId, newFriendship.secondUserId).flatMap {
              case true => Future.successful(Left("Friendship is already created"))
              case false =>
                val friendshipToCreate =
                  Friendship(
                    id = 0,
                    firstUserId = newFriendship.firstUserId,
                    secondUserId = newFriendship.secondUserId,
                    createdDate = LocalDateTime.now()
                  )
                friendshipRepository.createFriendship(friendshipToCreate).flatMap { createdFriendship =>
                  friendshipRequestService.deleteFriendshipRequestsBetweenUsers(newFriendship.firstUserId, newFriendship.secondUserId)
                  Future.successful(Right(createdFriendship))
                }
            }
        }
    }
  }

  def deleteFriendship(id: Long): Future[Option[Int]] = friendshipRepository.deleteFriendship(id)
  def findUserFriendships(userId: Long): Future[Seq[FriendshipDetailed]] = {
    friendshipRepository
      .findFriendshipsByUser(userId)
      .flatMap { friendships =>
        Future
          .sequence(friendships.map { friendship =>
            val friendId = if (friendship.firstUserId == userId) friendship.secondUserId else friendship.firstUserId
            userService.findUser(friendId).map(friend => Some(FriendshipDetailed(friendship, friend))).recover { case e: UserNotFoundException =>
              None
            }
          })
          .map(details => details.flatten)
      }
  }

  def findUserFriendsIds(userId: Long): Future[Seq[Long]] = {
    friendshipRepository
      .findFriendshipsByUser(userId)
      .flatMap { friendships =>
        Future
          .sequence(friendships.map { friendship =>
            val friendId = if (friendship.firstUserId == userId) friendship.secondUserId else friendship.firstUserId
            userRepository.checkIfExistsById(friendId).map {
              case true  => Some(friendId)
              case false => None
            }
          })
          .map(details => details.flatten)
      }
  }

  def findFriendshipBetweenUsers(firstUserId: Long, secondUserId: Long): Future[Option[Friendship]] =
    friendshipRepository.findFriendshipBetweenUsers(firstUserId, secondUserId)

}
