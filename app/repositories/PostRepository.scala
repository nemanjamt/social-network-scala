package repositories

import models.Post

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape
import slick.sql.SqlProfile.ColumnOption.NotNull
import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PostRepository @Inject() (val dbConfigProvider: DatabaseConfigProvider, val userRepository: UserRepository)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._
  private val posts = TableQuery[PostTable]

  def createPost(post: Post): Future[Post] = {
    db.run(
      (posts returning posts.map(_.id)
        into ((post, id) => post.copy(id = id))) += post
    )
  }

  def checkIfExistPostById(id: Long): Future[Boolean] = {
    db.run(
      posts.filter(p => p.id === id).exists.result
    )
  }

  def findPostsByFriendsIds(userFriendsIds: Seq[Long], limit: Int, offset: Int): Future[(Seq[Post], Int) ] = {
    db.run(
      for {
        friendsPosts <- posts.filter(p => p.userId.inSet(userFriendsIds)).sortBy(_.createdDate.desc).drop(offset).take(limit).result
        totalCount <- posts.filter(p => p.userId.inSet(userFriendsIds)).length.result
      } yield (friendsPosts, totalCount)
    )
  }

  def findPost(postId: Long): Future[Option[Post]] = {
    db.run(
      posts.filter(_.id === postId).filter(!_.deleted).result
    ).map(_.headOption)
  }

  def updatePost(post: Post): Future[Option[Post]] = {
    db.run(posts.filter(_.id === post.id).update(post).map {
      case 0       => None
      case 1       => Some(post)
      case updated => throw new RuntimeException(s"Updated $updated rows")
    })
  }

  def deletePost(post: Post): Future[Option[Int]] = {
    db.run(posts.filter(_.id === post.id).update(post).map {
      case 0       => None
      case 1       => Some(1)
      case deleted => throw new RuntimeException(s"Deleted $deleted rows")
    })
  }

  def findPostsByUser(userId: Long): Future[Seq[Post]] = {
    db.run(
      posts.filter(!_.deleted).filter(_.userId === userId).result
    )
  }

  class PostTable(tag: Tag) extends Table[Post](tag, "posts") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Long]("userId", NotNull)
    def content = column[String]("content", NotNull)
    def countLike = column[Int]("countLike", NotNull)
    def deleted = column[Boolean]("deleted", NotNull)
    def createdDate = column[LocalDateTime]("createdDate", NotNull)
    def updatedDate = column[Option[LocalDateTime]]("updatedDate")
    def user = foreignKey("user_fk", userId, TableQuery[userRepository.UserTable])(_.id)
    override def * : ProvenShape[Post] =
      (id, userId, content, countLike, deleted, createdDate, updatedDate) <> ((Post.apply _).tupled, Post.unapply)

  }
}
