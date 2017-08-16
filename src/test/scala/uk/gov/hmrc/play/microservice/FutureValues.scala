/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.play.microservice

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

trait FutureValues {

  import scala.concurrent.duration._
  import scala.concurrent.{Await, Future}

  implicit val defaultTimeout = 5 seconds

  implicit def extractAwait[A](future: Future[A]): A = awaitResult[A](future)

  def awaitResult[A](future: Future[A])(implicit timeout: Duration) = Await.result(future, timeout)

  implicit class FuturePimp[T](future: Future[T]) {
    def awaitSuccess() = {
      Await.result(future, defaultTimeout)
      assert(future.value.get.isSuccess, s"Future was failed, value was ${future.value}")
    }

    def awaitFailure() = {
      Await.result(future, defaultTimeout)
      assert(future.value.get.isFailure, s"Future was success, value was ${future.value}")
    }

    def await:T = {
      Await.result(future, defaultTimeout)
    }

    def await(timeout: Duration):T = {
      Await.result(future, timeout)
    }

    def awaitSuccessOrThrow(): Unit = {
      future.onComplete {
        case Success(value) => Unit
        case Failure(e) => throw e
      }
      Await.result(future, defaultTimeout)
    }
  }

}
