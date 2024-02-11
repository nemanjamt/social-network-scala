package services

import dto.{NewUser, UpdateUser, UserInfo}
import exceptions.UserExceptions.{UserNotFoundException, UserWithUsernameAlreadyExist}
import models.User
import play.api.Logger
import repositories.UserRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserService @Inject() (val userRepository: UserRepository)(implicit execution: ExecutionContext) {

  val logger: Logger = Logger(this.getClass)
  def addUser(user: NewUser): Future[UserInfo] = {
    val usernameExist = userRepository.checkIfExistsWithUsername(user.username)
    usernameExist.flatMap {
      case true =>
        logger.info("Username " + user.username + " already exist")
        Future.failed(new UserWithUsernameAlreadyExist("User with specified username already exist"))
      case false =>
        logger.info("Create user " + user.toString)
        val newUser = User(0, user.firstName, user.lastName, user.username, user.password, deleted = false)
        userRepository
          .createUser(newUser)
          .map(createdUser =>
            UserInfo(id = createdUser.id, firstName = createdUser.firstName, lastName = createdUser.lastName, username = createdUser.username)
          )
    }
  }

  def findUser(userId: Long): Future[UserInfo] = {
    userRepository.findUser(userId).map {
      case Some(u) => UserInfo(id = u.id, firstName = u.firstName, lastName = u.lastName, username = u.username)
      case None    => throw new UserNotFoundException("User with specified id does not exist")
    }
  }

  def updateUser(userId: Long, updateUser: UpdateUser): Future[UserInfo] = {
    userRepository.findUser(userId).flatMap {
      case None => Future.failed(new UserNotFoundException("User with specified id does not exist"))
      case Some(userToUpdate) =>
        logger.info("FOUND USER TO UPDATE")
        userRepository
          .updateUser(
            userToUpdate.copy(firstName = updateUser.firstName, lastName = updateUser.lastName, password = updateUser.password)
          )
          .map {
            case Some(updatedUser) =>
              UserInfo(id = updatedUser.id, firstName = updatedUser.firstName, lastName = updatedUser.lastName, username = updatedUser.username)

            case None => { throw new UserNotFoundException("User with specified id does not exist") }
          }

    }
  }

  def deleteUser(userId: Long): Future[Option[Int]] = {
    userRepository.findUser(userId).flatMap {
      case None => Future.failed(new UserNotFoundException("User with specified id does not exist"))
      case Some(userToUpdate) =>
        logger.info("FOUND USER TO DELETE")
        val updatedUser = userToUpdate.copy(deleted = true)
        userRepository.deleteUser(updatedUser)
    }
  }

}
