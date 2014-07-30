package controllers

import play.api._
import _root_.controllers.modules.bge.controllers.MySecurity
import play.api.mvc._

object Application extends Controller {

  def index(clientID:Int) = MySecurity.ClientAction(clientID) { implicit request =>
    Ok(s"client was set to:  ${request.client} !!")
  }

  def test(clientID:Int, reservationID:Int) = MySecurity.ClientAndReservationAction(clientID, reservationID) { implicit request =>
    Ok(s"client was set to:  ${request.client}   and ${request.reservation} !!")
  }

}