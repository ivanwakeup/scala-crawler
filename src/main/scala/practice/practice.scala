package practice

object practice extends App {

  def whileLoopByName(condition: => Boolean)(body: => Long): Unit = {
    if(condition){
      println(body)
      whileLoopByName(condition)(body)
    }
  }

  def whileLoopByValue(condition: => Boolean)(body: Long): Unit = {
    if(condition){
      println(body)
      whileLoopByValue(condition)(body)
    }
  }

  //we want this condition to be something that gens a random number, and returns false
  def cond: Boolean = {
    val result = new java.util.Random()
    val nxt = result.nextInt(11)
    nxt match {
      case 1 => false
      case _ => true
    }
  }

  def getTime() = {
    println("getting time")
    System.nanoTime()
  }

  val t = getTime()
  whileLoopByName(cond)(getTime())
  whileLoopByValue(cond)(t)

  //call by value functions are useful to ensure the "body" of your code gets executed EVERY time you call. so,
  //in our while loop we get this behavior

}
