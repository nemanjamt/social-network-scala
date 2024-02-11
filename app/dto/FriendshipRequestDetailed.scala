package dto

import models.FriendshipRequest
import play.api.libs.json.{Json, Writes}

case class FriendshipRequestDetailed(request: FriendshipRequest, sender: UserInfo)

object FriendshipRequestDetailed {
  implicit val friendshipRequestDetailedWrites: Writes[FriendshipRequestDetailed] = Json.writes[FriendshipRequestDetailed]
}
