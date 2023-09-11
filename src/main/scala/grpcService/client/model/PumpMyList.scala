package grpcService.client.model

object PumpMyList:

  extension[A] (l: List[A])
    def filterMap(cond: A => Boolean)(map: A => A): List[A] =
      l.map { i =>
        if(cond(i)) {
          map(i)
        } else {
          i
        }
      }

  @main def test() =
    var l = List(1,2,3,4,5,6)
    var newL = l.filterMap {
      i => i % 2 == 0
    } {
      i => i * 2
    }
    newL = newL :+ 7
    println(newL)
