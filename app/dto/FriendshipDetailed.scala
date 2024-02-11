package dto

import models.Friendship
import play.api.libs.json.{Json, Writes}

case class FriendshipDetailed(friendship: Friendship, friend: UserInfo)

object FriendshipDetailed {
  implicit val friendshipDetailedWrites: Writes[FriendshipDetailed] = Json.writes[FriendshipDetailed]
}
