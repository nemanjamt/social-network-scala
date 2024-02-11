package services

import dto.{PaginatedResult, PostDetailed}
import play.api.Logger
import repositories.{PostLikeRepository, PostRepository, UserRepository}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HomepageService @Inject() (
    val userRepository: UserRepository,
    val friendshipService: FriendshipService,
    val userService: UserService,
    val postRepository: PostRepository,
    val friendshipRequestService: FriendshipRequestService,
    val postLikeRepository: PostLikeRepository
)(implicit
    execution: ExecutionContext
) {
  val logger: Logger = Logger(this.getClass)

  def generateHomePage(userId: Long, limit: Int, offset: Int): Future[PaginatedResult[PostDetailed]] = {
    friendshipService
      .findUserFriendsIds(userId)
      .flatMap(friendIds => {
        postRepository.findPostsByFriendsIds(friendIds, limit, offset).flatMap { case (posts, totalCount) =>
          val entities = Future.sequence(posts.map { post =>
            for {
              postUserInfo <- userService.findUser(post.userId)
              userLiked <- postLikeRepository.checkUserAlreadyLike(post.id, userId)
            } yield PostDetailed(post, postUserInfo, userLiked)
          })
          val currentPage: Int = (offset / limit) + 1

          val totalPage: Int = if (Math.ceil(totalCount / limit).asInstanceOf[Int] > 0) Math.ceil(totalCount / limit).asInstanceOf[Int] else 1
          logger.info(currentPage.toString)
          logger.info(totalPage.toString)

          entities
            .map(entitiesSeq => PaginatedResult(totalCount = totalCount, entities = entitiesSeq, currentPage = currentPage, totalPage = totalPage))
        }
      })

  }

}
