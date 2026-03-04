import java.io.*;
import java.net.*;

import javax.net.*;
import javax.net.ssl.*;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import access_example.UserId;
import access_example.UserInfo;
import access_example.UserType;
import access_example.AuthenticatedId;
import access_example.BackendEntry;
import access_example.MedicalRecordEntry;
import access_example.RecordId;

public class server implements Runnable {
  private ServerSocket serverSocket = null;
  private static int numConnectedClients = 0;
  private static final ConcurrentHashMap<String, String[]> userCache = new ConcurrentHashMap<>();
  private final BackendEntry backend = new BackendEntry();
  private static final String pwFile =  "pwfile.txt";
  
  public server(ServerSocket ss) throws IOException {
    serverSocket = ss;
    newListener();
  }

  private static void addUser(String filePath, String userId, String password) throws Exception {
    if (userCache.containsKey(userId)) {
      System.out.println("Cannot add user which already exists.");
      return;
    }
    // current users and passwords
    // 1234    password
    // 4567    not_password


    SecureRandom random = new SecureRandom();
    byte[] saltBytes = new byte[16];
    random.nextBytes(saltBytes);
    String salt = Base64.getEncoder().encodeToString(saltBytes);

    MessageDigest md = MessageDigest.getInstance("SHA-512");
    md.update(salt.getBytes(StandardCharsets.UTF_8));
    byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
    
    StringBuilder sb = new StringBuilder();
    for (byte b : hashedBytes) {
      sb.append(String.format("%02x", b));
    }
    String storedHash = sb.toString();
    String[] value = {salt, storedHash, "0"};

    userCache.put(userId, value);
  }

  private static boolean verifyPassword(String input, String salt, String storedHash) {    
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-512");
      md.update(salt.getBytes(StandardCharsets.UTF_8));
      byte[] hashedBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

      StringBuilder sb = new StringBuilder();
      for (byte b : hashedBytes) {
          sb.append(String.format("%02x", b));
      }
      return sb.toString().equals(storedHash);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error in verifyPassword.", e);
    }
  }

  public static void parsePwFile(String filePath) {
    try (Scanner scanner = new Scanner(new File(filePath))) {
      while (scanner.hasNextLine()) {
        String[] parts = scanner.nextLine().split(":");
        if (parts.length >= 2) {
          userCache.put(parts[0], Arrays.copyOfRange(parts, 1, parts.length));
        }
      }
    } catch (Exception e) {
      System.err.println("Error parsing password file: " + e.getMessage());
    }
  }

  public void run() {
    try {
      SSLSocket socket=(SSLSocket)serverSocket.accept();
      newListener();
      SSLSession session = socket.getSession();
      Certificate[] cert = session.getPeerCertificates();
      String subject = ((X509Certificate) cert[0]).getSubjectX500Principal().getName();
      String issuer = ((X509Certificate) cert[0]).getIssuerX500Principal().getName();
      java.math.BigInteger serialNumber = ((X509Certificate) cert[0]).getSerialNumber();

      numConnectedClients++;
      System.out.println("client connected");
      System.out.println("client name (cert subject DN field): " + subject);
      System.out.println("client issuer (cert issuer DN field): " + issuer);
      System.out.println("client cert serial number: " + serialNumber.toString(16));
      System.out.println(numConnectedClients + " concurrent connection(s)\n");

      PrintWriter out = null;
      BufferedReader in = null;
      out = new PrintWriter(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      boolean loggedIn = false;
      UserInfo info = null;
      UserId id = null;
      AuthenticatedId  authenticatedId = null;

      while (true) {
        try {
          if (!loggedIn) {
            boolean validUserPw = false;
            boolean tooManyLogins = false;
            
            out.println("Enter personal ID:");
            out.println("__PROMPT__");
            String userId = in.readLine();  
            boolean validUserId = userCache.containsKey(userId);
            // MAYBE: certificate hash check against the certificate hashed ID here.
            
            out.println("Enter password: ");
            out.println("__PROMPT__");
            String password = in.readLine();
            
            if (validUserId) {
              String[] userInfo = userCache.get(userId);
              int attempts = Integer.parseInt(userInfo[2]);
              
              if (attempts <= 5) {
                validUserPw = verifyPassword(password, userInfo[0], userInfo[1]);
                if (validUserPw) {
                  attempts = 0;
                  userInfo[2] = "0";
                } else {
                  attempts += 1; 
                  userInfo[2] = String.valueOf(attempts);
                } 
                userCache.put(userId, userInfo); 
              } else {
                tooManyLogins = true;
              }
            }
            
            if (tooManyLogins) {
              out.println("Too many failed attempts, account locked. Please contact 070 123 45 67.");
              continue;
            }
            
            if (!validUserId || !validUserPw) {
              out.println("Invalid username or password.");
              continue;
            }
            
            id = new UserId(Integer.parseInt(userId));
            authenticatedId = new AuthenticatedId(id);
            info = backend.whoAmI(authenticatedId);
            if (info.type == UserType.DOCTOR) {
              System.out.println("AAAAHHHHH");
              System.out.println(info);
            }
            
            loggedIn = true;

            out.println("You logged in!");
            break;
          }
        } catch (NumberFormatException e) {
            out.println("Error: illformatted. Try again.");
        } catch (Exception e) {
            out.println("System error occurred.");
            e.printStackTrace();
        }
      }


      while (loggedIn) {
        try {
          out.println("Select an action: \n[1] Read Patient Records \n[2] Edit Record \n[3] Delete Record \n[4] Create Record \n[5] Quit"); 
          out.println("__PROMPT__");
          String action = in.readLine();

          if (action == null || action.equals("5")) {
            out.println("Logging out...");
            break;
          }
          if (action.equals("1")) {
            out.println("Enter ID of patient whose records you wish to read:");
            out.println("__PROMPT__");
            String patientStr = in.readLine();
            UserId patient = new UserId(Integer.parseInt(patientStr));
            
            List<MedicalRecordEntry> records = backend.requestPatientRecords(patient, authenticatedId);

            if (records.isEmpty()) {
              out.println("Records: " + records.toString());
              out.println("No records for this patient.");
              continue;
            }

            out.println("Printing patients records:");
            for (MedicalRecordEntry record : records) {
              out.println("Record ID: " + record.recordId + "\n" +
                              " | Patient: " + record.patient + "\n" +
                              " | Doctor: " + record.doctor + "\n" +
                              " | Nurse: " + record.nurse + "\n" +
                              " | Division: " + record.division + "\n" +
                              " | content: " + record.content
                            );
            }
            out.println("\n");
          } 
          else if (action.equals("2")) {
            out.println("Enter the ID of record you wish to edit:");
            out.println("__PROMPT__");
            RecordId record = new RecordId(Integer.parseInt(in.readLine()));

            out.println("Enter the new content of the record:");
            out.println("__PROMPT__");
            String content = in.readLine();

            backend.replaceRecordContent(record, content, authenticatedId);;
          } 
          else if (action.equals("3")) {
            if (info.type != UserType.AUTHORITY) {
              out.println("Only authorities may delete a user.");
              continue;
            }

            out.println("Enter the ID of record you wish to delete:");
            out.println("__PROMPT__");
            String recordStr = in.readLine();
            RecordId record = new RecordId(Integer.parseInt(recordStr));

            // proper check for records.contains(record) maybe?
            backend.deleteRecord(record, authenticatedId);
          } 
          else if (action.equals("4")) {
            if (info.type == UserType.NURSE || info.type == UserType.PATIENT) {
              out.println("If you are not a doctor or authority, you cannot add a user.");
              continue;
            }
            out.println("Enter ID of the patient for which you wish to add a record:");
            out.println("__PROMPT__");
            UserId patient = new UserId(Integer.parseInt(in.readLine()));

            out.println("Enter ID of nurse presiding over the record:");
            out.println("__PROMPT__");
            UserId nurse = new UserId(Integer.parseInt(in.readLine()));
            
            backend.createNewRecord(patient, nurse, authenticatedId);
          } 
          else {
            out.println("Invalid option, try again.");
            continue;
          }
        } catch (NumberFormatException e) {
            out.println("Error: ID must be a number. Try again.");
        } catch (Exception e) {
            out.println("System error occurred.");
            e.printStackTrace();
        }
      } 
      
      in.close();
      out.close();
      socket.close();
      numConnectedClients--;
      System.out.println("client disconnected");
      System.out.println(numConnectedClients + " concurrent connection(s)\n");
    } catch (IOException e) {
      System.out.println("Client died: " + e.getMessage());
      e.printStackTrace();
      return;
    }
  }
  
  private void newListener() { (new Thread(this)).start(); } // calls run()
  public static void main(String args[]) {
    parsePwFile(pwFile);

    try {
      addUser(pwFile, "1234", "password");
      addUser(pwFile, "1", "password");
      addUser(pwFile, "2", "password");
      addUser(pwFile, "3", "password");
      addUser(pwFile, "4", "password");
      addUser(pwFile, "5", "password");
      addUser(pwFile, "4567", "not_password");
    } catch (Exception e) {
      // TODO: handle exception
    }

    // writes to pwfile very few minutes
    // this needs to be concurrent, i.e synchronized or something.
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate(() -> {
      try {
        // System.out.println("Saving to txtfile.");
        File tempFile = new File(pwFile + ".tmp");
        try (PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
          for (Map.Entry<String, String[]> entry : userCache.entrySet()) {
            String userId = entry.getKey();
            String[] data = entry.getValue();
            String line = userId + ":" + String.join(":", data);
            writer.println(line);
          }
        }
        tempFile.renameTo(new File(pwFile)); 
      } catch (IOException e) {
          System.err.println("Failed to save cache: " + e.getMessage());
      }
    }, 5, 5, TimeUnit.SECONDS);

    System.setProperty("javax.net.ssl.keyStore", "serverkeystore");
    System.setProperty("javax.net.ssl.keyStorePassword", "password");
    System.setProperty("javax.net.ssl.trustStore", "servertruststore");
    System.setProperty("javax.net.ssl.trustStorePassword", "password");
    System.out.println("\nServer Started\n");
    int port = -1;
    if (args.length >= 1) {
      port = Integer.parseInt(args[0]);
    }
    String type = "TLSv1.2";
    try {
      ServerSocketFactory ssf = getServerSocketFactory(type);
      ServerSocket ss = ssf.createServerSocket(port, 0, InetAddress.getByName(null));
      ((SSLServerSocket)ss).setNeedClientAuth(true); // enables client authentication
      new server(ss);
    } catch (IOException e) {
      System.out.println("Unable to start Server: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static ServerSocketFactory getServerSocketFactory(String type) {
    if (type.equals("TLSv1.2")) {
      SSLServerSocketFactory ssf = null;
      try { // set up key manager to perform server authentication
        SSLContext ctx = SSLContext.getInstance("TLSv1.2");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        KeyStore ks = KeyStore.getInstance("JKS");
        KeyStore ts = KeyStore.getInstance("JKS");
        char[] password = "password".toCharArray();
        // keystore password (storepass)
        ks.load(new FileInputStream("serverkeystore"), password);  
        // truststore password (storepass)
        ts.load(new FileInputStream("servertruststore"), password); 
        kmf.init(ks, password); // certificate password (keypass)
        tmf.init(ts);  // possible to use keystore as truststore here
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        ssf = ctx.getServerSocketFactory();
        return ssf;
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      return ServerSocketFactory.getDefault();
    }
    return null;
  }
}
