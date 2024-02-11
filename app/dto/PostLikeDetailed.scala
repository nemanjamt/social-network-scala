package dto

import models.PostLike
import play.api.libs.json.{Json, Writes}

case class PostLikeDetailed(postLike: PostLike, user: UserInfo)

object PostLikeDetailed {
  implicit val postLikeDetailedWrites: Writes[PostLikeDetailed] = Json.writes[PostLikeDetailed]
}
