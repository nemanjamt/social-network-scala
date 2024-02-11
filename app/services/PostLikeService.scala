package services

import dto.{NewPostLike, PostLikeDetailed}
import exceptions.UserExceptions.UserNotFoundException
import models.PostLike
import play.api.Logger
import repositories.{PostLikeRepository, PostRepository, UserRepository}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PostLikeService @Inject() (
    val userRepository: UserRepository,
    val postRepository: PostRepository,
    val postLikeRepository: PostLikeRepository,
    val postService: PostService,
    val userService: UserService
)(implicit
    execution: ExecutionContext
) {
  val logger: Logger = Logger(this.getClass)

  def createPostLike(newPostLike: NewPostLike): Future[Either[String, PostLike]] = {
    userRepository.checkIfExistsById(newPostLike.userId).flatMap {
      case false => Future.successful(Left("User with specified id does not exist"))
      case true =>
        postRepository.checkIfExistPostById(newPostLike.postId).flatMap {
          case false => Future.successful(Left("Post with specified id does not exist"))
          case true =>
            postLikeRepository.checkUserAlreadyLike(newPostLike.postId, newPostLike.userId).flatMap {
              case true => Future.successful(Left("User has already liked post"))
              case false =>
                val postLikeToCreate = PostLike(id = 0, postId = newPostLike.postId, userId = newPostLike.userId)
                postLikeRepository.createPostLike(postLikeToCreate).flatMap { createdPostLike =>
                  postService.increaseCountLike(newPostLike.postId).flatMap {
                    case None    => Future.successful(Left("Post with specified id does not exist"))
                    case Some(_) => Future.successful(Right(createdPostLike))
                  }
                }
            }
        }
    }
  }

  def unlikePost(id: Long): Future[Either[String, Int]] = {
    postLikeRepository.findPostLikeById(id).flatMap {
      case None => Future.successful(Left("Post like with specified id does not exists"))
      case Some(postLike) =>
        postService.decreaseCountLike(postLike.postId).flatMap {
          case None => Future.successful(Left("Post with specified id does not exist"))
          case Some(_) =>
            postLikeRepository.deletePostLike(id).flatMap {
              case None    => Future.successful(Left("Post like with specified id does not exist"))
              case Some(_) => Future.successful(Right(1))
            }
        }
    }

  }

  def findLikesByPost(postId: Long): Future[Seq[PostLikeDetailed]] = {
    postLikeRepository.findLikesByPost(postId).flatMap { postLikes =>
      Future
        .sequence(postLikes.map { postLike =>
          userService
            .findUser(postLike.userId)
            .map(userInfo => Some(PostLikeDetailed(postLike, userInfo)))
            .recover { case e: UserNotFoundException =>
              None
            }
        })
        .map(details => details.flatten)
    }
  }

}
