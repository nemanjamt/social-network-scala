package repositories

import dto.NewPostLike
import models.PostLike
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape
import slick.sql.SqlProfile.ColumnOption.NotNull

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PostLikeRepository @Inject() (
    val dbConfigProvider: DatabaseConfigProvider,
    val userRepository: UserRepository,
    val postRepository: PostRepository
)(implicit
    ec: ExecutionContext
) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._
  private val postLikes = TableQuery[PostLikeTable]

  def createPostLike(newPostLike: PostLike): Future[PostLike] = {
    db.run(
      (postLikes returning postLikes.map(_.id)
        into ((postLike, id) => postLike.copy(id = id))) += newPostLike
    )
  }

  def deletePostLike(id: Long): Future[Option[Int]] = {
    db.run(postLikes.filter(_.id === id).delete.map {
      case 0       => None
      case 1       => Some(1)
      case deleted => throw new RuntimeException(s"Deleted $deleted rows")
    })
  }

  def findLikesByPost(postId: Long): Future[Seq[PostLike]] = {
    db.run(
      postLikes.filter(postLike => postLike.postId === postId).result
    )
  }

  def findPostLikeById(postLikeId: Long): Future[Option[PostLike]] = {
    db.run(
      postLikes.filter(postLike => postLike.id === postLikeId).result
    ).map(_.headOption)
  }

  def checkUserAlreadyLike(postId: Long, userId: Long): Future[Boolean] = {
    db.run(
      postLikes.filter(postLike => postLike.postId === postId && postLike.userId === userId).exists.result
    )
  }

  class PostLikeTable(tag: Tag) extends Table[PostLike](tag, "post_likes") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Long]("userId", NotNull)
    def postId = column[Long]("postId", NotNull)
    def user = foreignKey("user_fk", userId, TableQuery[userRepository.UserTable])(_.id)
    def post = foreignKey("post_fk", postId, TableQuery[postRepository.PostTable])(_.id)

    override def * : ProvenShape[PostLike] =
      (id, userId, postId) <> ((PostLike.apply _).tupled, PostLike.unapply)
  }
}
