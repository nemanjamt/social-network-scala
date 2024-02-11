package dto

import models.Post
import play.api.libs.json.{Json, Writes}

case class PostDetailed(post: Post, userInfo: UserInfo, likedByLoggedInUser: Boolean)

object PostDetailed {
  implicit val postDetailedWrites: Writes[PostDetailed] = Json.writes[PostDetailed]
}
