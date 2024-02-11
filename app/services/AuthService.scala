package services

import dto.Credentials
import pdi.jwt.{JwtAlgorithm, JwtJson}
import play.api.Logger
import play.api.libs.json.Json
import repositories.UserRepository

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class AuthService @Inject() (
    val userRepository: UserRepository,
    val userService: UserService,
    val friendshipRequestService: FriendshipRequestService
)(implicit
    execution: ExecutionContext
) {
  val logger: Logger = Logger(this.getClass)
  private val clock: Clock = Clock.systemUTC
  private val key = "kEpPF6WJjxVkYKSaWsnOWdboYxxO6ptsHyao8Jv3c49v8yKMnUWZYjRkJ7AZXBJ"
  val algo = JwtAlgorithm.HS256
  def getToken(credentials: Credentials): Future[Option[String]] = {
    userRepository.checkIfExistsWithUsernameAndPassword(credentials.username, credentials.password).map {
      case true =>
        val exp = clock.millis()
        val claim = Json.obj(("username", credentials.username), ("exp", exp))

        val token = JwtJson.encode(claim, key, algo)
        Some(token)
      case false => None
    }
  }

  def validateJwt(auth: String): Future[(Boolean, String)] = {
    JwtJson.decodeJson(auth, key, Seq(algo)) match {
      case Success(decoded) =>
        val claims = Json.parse(decoded.toString())
        val username = (claims \ "username").as[String]
        Future.successful((true, username))
      case Failure(_) =>
        Future.successful((false, ""))

    }
  }

}
