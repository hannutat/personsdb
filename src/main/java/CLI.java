import java.util.List;
import java.util.Scanner;

public class CLI {

    public void startCLI(String dbChoice) {
        //initialize database connection depending on db choice
        DatabaseConnection dbConn = new DatabaseConnection(dbChoice);
        Scanner scanner = new Scanner(System.in);

        int menuChoice, birthyear, personToDelete;
        String name, birthyearString, personToDeleteString;
        boolean successInsert, successDelete;

        //CLI main menu
        while (true) {
            System.out.println("\n-- PersonsDB Main menu --");
            System.out.println("[using " + dbChoice + " connection]");
            System.out.println("1: List all persons\n2: Search by name");
            System.out.println("3: Add a new person\n4: Delete a person\n5: Quit");
            System.out.println("Enter action number and press enter: ");
            String menuChoiceString = scanner.nextLine();
            try {
                menuChoice = Integer.parseInt(menuChoiceString);
            } catch (NumberFormatException error) {
                System.out.println("Please enter a number");
                continue;
            }
            List<Person> personsList;
            switch (menuChoice) {
                case 1:
                    //view all persons, search with ""
                    personsList = dbConn.readDatabase("");
                    personsList.forEach(System.out::println);
                    continue;
                case 2:
                    //search by a string, case insensitive
                    System.out.println("Please enter name or a part of name to search:");
                    String search = scanner.nextLine();
                    personsList = dbConn.readDatabase(search);
                    personsList.forEach(System.out::println);
                    continue;
                case 3:
                    //enter a new person, name and year of birth mandatory
                    while (true) {
                        System.out.println("Enter name:    (mandatory)");
                        name = scanner.nextLine();
                        if (name.equals("")) {
                            continue;
                        }
                        break;
                    }
                    while (true) {
                        System.out.println("Enter year of birth:    (mandatory)");
                        birthyearString = scanner.nextLine();
                        if (birthyearString.equals("")) {
                            continue;
                        }
                        try {
                            birthyear = Integer.parseInt(birthyearString);
                        } catch (NumberFormatException error) {
                            System.out.println("Please enter a number");
                            continue;
                        }
                        break;
                    }
                    System.out.println("Enter address:");
                    String address = scanner.nextLine();
                    System.out.println("Enter phone:");
                    String phone = scanner.nextLine();
                    System.out.println("Enter info1:");
                    String info1 = scanner.nextLine();
                    System.out.println("Enter info2:");
                    String info2 = scanner.nextLine();
                    System.out.println("Enter info3:");
                    String info3 = scanner.nextLine();

                    //create a new person based on user input
                    Person personToAdd = new Person(name, birthyear, address, phone, info1, info2, info3);

                    //try to insert the new person into db
                    successInsert = dbConn.insertDatabase(personToAdd);
                    if (successInsert) {
                        System.out.println("Person added");
                    } else {
                        System.out.println("Something went wrong...");
                    }
                    continue;
                case 4:
                    //delete person by id number
                    while (true) {
                        System.out.println("Enter ID number of person to delete:");
                        personToDeleteString = scanner.nextLine();
                        try {
                            personToDelete = Integer.parseInt(personToDeleteString);
                        } catch (NumberFormatException error) {
                            System.out.println("Please enter a valid ID number");
                            continue;
                        }

                        //try to delete the person from db
                        successDelete = dbConn.deleteDatabase(personToDelete);
                        if (successDelete) {
                            System.out.println("Person deleted");
                        } else {
                            System.out.println("Something went wrong...");
                        }
                        break;
                    }
                    continue;
                case 5:
                    System.exit(0);
            }
        }
    }
}
