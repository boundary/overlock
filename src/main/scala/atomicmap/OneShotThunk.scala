package atomicmap

import java.util.concurrent._
import locks._

class OneShotThunk[A](op : => A) {
  //wonder what the scala compiler will cook up here
  lazy val value = op
  
  
  override def equals(other : Any) = other match {
    case o : OneShotThunk[_] => value == o.value
    case _ => false
  }
}