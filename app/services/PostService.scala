package services

import dto.{NewPost, UpdatePost}
import exceptions.UserExceptions.UserNotFoundException
import models.Post
import play.api.Logger
import repositories.{PostRepository, UserRepository}

import java.time.LocalDateTime
import javax.inject.Inject
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ExecutionContext, Future}

class PostService @Inject() (val postRepository: PostRepository, val userRepository: UserRepository)(implicit execution: ExecutionContext) {

  val logger: Logger = Logger(this.getClass)
  def createPost(post: NewPost): Future[Post] = {
    userRepository.checkIfExistsById(post.userId).flatMap {
      case false => Future.failed(new UserNotFoundException("User with specified id does not exist"))
      case true =>
        val newPost =
          Post(
            id = 0,
            userId = post.userId,
            content = post.content,
            countLike = 0,
            deleted = false,
            createDate = LocalDateTime.now(),
            updateDate = None
          )
        postRepository.createPost(newPost)
    }

  }

  def findPost(postId: Long): Future[Option[Post]] =
    postRepository.findPost(postId)

  def updatePost(postId: Long, updatePost: UpdatePost): Future[Option[Post]] = {
    val post = findPost(postId)
    post.flatMap {
      case None => Future.successful(None)
      case Some(postToUpdate) =>
        logger.info("FOUND POST TO UPDATE")
        val updatedPost = postToUpdate.copy(countLike = updatePost.countLike, content = updatePost.content, updateDate = Some(LocalDateTime.now()))
        postRepository.updatePost(updatedPost)
    }
  }

  def increaseCountLike(postId: Long): Future[Option[Post]] = {
    findPost(postId).flatMap {
      case None => Future.successful(None)
      case Some(post) => {
        val updatedPost = post.copy(countLike = post.countLike + 1)
        postRepository.updatePost(updatedPost)
      }
    }
  }

  def decreaseCountLike(postId: Long): Future[Option[Post]] = {
    findPost(postId).flatMap {
      case None => Future.successful(None)
      case Some(post) => {
        val updatedPost = post.copy(countLike = post.countLike - 1)
        postRepository.updatePost(updatedPost)
      }
    }
  }

  def deletePost(postId: Long): Future[Option[Int]] = {
    val post = findPost(postId)
    post.flatMap {
      case None => Future.successful(None)
      case Some(postToUpdate) =>
        logger.info("FOUND POST TO DELETE")
        val updatedPost = postToUpdate.copy(deleted = true)
        postRepository.deletePost(updatedPost)
    }
  }

  def findPostsByUser(userId: Long): Future[Seq[Post]] = {
    postRepository.findPostsByUser(userId)
  }
}
