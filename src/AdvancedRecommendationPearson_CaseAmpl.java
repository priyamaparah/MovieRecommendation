

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class AdvancedRecommendationPearson_CaseAmpl {

	private MovieRecommUserData[] users;
    private int UserAc;
    private int simNum = 33;
    private double[] sim;
    private double[][] rating;
    private int[] SimUsersTop;    
    private double[] acgOfAllUsers;
    public int noOfUsers = 200;
    private int noOfMovies = 1000;
    private double userAvgAc;
    private ArrayList<Integer> movieToWrite;
    public AdvancedRecommendationPearson_CaseAmpl() throws FileNotFoundException {
    	
        sim = new double[noOfUsers];
        rating = new double[noOfUsers][noOfMovies];
        SimUsersTop = new int[simNum];
        
        Scanner s1 = new Scanner(new BufferedReader(new FileReader(new File("train.txt"))));
        for (int i = 0; i < noOfUsers; i++) {
            for (int j = 0; j < noOfMovies; j++) {
                rating[i][j] = s1.nextInt();
            }
        }
    }


    public void pearsonSimprediction(String testData, String resFile) throws FileNotFoundException, IOException {
        Scanner s = new Scanner(new BufferedReader(new FileReader(new File(testData))));
        PrintStream out = new PrintStream(new File(resFile));
        UserAc = s.nextInt() - 1;                          
        while (s.hasNext()) {
            movieToWrite = new ArrayList<Integer>();
            int[] currUser = new int[noOfMovies];
            Arrays.fill(currUser, -1);
            int nextUser = getNextUserId(currUser, s);
            if(UserAc == 332) {
            	System.out.println();
            }
            userAvgAc = getAvg(currUser);           
            for (int movie = 0; movie < 1000; movie++) {
                if (currUser[movie] == 0) {                 
                	if(movie == 7){
                		System.out.println();
                	}
                	getAvgOfAllBasedOnMovie(movie);
                    getCosineSim(currUser);
                    getTopSimiliarUsers(movie);
                    movieToWrite.add(movie);
                    currUser[movie] = refactor((int) Math.round(getfinalPr(movie)));  
                }                                                                 
            }
            movieToWrite(UserAc, currUser, out);
            UserAc = nextUser;
            if(UserAc == 203){
            	System.out.println("   ");
            }
        }
       
        out.flush();
        out.close();
    }
    
    public int getNextUserId(int[] activeUser, Scanner s) {
        int id = UserAc;
        while (id == UserAc) {
            try {
                activeUser[s.nextInt() - 1] = s.nextInt();
                if (s.hasNext()) {
                    id = s.nextInt() - 1;

                }
            } catch (Exception e) {
                break;
            }
        }
        return id;
    }
    private int refactor(int round) {
        if (round > 5) {
            return 5;
        } else if (round <= 0) {            
            System.out.println("Zero Value");
            return refactor((int) Math.round(userAvgAc));
        } else if(round >=1 && round<=5){
            return round;
        }else {
        	System.out.println("value of d" + round);
        	return round;
        }
    }
    


    private void getAvgOfAllBasedOnMovie(int mov) {
        acgOfAllUsers = new double[noOfUsers];
        for (int i = 0; i < noOfUsers; i++) {
            double sum = 0, count = 0;
            for (int j = 0; j < noOfMovies; j++) {
                if (rating[i][j] != 0 && j != mov) {
                    sum += rating[i][j];
                    count++;
                }
            }
            acgOfAllUsers[i] = sum / count;
        }
    }

    private void getTopSimiliarUsers(int movie) {
        int j = 0;
        if (UserAc == 400 && movie == 36) {
            System.out.println("");
        }
       for (int i = noOfUsers - 1; j < simNum && i >= 0; i--) {
            int user = users[i].id;

            if (rating[user][movie] != 0) {
                SimUsersTop[j] = users[i].id;
                j++;
            }
        }
        for (; j < simNum; j++) {
            SimUsersTop[j] = -1;
        }
    }

    private void sortAllUser() {
        users = new MovieRecommUserData[noOfUsers];

        for (int i = 0; i < noOfUsers; i++) {
            users[i] = new MovieRecommUserData(sim[i], i);
        }
        Arrays.sort(users, MovieRecommUserData.BySim);
    }
   
    private void getCosineSim(int[] userAc) {        
         
        for (int user = 0; user < noOfUsers; user++) {
            double XY = 0;
            double X = 1;
            double Y = 1;

            for (int movie = 0; movie < noOfMovies; movie++) {
                if (userAc[movie] > 0 && rating[user][movie] > 0) { 
                    XY += (userAc[movie] - userAvgAc) * (rating[user][movie] - acgOfAllUsers[user]);
                    X += Math.pow((userAc[movie] - userAvgAc), 2);
                    Y += Math.pow((rating[user][movie] - acgOfAllUsers[user]), 2);
                }
            }
            sim[user] = XY / (Math.sqrt(X) * Math.sqrt(Y));
            if(sim[user]==0 && (isAvginCurr(userAc) || isAvgInUser(user))){
                sim[user] = getCos(user, userAc);
            }  
            
            sim[user] = caseAmpf(sim[user]);
        }
        sortAllUser();
    }
    
    private double caseAmpf(double sim) {
        double r = 1.05; //last best 1.05
        double absRating = Math.abs(sim);
        sim = sim * Math.pow(absRating, r - 1);
        return sim;
    }
    
    private double getAvg(int[] currUser) {
        double total = 0, countC = 0;
        for (int i = 0; i < currUser.length; i++) {
            if (currUser[i] >= 1) {
                total += currUser[i];
                countC++;
            }
        }
        return total / countC;
    }
    
    private double getCos(int user, int[] currUser) {
        double XY = 0;
        double X = 1;
        double Y = 1;

        for (int movie = 0; movie < noOfMovies; movie++) {
            if (currUser[movie] != 0 && rating[user][movie] != 0 && currUser[movie] != -1) {
                XY += currUser[movie] * rating[user][movie];
                X += Math.pow(currUser[movie], 2);
                Y += Math.pow(rating[user][movie], 2);
            }
        }
        return  XY / (Math.sqrt(X) * Math.sqrt(Y));  
    }
        
    private boolean isAvgInUser(int user) {

        for (int movie = 0; movie < noOfMovies; movie++) {
            if (rating[user][movie] == acgOfAllUsers[user]) {
                continue;
            } else if(rating[user][movie]>0) {
                return false;
            }
        }
        return true;
    }
    private boolean isAvginCurr(int[] activeUser) {

        for (int movie = 0; movie < noOfMovies; movie++) {
            if (activeUser[movie] == userAvgAc) {
                continue;
            } else if (activeUser[movie] > 0) {
                return false;
            }
        }

        return true;
    }

    private void movieToWrite(int currName, int[] currUser, PrintStream out) {
        for (int movie = 0; movie < currUser.length; movie++) {
            if (currUser[movie] != -1 && movieToWrite.contains(movie)) {

                out.println((currName + 1) + " " + (movie + 1) + " " + (currUser[movie]));   
            }
        }


    }

    private double getfinalPr(int movie) {
        int j = 0;
        double numerator = 0;
        double denominator = 0;
        for (int i = 0; i < simNum && SimUsersTop[i] != -1; i++) {
            int user = SimUsersTop[i];
            
            numerator += sim[user] * (rating[user][movie] - acgOfAllUsers[user]);
            denominator += Math.abs(sim[user]);
            
        }
        if(numerator == 0 && denominator == 0 ){
        	System.out.println();
        }
        double pr = userAvgAc + (numerator / denominator);
        return pr;

    }

    

  

    public static void main(String args[]) throws FileNotFoundException, IOException {
        AdvancedRecommendationPearson_CaseAmpl my = new AdvancedRecommendationPearson_CaseAmpl();
        int num = 5;
        my.pearsonSimprediction("test"+num+".txt", "CASEAMPF"+num+".txt");
      
    }
}
