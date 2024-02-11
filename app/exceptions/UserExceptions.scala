package exceptions

object UserExceptions {
  class UserNotFoundException(message: String) extends RuntimeException(message: String)
  class UserWithUsernameAlreadyExist(message: String) extends RuntimeException(message: String)
}
