package practice

object practice extends App {

  def whileLoop(condition: => Boolean)(body: => Unit): Unit = {
    if(condition){
      body
      whileLoop(condition)(body)
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


  whileLoop(cond)(println("hio"))

}
