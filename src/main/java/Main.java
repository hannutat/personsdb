public class Main
{
    public static void main(String[] args) {
        /*
        Startup parameters:
        1st defines which database sqlite/mariadb. sqlite/mariadb
        2nd defines if the gui or cli interface is used. gui/cli
        No parameters (default) = sqlite gui
         */
        System.out.print("Starting with parameters: ");
        for (String argument : args) {
            System.out.print(argument + " ");
        }
        System.out.println();

        String dbChoice = "sqlite";

        if (args.length == 2) {
            if (args[0].equals("mariadb")) {
                dbChoice = "mariadb";
            } else if (args[0].equals("sqlite")) {
                dbChoice = "sqlite";
            } else {
                exitStatus1();
            }
            if (args[1].equals("cli")) {
                CLI cli = new CLI();
                cli.startCLI(dbChoice);
            } else if (args[1].equals("gui")){
                GUI gui = new GUI();
                gui.initStage(dbChoice);
            } else {
                exitStatus1();
            }
        } else if (args.length == 0) {
            //no parameters = use sqlite and gui
            GUI gui = new GUI();
            gui.initStage(dbChoice);
        } else {
            exitStatus1();
        }

    }

    public static void exitStatus1(){
        //When parameters are invalid, exit with status 1
        System.out.println("Invalid parameters. Exiting...");
        System.exit(1);
    }
}