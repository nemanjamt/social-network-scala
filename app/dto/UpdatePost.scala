package dto

import play.api.libs.json.{Json, Reads}

case class UpdatePost(countLike: Int, content: String)

object UpdatePost {
  implicit val updateUserReads: Reads[UpdatePost] = Json.reads[UpdatePost]
}
