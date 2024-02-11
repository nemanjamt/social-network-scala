package repositories

import models.FriendshipRequest
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape
import slick.sql.SqlProfile.ColumnOption.NotNull

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FriendshipRequestRepository @Inject() (val dbConfigProvider: DatabaseConfigProvider, val userRepository: UserRepository)(implicit
    ec: ExecutionContext
) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._
  private val friendshipRequests = TableQuery[FriendshipRequestTable]

  def createFriendshipRequest(newFriendshipRequest: FriendshipRequest): Future[FriendshipRequest] = {
    db.run(
      (friendshipRequests returning friendshipRequests.map(_.id)
        into ((friendshipRequest, id) => friendshipRequest.copy(id = id))) += newFriendshipRequest
    )
  }

  def deleteFriendshipRequest(id: Long): Future[Option[Int]] = {
    db.run(friendshipRequests.filter(_.id === id).delete.map {
      case 0       => None
      case 1       => Some(1)
      case deleted => throw new RuntimeException(s"Deleted $deleted rows")
    })
  }

  def deleteFriendshipRequestsBetweenUsers(firstUserId: Long, secondUserId: Long): Future[Option[Int]] = {
    db.run(
      friendshipRequests
        .filter(f => (f.receiverId === firstUserId && f.senderId === secondUserId) || (f.senderId === firstUserId && f.receiverId === secondUserId))
        .delete
        .map {
          case 0       => None
          case 1       => Some(1)
          case deleted => throw new RuntimeException(s"Deleted $deleted rows")
        }
    )
  }

  def findReceivedUsersFriendshipRequests(userId: Long): Future[Seq[FriendshipRequest]] = {
    db.run(
      friendshipRequests.filter(_.receiverId === userId).result
    )
  }

  def findSentUsersFriendshipRequests(userId: Long): Future[Seq[FriendshipRequest]] = {
    db.run(
      friendshipRequests.filter(_.senderId === userId).result
    )
  }

  def findFriendshipRequestBetweenUsers(firstUser: Long, secondUser: Long): Future[Option[FriendshipRequest]] = {
    db.run(
      friendshipRequests
        .filter(f => (f.receiverId === firstUser && f.senderId === secondUser) || (f.senderId === firstUser && f.receiverId === secondUser))
        .result
        .headOption
    )
  }

  def checkIfExistFriendshipRequestBetweenUsers(firstUser: Long, secondUser: Long): Future[Boolean] = {
    db.run(
      friendshipRequests
        .filter(f => (f.receiverId === firstUser && f.senderId === secondUser) || (f.senderId === firstUser && f.receiverId === secondUser))
        .exists
        .result
    )
  }

  class FriendshipRequestTable(tag: Tag) extends Table[FriendshipRequest](tag, "friendshipRequests") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def senderId = column[Long]("senderId", NotNull)
    def receiverId = column[Long]("receiverId", NotNull)
    def createdDate = column[LocalDateTime]("createdDate", NotNull)
    def sender = foreignKey("sender_fk", senderId, TableQuery[userRepository.UserTable])(_.id)
    def receiver = foreignKey("receiver_fk", receiverId, TableQuery[userRepository.UserTable])(_.id)

    override def * : ProvenShape[FriendshipRequest] =
      (id, senderId, receiverId, createdDate) <> ((FriendshipRequest.apply _).tupled, FriendshipRequest.unapply)
  }
}
