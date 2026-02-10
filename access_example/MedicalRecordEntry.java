package access_example;
import access_example.UserId;
import access_example.UserType;
public class MedicalRecordEntry {
  public UserId patient;
  public UserId nurse;
  public UserId doctor;
  public String division;
  public String content;

  public MedicalRecordEntry(UserId patient, UserId nurse, UserId doctor, String division, String content){
    this.patient = patient;
    this.nurse = nurse;
    this.doctor = doctor;
    this.division = division;
    this.content = content;
  }
}
