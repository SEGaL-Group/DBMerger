package dbm;

import db.DatabaseConnector;


public class Main
{
	public static void main(String[] args) {
		System.out.println("DBM developed by eggnet.");
		System.out.println();
		try {
			if (args.length < 3 ) {
				System.out.println("Retry: DBM [mergeDB] [partialDumpsFolder] [bugDump]");
				throw new ArrayIndexOutOfBoundsException();
			}
			else {
				try  {
					Resources.mergeDB = args[0];
					Resources.partialDumpsFolder = args[1];
					Resources.bugDump = args[2];
					
					// Create the final DB
					DatabaseConnector fdb = new DatabaseConnector();
					fdb.connect("eggnet");
					fdb.createDatabaseEmpty(Resources.mergeDB);
					
					// Create the temp DB
					DatabaseConnector tdb = new DatabaseConnector();
					
					
					
					fdb.close();
					tdb.close();
				} 
				catch (Exception e) {
					e.printStackTrace();
				} 
			}
		}
		catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
	}
}
