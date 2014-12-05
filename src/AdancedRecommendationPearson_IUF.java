

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class AdancedRecommendationPearson_IUF {

	private MovieRecommUserData[] users;
    private int UserAc;
    private int simNum = 33;
    private double[] sim;
    private double[][] rating;
    private int[] SimUsersTop;    
    private double[] avgOfAllUsers;
    public int noOfUsers = 200;
    private int noOfMovies = 1000;
    private double userAvgAc;
    private ArrayList<Integer> movieToWrite;
    private double[] iuf;
    public AdancedRecommendationPearson_IUF() throws FileNotFoundException {
    	
        sim = new double[noOfUsers];
        rating = new double[noOfUsers][noOfMovies];
        SimUsersTop = new int[simNum];
        iuf = new double[noOfMovies];
        Scanner s1 = new Scanner(new BufferedReader(new FileReader(new File("train.txt"))));
        for (int i = 0; i < noOfUsers; i++) {
            for (int j = 0; j < noOfMovies; j++) {
                rating[i][j] = s1.nextInt();
                if (rating[i][j] > 0) {
                    iuf[j]++;
                }
            }
        }
        s1.close();
        for (int movie = 0; movie < noOfMovies; movie++) {
            if (iuf[movie] > 0) {
                iuf[movie] = Math.log(noOfUsers / iuf[movie]);
            }

        }
    }

    public void pearsonSimprediction(String testData, String resFile) throws FileNotFoundException, IOException {
        Scanner s = new Scanner(new BufferedReader(new FileReader(new File(testData))));
        PrintStream fout = new PrintStream(new File(resFile));
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
            		double pr = 0.0;
                if (currUser[movie] == 0) {                 
                	if(movie == 7){
                		System.out.println();
                	}
                	getAvgOfAllBasedOnMovie(movie);
                    getCosineSim(currUser);
                    getTopSimiliarUsers(movie);
                    movieToWrite.add(movie);
                    pr = Math.round(getComputedPr(movie));
                    currUser[movie] = refactor((int)pr);                                                                                
                }                                                                  
            }
            movietooutput(UserAc, currUser, fout);
            UserAc = nextUser;
            if(UserAc == 203){
            	System.out.println("   ");
            }
        }
       
        fout.flush();
        fout.close();
    }
    

	public int getNextUserId(int[] currUser, Scanner s) {
        int id = UserAc;
        while (id == UserAc) {
            try {
                currUser[s.nextInt() - 1] = s.nextInt();
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
        	return refactor((int) Math.round(userAvgAc));
        } else if(round >=1 && round<=5){
            return round;
        }else {
        	System.out.println("value of d" + round);
        	return round;
        }
		
    }
    


    private void getAvgOfAllBasedOnMovie(int mov) {
        avgOfAllUsers = new double[noOfUsers];
        for (int i = 0; i < noOfUsers; i++) {
            double sum = 0, count = 0;
            for (int j = 0; j < noOfMovies; j++) {
                if (rating[i][j] != 0 && j != mov) {
                    sum += rating[i][j];
                    count++;
                }
            }
            avgOfAllUsers[i] = sum / count;
        }
    }

    private void getTopSimiliarUsers(int movie) {
        int j = 0;
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
                    XY += ((userAc[movie] * iuf[movie]) - (userAvgAc * iuf[movie])) * ((rating[user][movie] * iuf[movie])- (avgOfAllUsers[user]* iuf[movie]));
                    X += Math.pow(((userAc[movie]* iuf[movie]) - (userAvgAc* iuf[movie])), 2);
                    Y += Math.pow(((rating[user][movie]* iuf[movie]) - (avgOfAllUsers[user] * iuf[movie])), 2);
                }
            }
            sim[user] = XY / (Math.sqrt(X) * Math.sqrt(Y));
            if(sim[user]==0 && (isAvgInCurr(userAc) || isAvgInUser(user))){
                sim[user] = cosineSimilarity(user, userAc);
            }                                  
        }
        sortAllUser();
    }
        private double cosineSimilarity(int user, int[] activeUser) {
        double XY = 0;
        double X = 1;
        double Y = 1;

        for (int movie = 0; movie < noOfMovies; movie++) {
            if (activeUser[movie] != 0 && rating[user][movie] != 0 && activeUser[movie] != -1) {
                XY += activeUser[movie] * rating[user][movie];
                X += Math.pow(activeUser[movie], 2);
                Y += Math.pow(rating[user][movie], 2);
            }
        }
        return  XY / (Math.sqrt(X) * Math.sqrt(Y));  
    } 
        
    private boolean isAvgInUser(int user) {

        for (int movie = 0; movie < noOfMovies; movie++) {
            if (rating[user][movie] == avgOfAllUsers[user]) {
                continue;
            } else if(rating[user][movie]>0) {
                return false;
            }
        }
        return true;
    }
    private boolean isAvgInCurr(int[] currUser) {

        for (int movie = 0; movie < noOfMovies; movie++) {
            if (currUser[movie] == userAvgAc) {
                continue;
            } else if (currUser[movie] > 0) {
                return false;
            }
        }

        return true;
    }

    private void movietooutput(int currName, int[] currUser, PrintStream fout) {
        for (int movie = 0; movie < currUser.length; movie++) {
            if (currUser[movie] != -1 && movieToWrite.contains(movie)) {

                fout.println((currName + 1) + " " + (movie + 1) + " " + (currUser[movie]));  
            }
        }


    }

    private double getComputedPr(int movie) {
        double num = 0;
        double denom = 0;
        for (int i = 0; i < simNum && SimUsersTop[i] != -1; i++) {
            int user = SimUsersTop[i];
            
            num += sim[user] * (rating[user][movie] - avgOfAllUsers[user]);
            denom += Math.abs(sim[user]);
            
        }
        if(num == 0 && denom == 0 ){
        	System.out.println();
        }
        double pr = userAvgAc + (num / denom);
        return pr;

    }

    private double getAvg(int[] curUser) {
        double total = 0, totactC = 0;
        for (int i = 0; i < curUser.length; i++) {
            if (curUser[i] >= 1) {
                total += curUser[i];
                totactC++;
            }
        }
        return total / totactC;
    }

  

    public static void main(String args[]) throws FileNotFoundException, IOException {
        AdancedRecommendationPearson_IUF my = new AdancedRecommendationPearson_IUF();
        int num = 20;
        my.pearsonSimprediction("test"+num+".txt", "IUF"+num+".txt");
      
    }
}
