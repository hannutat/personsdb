import java.sql.*;
import java.lang.*;
import java.util.List;
import java.util.ArrayList;

public class DatabaseConnection {
    private String dbPath;
    private String dbChoice;
    private Connection dbConn;
    private Statement statement;
    private String user;
    private String pass;

    //constructor sets dbPath depending which db is chosen, mariadb requires user and pass
    public DatabaseConnection(String dbChoice){
        if (dbChoice.equals("mariadb")){
            this.dbPath = "jdbc:mariadb://localhost/personsdb";
            this.user = "root";
            this.pass = "new-password";
        } else if (dbChoice.equals("sqlite")) {
            this.dbPath = "jdbc:sqlite:sqlite/persons.db";
        }
        this.dbConn = null;
        this.statement = null;
        this.dbChoice = dbChoice;
    }

    //opens the connection to db, mariadb requires user and pass
    public void openConnection() {
        try {
            if (this.dbChoice.equals("mariadb")){
                this.dbConn = DriverManager.getConnection(this.dbPath, user, pass);
            } else if (this.dbChoice.equals("sqlite")) {
                this.dbConn = DriverManager.getConnection(this.dbPath);
            }
            this.statement = this.dbConn.createStatement();
        } catch (Exception exception) {
            System.out.println("Database error: " + exception.getMessage());
        }
    }

    //closes the db connection
    public void closeConnection(){
        try {
            this.statement.close();
            this.dbConn.close();
        } catch (Exception exception) {
            System.out.println("Error closing connection: " + exception.getMessage());
        }
    }

    //database read method, searches a string from field name
    public List<Person> readDatabase(String search) {
        openConnection();
        String sqlQuery = "SELECT * FROM persons WHERE name LIKE '%" + search + "%';";
        List<Person> personsList = new ArrayList<>();
        try {
            ResultSet results = statement.executeQuery(sqlQuery);
            while (results.next()) {
                Person newPerson = new Person(results.getString("name"), results.getInt("birthyear"),
                        results.getString("address"), results.getString("phone"), results.getString("info1"),
                        results.getString("info2"), results.getString("info3"), results.getInt("person_id"));
                personsList.add(newPerson);
            }
        } catch (Exception exception) {
            System.out.println("ERROR: " + exception.getMessage());
        }
        closeConnection();
        return personsList;
    }

    //database read method, searches a specific id
    public Person searchById(int id){
        openConnection();
        String sqlQuery = "SELECT * FROM persons WHERE person_id=" + id;
        Person person = null;
        try {
            ResultSet results = statement.executeQuery(sqlQuery);
            while (results.next()) {
                person = new Person(results.getString("name"), results.getInt("birthyear"),
                        results.getString("address"), results.getString("phone"), results.getString("info1"),
                        results.getString("info2"), results.getString("info3"), results.getInt("person_id"));
            }
        } catch (Exception exception) {
            System.out.println("ERROR: " + exception.getMessage());
        }
        closeConnection();
        return person;
    }

    //generates an sql string from a person object for inserting a new person and calls sqlExecute() to execute it
    public boolean insertDatabase(Person personToInsert) {
        String sqlString = "INSERT INTO persons VALUES (" + null + ", '" + personToInsert.getName() + "', '" + personToInsert.getAddress() +
                "', '" + personToInsert.getPhone() + "', '" + personToInsert.getBirthYear() + "', '" + personToInsert.getInfo1() +
                "', '" + personToInsert.getInfo2() + "', '" + personToInsert.getInfo3() + "');";

        return sqlExecute(sqlString);
    }

    //first tries if a person with the requested id exists in db
    //generates an sql string based on the given person id for deleting a person and calls sqlExecute() to execute it
    public boolean deleteDatabase(int personToDelete) {
        String sqlString = "SELECT * FROM persons WHERE person_id=" + personToDelete + ";";
        openConnection();
        try {
            ResultSet results = this.statement.executeQuery(sqlString);
            if (!results.next()){
                return false;
            }
        } catch (Exception exception) {
            System.out.println("ERROR: " + exception.getMessage());
        }
        closeConnection();

        sqlString = "DELETE FROM persons WHERE person_id=" + personToDelete + ";";

        return sqlExecute(sqlString);
    }

    //generates an sql string from a person object for modifying a person in db (by id) and calls sqlExecute() to execute it
    public boolean updateDatabase(Person updatedPerson){
        String sqlString = "UPDATE persons SET name='" + updatedPerson.getName() + "', address='" +
                updatedPerson.getAddress() + "', phone='" + updatedPerson.getPhone() + "', birthyear=" +
                updatedPerson.getBirthYear() + ", info1='" + updatedPerson.getInfo1() + "', info2='" +
                updatedPerson.getInfo2() + "', info3='" + updatedPerson.getInfo3() + "' WHERE person_id=" +
                updatedPerson.getPerson_id() + ";";

        return sqlExecute(sqlString);
    }

    //executes an sql string given as parameter
    private boolean sqlExecute(String sqlString){
        openConnection();
        boolean success;
        try {
            this.statement.executeUpdate(sqlString);
            success = true;
        } catch (Exception exception) {
            System.out.println("ERROR: " + exception.getMessage());
            success = false;
        }
        closeConnection();
        return success;
    }
}