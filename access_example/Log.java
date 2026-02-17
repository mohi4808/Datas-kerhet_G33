package access_example;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log{
  private String log;
  public Log(){
    this.log = "";
  }
  public void start(){
    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
    Date date = new Date();
    this.log = this.log + "\n--------------" + formatter.format(date) +  "-------------\n";
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
