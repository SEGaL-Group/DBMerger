package dbm;

import java.io.File;
import java.util.List;

import models.Pair;

import db.DatabaseConnector;

import process.Spawner;

public class Merger
{
	private DatabaseConnector fdb;
	private DatabaseConnector tdb;
	private Spawner spawner;
	
	Merger(DatabaseConnector fdb, DatabaseConnector tdb) {
		this.spawner = new Spawner("/home/jell");
		
		this.fdb = fdb;
		this.tdb = tdb;
	}
	
	public void mergeDumps() {
		// Get all the dumps
		File folder = new File(Resources.partialDumpsFolder);
		File[] listOfFiles = folder.listFiles();
		
		// Iterate over
		for(int i = 0; i < listOfFiles.length; i++) {
			if(listOfFiles[i].isFile()) {
				mergeDump(listOfFiles[i].getAbsolutePath());
			}
		}
	}
	
	private void mergeDump(String file) {
		// Create the temp db
		tdb.connect("eggnet");
		tdb.createDatabaseEmpty(Resources.tempDB);
		
		// Restore dump
		restoreTNG(file);
		
		// Add fix inducing
		restoreFI();
		
		// Run STCA on the temp DB
		runSTCA();
	}
	
	private void restoreTNG(String file) {
		spawner.spawnProcess(new String[] {"psql", "-d", Resources.tempDB, "-f", file});
	}
	
	private void restoreFI() {
		spawner.spawnProcess(new String[] {"psql", "-f", Resources.bugDump, Resources.tempDB});
	}
	
	private void runSTCA() {
		List<Pair<String, Integer>> networks = tdb.getAllNetworkCommits();
		
		for(Pair<String, Integer> network: networks) {
			// Get the edges
			List<Pair<String, String>> edges = tdb.getEdgesFromNetwork(network.getSecond());
			
			// Get commit status
			boolean fail = tdb.getCommitStatus(network.getFirst());
			
			// Upsert into final DB
			for(Pair<String, String> edge: edges) {
				fdb.upsertPattern(edge, fail);
			}
		}
	}
}
