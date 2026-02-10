package access_example;
public class Log{
  private String log;
  public Log(){
    this.log = "";
  } 
  public void append(String entry){
    this.log = this.log + entry;
  }
  public String content(){
    return this.log;
  } 
  public void print(){
    System.out.print(this.log);
  }
} 
