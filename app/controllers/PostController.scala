package controllers

import models.Post
import dto.{NewPost, UpdatePost}
import exceptions.UserExceptions.UserNotFoundException
import play.api.libs.json._
import play.api.mvc._
import services.PostService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PostController @Inject() (val controllerComponents: ControllerComponents, postService: PostService)(implicit execution: ExecutionContext)
    extends BaseController {

  def createPost(): Action[NewPost] = Action.async(parse.json[NewPost]) { implicit request =>
    val newPost = request.body
    postService
      .createPost(newPost)
      .map(post => Created(Json.toJson(post)))
      .recover { case e: UserNotFoundException =>
        Conflict(Json.obj("message" -> e.getMessage))
      }
  }

  def readPost(postId: Long): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    postService.findPost(postId).map {
      case Some(post) => Ok(Json.toJson(post))
      case None       => NotFound(Json.obj("message" -> "Post with specified id does not exist"))
    }
  }

  def updatePost(postId: Long): Action[UpdatePost] = Action.async(parse.json[UpdatePost]) { implicit request =>
    val postToUpdate: UpdatePost = request.body
    postService.updatePost(postId, postToUpdate).map {
      case Some(updatedPost) => Ok(Json.toJson(updatedPost))
      case None              => NotFound(Json.obj("message" -> "Post with specified id does not exist"))

    }
  }

  def removePost(postId: Long): Action[AnyContent] = Action.async { implicit request =>
    postService.deletePost(postId).map {
      case Some(_) => Ok(Json.obj("message" -> "Post successful deleted"))
      case None    => NotFound(Json.obj("message" -> "Post with specified id does not exist"))
    }
  }

  def findPostsByUser(userId: Long): Action[AnyContent] = Action.async {
    postService.findPostsByUser(userId).map(posts => Ok(Json.toJson(posts)))
  }
}
