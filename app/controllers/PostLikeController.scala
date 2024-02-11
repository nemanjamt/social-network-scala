package controllers

import dto.NewPostLike
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.PostLikeService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PostLikeController @Inject() (val controllerComponents: ControllerComponents, postLikeService: PostLikeService)(implicit
    execution: ExecutionContext
) extends BaseController {

  val logger: Logger = Logger(this.getClass)
  def createPostLike(): Action[NewPostLike] = Action.async(parse.json[NewPostLike]) { implicit request =>
    val newPostLike = request.body
    postLikeService.createPostLike(newPostLike).map {
      case Right(postLike) => Created(Json.toJson(postLike))
      case Left(message)   => Conflict(Json.obj("message" -> message))
    }

  }

  def unlikePost(postLikeId: Long): Action[AnyContent] = Action.async {
    postLikeService.unlikePost(postLikeId).map {
      case Right(_)      => Ok(Json.obj("message" -> "Post successful unliked"))
      case Left(message) => NotFound(Json.obj("message" -> message))
    }
  }

  def findPostLikes(postId: Long): Action[AnyContent] = Action.async {
    postLikeService.findLikesByPost(postId).map(postLikes => Ok(Json.toJson(postLikes)))
  }

}
