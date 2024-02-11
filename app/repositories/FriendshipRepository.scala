package repositories

import models.Friendship
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape
import slick.sql.SqlProfile.ColumnOption.NotNull

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FriendshipRepository @Inject() (val dbConfigProvider: DatabaseConfigProvider, val userRepository: UserRepository)(implicit
    ec: ExecutionContext
) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._
  private val friendships = TableQuery[FriendshipTable]

  def createFriendship(newFriendship: Friendship): Future[Friendship] = {
    db.run(
      (friendships returning friendships.map(_.id)
        into ((friendship, id) => friendship.copy(id = id))) += newFriendship
    )
  }

  def findFriendshipsByUser(userId: Long): Future[Seq[Friendship]] = {
    db.run(
      friendships.filter(f => (f.firstUserId === userId || f.secondUserId === userId)).result
    )
  }

  def deleteFriendship(id: Long): Future[Option[Int]] = {
    db.run(friendships.filter(_.id === id).delete.map {
      case 0       => None
      case 1       => Some(1)
      case deleted => throw new RuntimeException(s"Deleted $deleted rows")
    })
  }

  def findFriendshipBetweenUsers(firstUser: Long, secondUser: Long): Future[Option[Friendship]] = {
    db.run(
      friendships
        .filter(f => (f.firstUserId === firstUser && f.secondUserId === secondUser) || (f.secondUserId === firstUser && f.firstUserId === secondUser))
        .result
        .headOption
    )
  }

  def checkIfExistFriendshipBetweenUsers(firstUser: Long, secondUser: Long): Future[Boolean] = {
    db.run(
      friendships
        .filter(f => (f.firstUserId === firstUser && f.secondUserId === secondUser) || (f.secondUserId === firstUser && f.firstUserId === secondUser))
        .exists
        .result
    )
  }

  class FriendshipTable(tag: Tag) extends Table[Friendship](tag, "friendships") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def firstUserId = column[Long]("firstUserId", NotNull)
    def secondUserId = column[Long]("secondUserId", NotNull)
    def createdDate = column[LocalDateTime]("createdDate", NotNull)
    def firstUser = foreignKey("first_user_fk", firstUserId, TableQuery[userRepository.UserTable])(_.id)
    def secondUser = foreignKey("second_user_fk", secondUserId, TableQuery[userRepository.UserTable])(_.id)

    override def * : ProvenShape[Friendship] =
      (id, firstUserId, secondUserId, createdDate) <> ((Friendship.apply _).tupled, Friendship.unapply)
  }

}
