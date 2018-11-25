package partyLogic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;

public class Main 
{
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException 
	{
		// Initialize file read variables
        String line = "";	                                                                        // Holds the actual line read before being split by delimiters																	
        String[] inputs = new String[4];		                                                    // Holds line info during each line read - never more than 4 line elements to read
       
        // Loops through the folder /src/input to find any files we wish to process
        // So add new files to process there please! :)
        ArrayList<String> inputFiles = new ArrayList<String>();     
        ArrayList<String> inputFileNames = new ArrayList<String>();
        ArrayList<String> outputFiles = new ArrayList<String>();
        ArrayList<String> outputText = new ArrayList<String>();
        File folder = new File("src/input/");
        File[] listofFiles = folder.listFiles();
        for(File file : listofFiles) 
        { 
        	// inputFiles - strings like this: "src/input/example_in.txt"  || inputFileNames - strings like this: "example_in.txt"
            if(file.isFile()) { inputFiles.add(file.getPath()); inputFileNames.add(file.getName()); }
        }
        
        // Create array of output file paths to write to later
        for (String s : inputFileNames)
        {
        	String token[] = s.split("_");
        	for (String a : token) { if (!a.equals("in.txt")) { outputFiles.add("src/output/" + a + "_out.txt"); }}
        }
        
        // Loop through all input files, process each one fully and add all text to outputText array
        for (String s : inputFiles)
        {
        	
        	// Keep track of how many test cases in each file and which one is currently being processed
        	int testCases = 0;
        	int currentCase = 1;
        	try 																						
            {
                // Start reading input file here
                InputStream input = new FileInputStream(s);									
                BufferedReader inputStream = new BufferedReader(new InputStreamReader(input));	
                
                // Go through every line in the input file
                while (inputStream.ready()) 														
                {			
                	// First line finds # of test cases
                    line = inputStream.readLine();														
                    inputs = line.split(" ");														
                    testCases = Integer.parseInt(inputs[0]);
                    Set<Integer> globalSet = new HashSet<Integer>();
                    
                    // Then we continue reading until we're out of test cases to check
                    // If there were more lines than test cases, they would be looped over in the outer loop but this loop would not process any more
                    while (testCases > 0)
                    {
                    	ArrayList<Integer> hostIDs = new ArrayList<Integer>();
                    	ArrayList<Integer> heurAHostIDs = new ArrayList<Integer>();
                    	ArrayList<Integer> heurBHostIDs = new ArrayList<Integer>();
                    	ArrayList<Integer> heurCHostIDs = new ArrayList<Integer>();
                    	ArrayList<Integer> heurRHostIDs = new ArrayList<Integer>();
                    	SimpleGraph<Integer, DefaultWeightedEdge> graph = new SimpleGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
                    	Map<Integer, Double> map = new HashMap<Integer, Double>(); 
                    	int numPeople, friendLinks, numParties, numHosts;
                    	
                    	// Start reading each test case within each file here
                    	line = inputStream.readLine();														
                        inputs = line.split("\\s+");														
                        numPeople = Integer.parseInt(inputs[0]);
                        friendLinks = Integer.parseInt(inputs[1]);
                        numParties = Integer.parseInt(inputs[2]);
                        numHosts = Integer.parseInt(inputs[3]);
                        
                        for (int i = 1; i <= numPeople; i++)
                        {
                        	 graph.addVertex(i);
                        }
                        
                        // Loop through F more lines
                        for (int i = 0; i < friendLinks; i++)
                        {
                        	line = inputStream.readLine();                                                  
                            inputs = line.split("\\s+");
                            int vertA = Integer.parseInt(inputs[0]);
                            int vertB = Integer.parseInt(inputs[1]);
                            graph.addEdge(vertA, vertB);
                        }
                        
                        // Loop over A more lines, assuming A is > 0
                        for (int i = 0; i < numHosts; i++)
                        {
                        	line = inputStream.readLine();
                        	inputs = line.split("\\s+");
                        	
                        	// Add all host IDs to array of integers
                        	hostIDs.add(Integer.parseInt(inputs[0]));
                        }
                        
                        globalSet = graph.vertexSet();

                        // Part 1 & 2
                        if (numHosts > 0)
                        {
                        	outputText.add("Test case " + currentCase + ".");                        	
                        	double avgSocial = calcSocial(graph, map, hostIDs);
                        	outputText.add("Average social awkwardness = " + avgSocial);
                        }
                        
                        // Part 5
                        else if (numHosts == 0 && numParties == 0)
                        {
                        	if (currentCase > 1) { outputText.add("\nSpecial Test case " + currentCase + "."); }
                        	else { outputText.add("Test case " + currentCase + "."); }
                        	double heurASocial = 0.00;
                        	double heurBSocial = 0.00;
                        	double heurCSocial = 0.00;
                        	Set<Integer> vSet = graph.vertexSet();
                        	Set<Integer> xSet = new HashSet<Integer>();
                        	for (Integer x : vSet) { map.put(x, -1.0); }
                        	
                        	for (Integer x : vSet)
                        	{
                        		boolean checker = true;
                        		for (Integer i : hostIDs)
                        		{
                        			if (x == i) { checker = false; }
                        		}
                        		if (checker == true) { xSet.add(x); }
                        	}
                        	
                        	// Heuristic 1
                        	Set<Integer> ySet = new HashSet<Integer>(); ySet.addAll(xSet);
                        	int hosts = 0;
                        	String heurAHosts = "";
                        	while (heurASocial != 1.0 && hosts <= numPeople)
                        	{
                        		Integer newHost = findPopular(graph, ySet);
                        		ySet.remove(newHost);
                        		heurAHostIDs.add(newHost);
                        		hosts++;
	                        	for (int i = 0; i < heurAHostIDs.size(); i++)
	                        	{
	                        		if (!(i + 1 < heurAHostIDs.size())) { heurAHosts += heurAHostIDs.get(i); }
	                        		else { heurAHosts += heurAHostIDs.get(i) + ","; }
	                        	}
	                        	heurASocial = calcSocial(graph, map, heurAHostIDs);
                        	}
                        	// END Heuristic 1
                        	
                        	// Heuristic 2
                        	for (Integer x : vSet) { map.put(x, -1.0); }
                        	Set<Integer> zSet = new HashSet<Integer>(); zSet.addAll(xSet);
                        	int hostsB = 0;
                        	Integer newHost = findPopular(graph, zSet);
                        	zSet.remove(newHost);
                        	heurBHostIDs.add(newHost);
                        	hostsB++;
                        	updateSocials(heurBHostIDs, zSet, map, graph);
                        	String heurBHosts = "";
                        	while (heurBSocial != 1.0 && hostsB <= numPeople)
                        	{
                        		Integer newHostSub = findAwkward(map, zSet);
                        		zSet.remove(newHostSub);
                        		heurBHostIDs.add(newHostSub);
                        		hostsB++;
                        		updateSocials(heurBHostIDs, zSet, map, graph);
	                        	for (int i = 0; i < heurBHostIDs.size(); i++)
	                        	{
	                        		if (!(i + 1 < heurBHostIDs.size())) { heurBHosts += heurBHostIDs.get(i); }
	                        		else { heurBHosts += heurBHostIDs.get(i) + ","; }
	                        	}
	                        	heurBSocial = calcSocial(graph, map, heurBHostIDs);
                        	}
                        	// END Heuristic 2
                        	
                        	// Custom Heuristic
                        	for (Integer x : vSet) { map.put(x, -1.0); }
                        	Set<Integer> uSet = new HashSet<Integer>(); uSet.addAll(xSet);
                        	int hostsC = 0;
                        	Integer newHostB = findPopular(graph, uSet);
                        	uSet.remove(newHostB);
                        	heurCHostIDs.add(newHostB);
                        	hostsC++;
                        	updateSocials(heurCHostIDs, uSet, map, graph);
                        	String heurCHosts = "";
                        	while (heurCSocial != 1.0 && hostsC < numPeople)
                        	{
                        		Integer newHostSub = findAwkwardNeighbor(map, uSet, graph);
                        		uSet.remove(newHostSub);
                        		heurCHostIDs.add(newHostSub);
                        		hostsC++;
                        		updateSocials(heurCHostIDs, uSet, map, graph);
	                        	for (int i = 0; i < heurCHostIDs.size(); i++)
	                        	{
	                        		if (!(i + 1 < heurCHostIDs.size())) { heurCHosts += heurCHostIDs.get(i); }
	                        		else { heurCHosts += heurCHostIDs.get(i) + ","; }
	                        	}
	                        	heurCSocial = calcSocial(graph, map, heurCHostIDs);
                        	}

                        	outputText.add("Heuristic 1 hosts are " + heurAHosts);
                        	outputText.add("Average social awkwardness = " + heurASocial);
                        	outputText.add("		# of hosts: " + heurAHostIDs.size());
                        	outputText.add("Heuristic 2 hosts are " + heurBHosts);
                        	outputText.add("Average social awkwardness = " + heurBSocial);
                        	outputText.add("		# of hosts: " + heurBHostIDs.size());
                        	outputText.add("My heuristic hosts are " + heurCHosts);
                        	outputText.add("Average social awkwardness = " + heurCSocial);
                        	outputText.add("		# of hosts: " + heurCHostIDs.size());
                        }
                        
                        // Parts 3 & 4
                        else
                        {                        	
                        	if (currentCase > 1) { outputText.add("\nTest case " + currentCase + "."); }
                        	else { outputText.add("Test case " + currentCase + "."); }
                        	double heurASocial = 0.00;
                        	double heurBSocial = 0.00;
                        	double heurCSocial = 0.00;
                        	Set<Integer> vSet = graph.vertexSet();
                        	Set<Integer> xSet = new HashSet<Integer>();
                        	for (Integer x : vSet) { map.put(x, -1.0); }
                        	
                        	for (Integer x : vSet)
                        	{
                        		boolean checker = true;
                        		for (Integer i : hostIDs)
                        		{
                        			if (x == i) { checker = false; }
                        		}
                        		if (checker == true) { xSet.add(x); }
                        	}
                        	
                        	// Heuristic 1
                        	Set<Integer> ySet = new HashSet<Integer>(); ySet.addAll(xSet);
                        	int hosts = 0;
                        	while (hosts != numParties && hosts < numParties)
                        	{
                        		Integer newHost = findPopular(graph, ySet);
                        		ySet.remove(newHost);
                        		heurAHostIDs.add(newHost);
                        		hosts++;
                        	}
                        	
                        	String heurAHosts = "";
                        	for (int i = 0; i < heurAHostIDs.size(); i++)
                        	{
                        		if (!(i + 1 < heurAHostIDs.size())) { heurAHosts += heurAHostIDs.get(i); }
                        		else { heurAHosts += heurAHostIDs.get(i) + ","; }
                        	}

                        	heurASocial = calcSocial(graph, map, heurAHostIDs);
                        	// END Heuristic 1
                        	
                        	// Heuristic 2
                        	for (Integer x : vSet) { map.put(x, -1.0); }
                        	Set<Integer> zSet = new HashSet<Integer>(); zSet.addAll(xSet);
                        	int hostsB = 0;
                        	Integer newHost = findPopular(graph, zSet);
                        	zSet.remove(newHost);
                        	heurBHostIDs.add(newHost);
                        	hostsB++;
                        	updateSocials(heurBHostIDs, zSet, map, graph);
                        	while (hostsB != numParties && hostsB < numParties)
                        	{
                        		Integer newHostSub = findAwkward(map, zSet);
                        		zSet.remove(newHostSub);
                        		heurBHostIDs.add(newHostSub);
                        		hostsB++;
                        		updateSocials(heurBHostIDs, zSet, map, graph);
                        	}
                        	
                        	String heurBHosts = "";
                        	for (int i = 0; i < heurBHostIDs.size(); i++)
                        	{
                        		if (!(i + 1 < heurBHostIDs.size())) { heurBHosts += heurBHostIDs.get(i); }
                        		else { heurBHosts += heurBHostIDs.get(i) + ","; }
                        	}
                        	
                        	heurBSocial = calcSocial(graph, map, heurBHostIDs);
                        	// END Heuristic 2
                        	
                        	// Custom Heuristic
                        	//double randomHeurSocial = -1.0;		// for random brute force
                        	for (Integer x : vSet) { map.put(x, -1.0); }
                        	Set<Integer> uSet = new HashSet<Integer>(); uSet.addAll(xSet);
                        	int hostsC = 0;
                        	Integer newHostB = findPopular(graph, uSet);
                        	uSet.remove(newHostB);
                        	heurCHostIDs.add(newHostB);
                        	hostsC++;
                        	updateSocials(heurCHostIDs, uSet, map, graph);
                        	while (hostsC != numParties && hostsC < numParties)
                        	{
                        		Integer newHostSub = findAwkwardNeighbor(map, uSet, graph);
                        		uSet.remove(newHostSub);
                        		heurCHostIDs.add(newHostSub);
                        		hostsC++;
                        		updateSocials(heurCHostIDs, uSet, map, graph);
                        	}
                        	
                        	String heurCHosts = "";
                        	for (int i = 0; i < heurCHostIDs.size(); i++)
                        	{
                        		if (!(i + 1 < heurCHostIDs.size())) { heurCHosts += heurCHostIDs.get(i); }
                        		else { heurCHosts += heurCHostIDs.get(i) + ","; }
                        	}
                        	
                        	heurCSocial = calcSocial(graph, map, heurCHostIDs);
                        	
                        	// Handles the weird test case #7 from all_in.txt
                        	// In that case, using heuristic 1/2 is fine, since we're beating every other test case anyway
                        	// So here we just set heurCSocial to be equal to the lower of the two original heuristic values
                        	if (heurCSocial > heurASocial || heurCSocial > heurBSocial)
                        	{
                        		if (heurASocial > heurBSocial) { heurCSocial = heurBSocial; heurCHosts = heurBHosts; }
                        		else { heurCSocial = heurASocial; heurCHosts = heurAHosts; }
                        	}
                        	
                        	/* RANDOM HOST BRUTE FORCE (way too slow but fun)
                        	if (!((heurCSocial < heurASocial) && (heurCSocial < heurBSocial)))
                        	{     
                            	if (numParties > 0)
                                {
                            		System.out.println("Test case : " + currentCase + ".");
        	                    	System.out.println("File: " + s);
                            		boolean a = false;
    	                    		boolean b = false;
    	                    		boolean c = false;
    	                    		Map<String, Double> mapR = new HashMap<String, Double>();
    	                    		String mapString = "";
        	                    	while (randomHeurSocial == -1.0 || c == false)
        	                    	{
        	                    		a = false; b = false; c = false;
        	                    		heurCHosts = "";
        	                    		mapString = "";
        	                    		heurRHostIDs.addAll(getRandomHosts(numParties, globalSet));
        	                    		if (!mapR.containsKey(mapString)) 
        	                    		{
        	                    			Set<Integer> rSet = new HashSet<Integer>(); rSet.addAll(heurRHostIDs);
            	                        	for (int i = 0; i < heurRHostIDs.size(); i++)
            	                        	{
            	                        		if (!(i + 1 < heurRHostIDs.size())) { heurCHosts += heurRHostIDs.get(i); mapString += heurRHostIDs.get(i); }
            	                        		else { heurCHosts += heurRHostIDs.get(i) + ","; mapString += heurRHostIDs.get(i); }
            	                        	}
            	                        	updateSocials(heurRHostIDs, rSet, map, graph);
            	                    		randomHeurSocial = calcSocial(graph, map, heurRHostIDs);
            	                    		if (randomHeurSocial <= heurASocial) { a = true; }
            	                    		if (randomHeurSocial <= heurBSocial) { b = true; }
            	                    		if (a && b) { c = true; }
            	                    		else { a = false; b = false; c = false; }
            	                    		mapR.put(mapString, randomHeurSocial);
            	                    		System.out.println(mapString);
            	                    		
        	                    		}
        	                    		heurRHostIDs.removeAll(heurRHostIDs);
        	                    		
        	                    	}
        	                    	System.out.println("ENDED WHILE LOOP!");
        	                    	if (heurCSocial > randomHeurSocial) { heurCSocial = randomHeurSocial; System.out.println("heurCSocial: " + heurCSocial + " randomHeurSocial: " + randomHeurSocial); }
                                }
                        	}
                        	*/ // END RANDOM BRUTE FORCE
                        	// END Custom Heuristic

                        	outputText.add("Heuristic 1 hosts are " + heurAHosts);
                        	outputText.add("Average social awkwardness = " + heurASocial);
                        	outputText.add("Heuristic 2 hosts are " + heurBHosts);
                        	outputText.add("Average social awkwardness = " + heurBSocial);
                        	outputText.add("My heuristic hosts are " + heurCHosts);
                        	outputText.add("Average social awkwardness = " + heurCSocial);
                        }

                        // Update test case status
                        currentCase++;
                        testCases--;
                    }
                }
                inputStream.close();
            }catch (IOException e){ e.printStackTrace(); }
        	
        	// Basically a delimiter within the outputText array so that when we write to files later it is clear what text came from which file
        	outputText.add("END OF FILE");
        }
       
 
        // Fill all outputFiles by reading through outputText array and switching to a new file each time "END OF FILE" is found
        int index = 0;
        for (String a : outputFiles)
        {
        	File file = new File(a);        	
        	PrintWriter writer = new PrintWriter(file, "UTF-8"); 
        	while (!outputText.get(index).equals("END OF FILE")) { writer.println(outputText.get(index)); index++; }
        	if (outputText.get(index).equals("END OF FILE")) { index++; }
        	writer.close(); 
        }
	}
	
	public static double round(double value, int places) 
	{
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	public static Integer countNeighbors(SimpleGraph<Integer, DefaultWeightedEdge> graph, Integer vertex)
	{
		Integer numNeighbors = 0;
		List<Integer> edges = Graphs.neighborListOf(graph, vertex);
		for (Integer e : edges) { numNeighbors++; }
		return numNeighbors;
	}
	
	public static void testPrintNeighbors(SimpleGraph<Integer, DefaultWeightedEdge> graph, Integer vertex)
	{
		List<Integer> edges = Graphs.neighborListOf(graph, vertex);
		for (Integer e : edges) { System.out.println(e); }
	}
	
	
	public static Integer findPopular(SimpleGraph<Integer, DefaultWeightedEdge> graph, Set<Integer> vSet)
	{
		Integer friendCount = 0;
		Integer popVert = 0;
		for (Integer i : vSet)
		{
			if (countNeighbors(graph, i) > friendCount)
			{
				friendCount = countNeighbors(graph, i);
				popVert = i;
			}
			
			else if (countNeighbors(graph, i) == friendCount)
			{
				if (popVert > i) { popVert = i; }
			}
		}
		
		return popVert;
	}
	
	public static Integer findUnpopular(SimpleGraph<Integer, DefaultWeightedEdge> graph, Set<Integer> vSet)
	{
		Integer friendCount = 1000000;
		Integer popVert = 0;
		for (Integer i : vSet)
		{
			if (countNeighbors(graph, i) < friendCount)
			{
				friendCount = countNeighbors(graph, i);
				popVert = i;
			}
			
			else if (countNeighbors(graph, i) == friendCount)
			{
				if (popVert > i) { popVert = i; }
			}
		}
		
		return popVert;
	}
	
	public static Integer findSuperPopular(SimpleGraph<Integer, DefaultWeightedEdge> graph, Set<Integer> vSet)
	{
		Integer friendCount = 0;
		Integer popVert = 0;

		for (Integer i : vSet)
		{
			int tempCounter = 0;
			tempCounter += countNeighbors(graph, i);
			List<Integer> edges = Graphs.neighborListOf(graph, i);
			for (Integer e : edges) { tempCounter += countNeighbors(graph, e); }
			
			if (tempCounter > friendCount)
			{
				friendCount = tempCounter;
				popVert = i;
			}
			else if (tempCounter == friendCount)
			{
				if (popVert > i) { popVert = i; }
			}
		}
		
		return popVert;
	}
	
	public static Integer findAwkward(Map<Integer, Double> map, Set<Integer> vertexSet)
	{
		double mostAwk = 0.00;
		Integer found = -1;
		Set<Entry<Integer, Double>> mapSet = map.entrySet();
		for (Entry<Integer, Double> e : mapSet)
		{
			Integer entry = e.getKey();
			if (vertexSet.contains(entry))
			{
				if (map.get(entry) > mostAwk)
				{
					mostAwk = map.get(entry);
					found = entry;
				}
			}
		}
		
		return found;
	}
	
	public static double calcSocial(SimpleGraph<Integer, DefaultWeightedEdge> graph, Map<Integer, Double> map, ArrayList<Integer> hostIDs)
	{
		double totalSocial = 0.00;
    	double vertices = 0.00;
    	double avgSocial = 0.00;
    	Set<Integer> vSet = graph.vertexSet();
    	Set<Integer> xSet = new HashSet<Integer>();
    	for (Integer x : vSet) { map.put(x, -1.0); }
    	
    	for (Integer x : vSet)
    	{
    		boolean checker = true;
    		for (Integer i : hostIDs)
    		{
    			if (x == i) { checker = false; }
    		}
    		if (checker == true) { xSet.add(x); }
    	}
    	                                                              	
    	for (Integer i : hostIDs)
    	{
    		for (Integer v : xSet)
    		{                        		
				GraphPath<Integer, DefaultWeightedEdge> shortest_path = DijkstraShortestPath.findPathBetween(graph, i, v);
				if ((map.get(v).equals(-1.0)) || map.get(v) > shortest_path.getWeight()) { map.put(v, shortest_path.getWeight()); }
    		}
    	}
    	
    	for (Integer v : xSet)
    	{
    		totalSocial += map.get(v);
    		vertices++;
    	}
    	
    	avgSocial = totalSocial / vertices;
    	avgSocial = round(avgSocial, 2);
		return avgSocial;
	}
	
	public static void updateSocials(ArrayList<Integer> hostIDs, Set<Integer> xSet, Map<Integer, Double> map, SimpleGraph<Integer, DefaultWeightedEdge> graph)
	{
		for (Integer i : hostIDs)
    	{
    		for (Integer v : xSet)
    		{                        		
				GraphPath<Integer, DefaultWeightedEdge> shortest_path = DijkstraShortestPath.findPathBetween(graph, i, v);
				if ((map.get(v).equals(-1.0)) || map.get(v) > shortest_path.getWeight()) { map.put(v, shortest_path.getWeight()); }
    		}
    	}
	}
	
	public static Integer findAwkwardNeighbor(Map<Integer, Double> map, Set<Integer> vertexSet, SimpleGraph<Integer, DefaultWeightedEdge> graph)
	{
		Integer found = 0;
		Integer awkward = findAwkward(map, vertexSet);
		Set<Integer> vSet = new HashSet<Integer>();
		List<Integer> edges = Graphs.neighborListOf(graph, awkward);
		for (Integer e : edges) { vSet.add(e); }
		found = findPopular(graph, vSet);
		return found;
	}
	

	// Generate a random array of hosts (making sure to choose one random host at a time, then removing it from pool of possible selections)
	// Use those hosts to calculate average social awkwardness
	// Use map to hold each randomized selection of host's avg social awk value
	// Each time we generate a new array of hosts, check the map to see if we already generated it
	// If so, do not recalculate just generate another random array
	// Keep doing this process until you calculate a average social awk value lower than both heurASocial and heurBSocial
	public static ArrayList<Integer> getRandomHosts(int requiredHosts, Set<Integer> vSet)
	{
		ArrayList<Integer> vertexSet = new ArrayList<Integer>();
		ArrayList<Integer> randoms = new ArrayList<Integer>();
		
		int highestV = 0;
		
		for (Integer v : vSet)
		{
			vertexSet.add(v);
			if (v > highestV) { highestV = v; }
		}
		
		for (int i = 0; i < requiredHosts; i++)
		{
			Integer randomHost = vertexSet.get(new Random().nextInt(vertexSet.size()));
			randoms.add(randomHost);
		}
		
		return randoms;
	}
}
