import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;

public class IRoadTrip {
  private Map<String, Map<String, Integer>> adjacencyList; 
  private Map<String, String> stateNameMap;

  public IRoadTrip(String[] args) {
      if (args.length != 3) {
          System.err.println("Usage: java IRoadTrip borders.txt capdist.csv state_name.tsv");
          
      }

  
      adjacencyList = new HashMap<>();
      stateNameMap = new HashMap<>();

      
      readBordersFile(args[0]);
      readCapDistFile(args[1]);
      readStateNameFile(args[2]);
  
  }
  private void readBordersFile(String fileName) {
      try (Scanner scanner = new Scanner(new File(fileName))) {
          while (scanner.hasNextLine()) {
              String line = scanner.nextLine();
              int equalsIndex = line.indexOf('=');
              if (equalsIndex != -1) {
                  String country = line.substring(0, equalsIndex).trim();
                  if (!adjacencyList.containsKey(country)) {
                      adjacencyList.put(country, new HashMap<>());
                  }
                  String[] neighbors = line.substring(equalsIndex + 1).split(";");
                  for (String neighbor : neighbors) {
                    String x=neighbor.replaceAll(",", "").trim();
                    x=x.replaceAll("km", "").trim();

                      String[] neighborInfo = x.split(" ");
                      String coun="";
                      for(int i=0;i<neighborInfo.length-1;i++){
                        coun=coun+neighborInfo[i]+" ";
                      }
                      if (coun==""){
                        continue;
                      }
                      int distance = Integer.parseInt(neighborInfo[neighborInfo.length-1]);
                      adjacencyList.get(country).put(coun,distance);
                  }
              }
          }
      } catch (Exception e) {
          System.err.println("Error reading borders file: " + e.getMessage());
          System.exit(1);
      }
  }

  private void readCapDistFile(String fileName) {
    try (Scanner scanner = new Scanner(new File(fileName))) {
      if (scanner.hasNextLine()) {
        scanner.nextLine();
      }
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        String[] parts = line.split(",");
        String countryA = getDecodedCountry(parts[1].trim());
        String countryB = getDecodedCountry(parts[3].trim());
        int distance = Integer.parseInt(parts[4].trim());
  
     
        if (adjacencyList.containsKey(countryA) && adjacencyList.get(countryA).containsKey(countryB)) {
          adjacencyList.get(countryA).put(countryB, distance);
          adjacencyList.get(countryB).put(countryA, distance);
        }
      }
    } 
    catch (Exception e) {
      System.err.println("Error reading capdist file: " + e.getMessage());
      System.exit(1);
    }
  }

  private void readStateNameFile(String fileName) {
    try (Scanner scanner = new Scanner(new File(fileName))) {
      if (scanner.hasNextLine()) {
        scanner.nextLine();
      }
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        String[] parts = line.split("\t");
  
        String encodedCountry = parts[1].trim();
        String decodedCountry = parts[2].trim();
        stateNameMap.put(encodedCountry, decodedCountry);
      }
    } 
  catch (Exception e) {
    System.err.println("Error reading state_name file: " + e.getMessage());
    System.exit(1);
  }
}

  private String getDecodedCountry(String encodedCountry) {
    if (stateNameMap.containsKey(encodedCountry)) {
      return stateNameMap.get(encodedCountry);
    }
    return null;
  }

  
  float getDistance(String country1, String country2) {
      if (!isValidCountry(country1) || !isValidCountry(country2)) {
          return -1; 
      }

      PriorityQueue<Map.Entry<String, Integer>> minHeap = new PriorityQueue<>(
          Comparator.comparingInt(Map.Entry::getValue)
      );
      Map<String, Integer> distances = new HashMap<>();
      Set<String> visited = new HashSet<>();

     
      for (String country : adjacencyList.keySet()) {
          distances.put(country, Integer.MAX_VALUE);
      }
      distances.put(country1, 0);
      minHeap.offer(new AbstractMap.SimpleEntry<>(country1, 0));

      while (!minHeap.isEmpty()) {
          Map.Entry<String, Integer> entry = minHeap.poll();
          String current = entry.getKey();
          int currentDist = entry.getValue();

          if (!visited.add(current)) {
              continue;
          }

          if (current.equals(country2)) {
              return currentDist;
          }

          for (Map.Entry<String, Integer> neighbor : adjacencyList.get(current).entrySet()) {
              String next = neighbor.getKey();
              int nextDist = currentDist + neighbor.getValue();
              if (nextDist < distances.get(next)) {
                  distances.put(next, nextDist);
                  minHeap.offer(new AbstractMap.SimpleEntry<>(next, nextDist));
              }
          }
      }

      return -1;
  }

  private List<String> findPath(String country1, String country2) {
      List<String> path = new ArrayList<>();
      if (!isValidCountry(country1) || !isValidCountry(country2)) {
          return path;
      }

      if (!adjacencyList.containsKey(country1) || !adjacencyList.containsKey(country2)) {
          return path;
      }

      Queue<String> queue = new LinkedList<>();
      Map<String, String> prev = new HashMap<>();
      Set<String> visited = new HashSet<>();

      queue.offer(country1);
      visited.add(country1);

      while (!queue.isEmpty()) {
          String current = queue.poll();
          if (current.equals(country2)) {
              while (current != null) {
                  path.add(0, current);
                  current = prev.get(current);
              }
              return path;
          }

          Map<String, Integer> neighbors = adjacencyList.get(current);
          if (neighbors != null) {
              for (String neighbor : neighbors.keySet()) {
                  if (visited.add(neighbor)) {
                      queue.offer(neighbor);
                      prev.put(neighbor, current);
                  }
              }
          }
      }

      return path;
  }




  public void acceptUserInput() {
      Scanner scanner = new Scanner(System.in);

      while (true) {
          System.out.print("Enter the name of the first country (type EXIT to quit): ");
          String country1 = scanner.nextLine().trim();

          if (country1.equalsIgnoreCase("EXIT")) {
              break;
          }

          boolean isValidCountry1 = isValidCountry(country1);

          if (!isValidCountry1) {
              System.out.println("Invalid country name. Please enter a valid country name.");
              continue; 
          }

          System.out.print("Enter the name of the second country (type EXIT to quit): ");
          String country2 = scanner.nextLine().trim();

          if (country2.equalsIgnoreCase("EXIT")) {
              break;
          }

          boolean isValidCountry2 = isValidCountry(country2);

          if (!isValidCountry2) {
              System.out.println("Invalid country name. Please enter a valid country name.");
              continue;
          }
          List<String> path = findPath(country1, country2);

          if (!path.isEmpty()) {
              System.out.println("Route from " + country1 + " to " + country2 + ":");
              for (String step : path) {
                  System.out.println("* " + step);
              }
          } else {
              System.out.println("No path found between " + country1 + " and " + country2);
          }
      }

      scanner.close();
  }


  private boolean isValidCountry(String inputCountry) {
    for (String country : adjacencyList.keySet()) {
      if (country.equals(inputCountry) || country.contains(inputCountry) || inputCountry.contains(country)) {
        return true;
      }
    }
    for (String encodedCountry : stateNameMap.keySet()) {
      if (encodedCountry.equals(inputCountry)) {
        return true;
      }
    }
    return false;
  }
  public class Main {

    public static void main(String[] args) {
      IRoadTrip a3 = new IRoadTrip(new String[]{"borders.txt", "capdist.csv","state_name.tsv"});

      System.out.println(a3.getDistance("USF", "My House"));
      System.out.println(a3.getDistance("France", "Spain"));
      System.out.println(a3.getDistance("Canada", "Panama"));
    }
  }
}
