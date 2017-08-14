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

trait ConfigSetup {


  val enrol1EnrolmentConfig =
    """[{
      |  enrolment = "ENROL-1"
      |  identifiers = [{ key = "BOO", value = "$taxId" }]
      |}]""".stripMargin

  val enrol2EnrolmentConfig =
    """[{
      |  enrolment = "ENROL-2"
      |  identifiers = [{ key = "AHH", value = "$taxId" }]
      |}]""".stripMargin

  val fullConfig =
    s"""
       |controllers {
       |
     |  authorisation = {
       |
     |    enrol1 = {
       |      patterns = [
       |        "/foo/enrol1/:taxId"
       |        "/foo/enrol1/:taxId/:rest"
       |      ],
       |      predicates = $enrol1EnrolmentConfig
       |    }
       |
     |    enrol2 = {
       |      patterns = [
       |        "/foo/enrol2/:taxId"
       |        "/foo/enrol2/:taxId/:rest"
       |      ],
       |      predicates = $enrol2EnrolmentConfig
       |    }
       |
     |  }
       |
     |  foo.FooController = {
       |    authorisedBy = ["enrol1", "enrol2"]
       |    needsLogging = false
       |    needsAuditing = false
       |  }
       |
     |  bar.BarController = {
       |    authorisedBy = ["enrol1"]
       |    needsLogging = false
       |    needsAuditing = false
       |  }
       |
     |  baz.BazController = {
       |    needsLogging = false
       |    needsAuditing = false
       |  }
       |
     |  bim.BimController = {
       |    authorisedBy = ["unknown"]
       |    needsLogging = false
       |    needsAuditing = false
       |  }
       |
     |}""".stripMargin


}
