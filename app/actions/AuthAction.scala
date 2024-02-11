package actions

import play.api.mvc.Results.Unauthorized
import play.api.mvc._
import services.AuthService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class UserRequest[A](username: String, request: Request[A]) extends WrappedRequest[A](request)
class AuthAction @Inject() (bodyParser: BodyParsers.Default, authService: AuthService)(implicit ec: ExecutionContext)
    extends ActionBuilder[UserRequest, AnyContent] {

  override def parser: BodyParser[AnyContent] = bodyParser
  override protected def executionContext: ExecutionContext = ec
  private def extractToken(authHeader: String): Option[String] =
    authHeader.split("Bearer ").drop(1).headOption
  override def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] =
    request.headers.get("Authorization") match {
      case Some(auth) =>
        extractToken(auth) match {
          case Some(token) =>
            authService
              .validateJwt(token)
              .flatMap {
                case (true, username) => block(UserRequest(username, request))
                case (false, _)       => Future.successful(Unauthorized)
              }
          case None => Future.successful(Unauthorized)
        }

      case _ =>
        Future.successful(Unauthorized)
    }
}
