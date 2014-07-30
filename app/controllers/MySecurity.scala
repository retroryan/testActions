package controllers

package modules.bge.controllers

import play.api.mvc._
import scala.concurrent.Future
import play.api.mvc.Result
import scala.Some
import scala.util.{Failure, Success, Try}

import scala.concurrent.ExecutionContext.Implicits.global

case class Client(username: String, clientID: Int)

case class Reservation(id: Int, guests: Set[Client])

class ClientRequest[A](val client: Client, request: Request[A]) extends WrappedRequest[A](request)

class ClientAndReservationRequest[A](val client: Client, val reservation: Reservation, request: Request[A]) extends WrappedRequest[A](request)


object ClientDao {
  def findById(clientID: Int): Future[Try[Client]] = {
    if (clientID == 1) {
      Future.successful(Success(new Client("Sherlock", clientID)))
    }
    else {
      Future.successful(Failure(new Exception("Client Not Found")))
    }
  }
}

object ReservationDao {
  def findById(reservationID: Int, client: Client): Try[Reservation] = {
    if (reservationID == 1) {
      Success(new Reservation(reservationID, Set(client)))
    }
    else {
      Failure(new Exception("Reservation Not Found"))
    }
  }

  def findByIdFuture(reservationID: Int, client: Client): Future[Try[Reservation]] = {
    if (reservationID == 1) {
      Future.successful(Success(new Reservation(reservationID, Set(client))))
    }
    else {
      Future.successful(Failure(new Exception("Reservation Not Found")))
    }
  }
}

object MySecurity {

  def ClientAction(clientID: Int) = new ActionRefiner[Request, ClientRequest] with ActionBuilder[ClientRequest] {
    def refine[A](input: Request[A]) = {
      ClientDao.findById(clientID).map {
        case Success(client) => Right(new ClientRequest(client, input))
        case Failure(excp) => Left(Results.Forbidden("Not Authorized"))
      }
    }
  }

  def ClientAndReservationAction(clientID: Int, reservationID: Int) = new ActionRefiner[Request, ClientAndReservationRequest] with ActionBuilder[ClientAndReservationRequest] {
    def refine[A](request: Request[A]) = {
      ClientDao.findById(clientID).map {
        case Success(client) => {

          //This should work to lookup and return a future reservation?
          ReservationDao.findByIdFuture(reservationID, client) map {
            case Success(reservation) => Right(new ClientAndReservationRequest(client, reservation, request))
            case Failure(excp) => Left(Results.Forbidden("Not Authorized"))
          }

          //Delete this - just here to get things to compile
          ReservationDao.findById(reservationID, client) match {
            case Success(reservation) => Right(new ClientAndReservationRequest(client, reservation, request))
            case Failure(excp) => Left(Results.Forbidden("Not Authorized"))
          }

        }
        case Failure(excp) => Left(Results.Forbidden("Not Authorized"))
      }
    }
  }

  //Why doesn't this work to create an ActionRefiner that takes a ClientRequest and returns a ClientAndReservationRequest
  /**
  def ReservationAction(clientID: Int, reservationID: Int) = new ActionRefiner[ClientRequest, ClientAndReservationRequest] with ActionBuilder[ClientAndReservationRequest] {
    def refine[A](request: ClientRequest[A]) = {
      //This should work to lookup and return a future reservation?
      ReservationDao.findByIdFuture(reservationID, request.client) map {
        case Success(reservation) => Right(new ClientAndReservationRequest(request.client, reservation, request))
        case Failure(excp) => Left(Results.Forbidden("Not Authorized"))
      }
    }
  }

  **/


  def FilterClientId(clientID: Int) = new ActionFilter[Request] with ActionBuilder[Request] {
    override protected def filter[A](request: Request[A]): Future[Option[Result]] = Future.successful {
      println(s"${request.body}")
      if (clientID != 1)
        Some(Results.Forbidden("Not Authorized"))
      else
        None
    }
  }

  def FilterReservationId(reservationID: Int) = new ActionFilter[Request] with ActionBuilder[Request] {
    override protected def filter[A](request: Request[A]): Future[Option[Result]] = Future.successful {
      if (reservationID != 1)
        Some(Results.Forbidden("Not Authorized"))
      else
        None
    }
  }

}