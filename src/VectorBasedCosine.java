

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Scanner;

public class VectorBasedCosine {

    private MovieRecommUserData[] users;   
    private int sim = 25;
    private double[] simUsers;
    private double[][] ratings;
    private int[] simUsersTop;
    private int currentUser;
    public int noOfUsers = 200;
    private int noOfMovies = 1000;
    private Hashtable<Integer, Integer> movieTowrite;

    public VectorBasedCosine() throws FileNotFoundException {
        simUsers = new double[noOfUsers];
        ratings = new double[noOfUsers][noOfMovies];
        simUsersTop = new int[sim];
        Scanner sc = new Scanner(new BufferedReader(new FileReader(new File("train.txt"))));
        for (int i = 0; i < noOfUsers; i++) {
            for (int j = 0; j < noOfMovies; j++) {
                ratings[i][j] = sc.nextInt();
            }
        }
        
        sc.close();
    }


    

    private double getCurrentAvg(int[] activeUser) {
        double sum = 0;
        double count = 0;
        for (int i = 0; i < activeUser.length; i++) {
            if (activeUser[i] >= 1) {
                sum += activeUser[i];
                count++;
            }
        }
        return sum / count;

    }

    

    public int getNext(int[] activeUser, Scanner s) {
        int name = currentUser;
        while (name == currentUser) {
            try {
                activeUser[s.nextInt() - 1] = s.nextInt();
                if (s.hasNext()) {
                    name = s.nextInt() - 1;
                }
            } catch (Exception e) {
                break;
            }
        }
        return name;
    }
    

    private void getTopSimiliarUsers(int movie) {
        int mov = 0;

        for (int u = noOfUsers - 1; mov < sim && u >= 0; u--) {
            int user = users[u].id;

            if (ratings[user][movie] != 0) {
                simUsersTop[mov] = users[u].id;
                mov++;
            }
        }
        for (; mov < sim; mov++) {
            simUsersTop[mov] = -1;
        }
    }

    private void getCos(int[] activeUser) {
        for (int user = 0; user < noOfUsers; user++) {
            int XY = 0;
            int X = 1;
            int Y = 1;

            for (int movie = 0; movie < noOfMovies; movie++) {
                if (activeUser[movie] != 0 && ratings[user][movie] != 0 && activeUser[movie] != -1) {
                    XY += activeUser[movie] * ratings[user][movie];
                    X += Math.pow(activeUser[movie], 2);
                    Y += Math.pow(ratings[user][movie], 2);
                }
            }
                
           simUsers[user] = XY / (Math.sqrt(X) * Math.sqrt(Y));                                                    
        }
        sortAllUser();

    }
    
    private void sortAllUser() {
        users = new MovieRecommUserData[noOfUsers];

        for (int i = 0; i < noOfUsers; i++) {
            users[i] = new MovieRecommUserData(simUsers[i], i);
        }
        Arrays.sort(users, MovieRecommUserData.BySim);


    }
    
    
    public void getPrediction(String testData, String fileOut) throws FileNotFoundException, IOException {
        Scanner s = new Scanner(new BufferedReader(new FileReader(new File(testData))));
        PrintStream out = new PrintStream(new File(fileOut));
        //fout.write(System.getProperty( "line.separator" ));
        currentUser = s.nextInt() - 1;                           //storing username as one less than the original to pace with array indexes.
        while (s.hasNext()) {       
            int[] currUser = new int[noOfMovies];
            Arrays.fill(currUser, -1);
            int next = getNext(currUser, s);
            movieTowrite = new Hashtable<Integer, Integer>();
            getCos(currUser);
            for (int movie = 0; movie < currUser.length; movie++) {
                double avg = getCurrentAvg(currUser);
                if (currUser[movie] == 0) {
                    movieTowrite.put(movie, 1);
                    getTopSimiliarUsers(movie);
                    currUser[movie] = (int) Math.round(getValue(movie));                     
                    if (currUser[movie] == 0) {                      
                        currUser[movie] = (int) Math.round(avg);
                    }
                    getCos(currUser);                                       
                }
            }
            saveVal(currentUser, currUser, out);
            currentUser = next;
        }
        out.flush();
        out.close();
    }
    
    private void saveVal(int currentName, int[] currUser, PrintStream out) throws IOException {
        for (int movie = 0; movie < currUser.length; movie++) {
            if (currUser[movie] != -1 && movieTowrite.get(movie) != null) {
                out.println((currentName + 1) + " " + (movie + 1) + " " + (currUser[movie]));   //need to add 1
            }
        }
    }

    private double getValue(int movie) {   
        double num = 0;
        double denom = 0;
        for (int i = 0; i < sim && simUsersTop[i] != -1; i++) {
            int user = simUsersTop[i];
            num += simUsers[user] * ratings[user][movie];
            denom += simUsers[user];
        }
        return num / denom;
    }

    public static void main(String args[]) throws FileNotFoundException, IOException {
        VectorBasedCosine r = new VectorBasedCosine();
        int num = 20; 
        r.getPrediction("test"+num+".txt", "myCOSresult_New"+num+".txt");



    }
}
