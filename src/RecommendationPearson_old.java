import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


public class RecommendationPearson_old {
	
	private int activeUserName;
	private ArrayList<Integer> movieToWrite;
	private double activeUserAverage;
	private double[] averageOfUser;
	private double[][] trainData;
	private MovieRecommUserData[] users;
	private int k = 33;
	private double[] simliarUsers;
	private int[] topUsers;
	private int noOfUsers = 200;
	private int noOfMovies= 1000;		
	public RecommendationPearson_old() throws FileNotFoundException{
		simliarUsers = new double[noOfUsers];
        trainData = new double[noOfUsers][noOfMovies];
        topUsers = new int[k];
        Scanner s = new Scanner(new BufferedReader(new FileReader(new File("train.txt"))));

        for (int i = 0; i < noOfUsers; i++) {

            for (int j = 0; j < noOfMovies; j++) {
                trainData[i][j] = s.nextInt();
            }
        }
	}
	
	private void predictBasedOnPearsonSimilarity(String testData, String resultFile) throws FileNotFoundException {
		// TODO Auto-generated method stub
		 Scanner s = new Scanner(new BufferedReader(new FileReader(new File(testData))));
		 activeUserName = s.nextInt() - 1;
		 PrintStream fout = new PrintStream(new File(resultFile));
		 while (s.hasNext()) {
			 	movieToWrite = new ArrayList<Integer>();
	            int[] activeUserTable = new int[noOfMovies];
	            Arrays.fill(activeUserTable, -1);
	            int nextUser = getNextUser(activeUserTable, s);
	            if(activeUserName == 332) {
	            	System.out.println();
	            }
	            activeUserAverage = findAverage(activeUserTable);            //put outside from for loop
	            for (int movie = 0; movie < noOfMovies; movie++) {
	                if (activeUserTable[movie] == 0) { 
	                	if(movie == 7){
	                		System.out.println();
	                	}
	                    movieToWrite.add(movie);
	                    findAverageOfAll(movie);
	                    findCosineSimilarity(activeUserTable);
	                    findTopKNearestFor(movie);
	                    activeUserTable[movie] = adjust((int) Math.round(predict(movie)));                                                           	                    
	                }                                                                  
	            }
	            write(activeUserName, activeUserTable, fout);
	            activeUserName = nextUser;
		 }
		 fout.flush();
	     fout.close();
	}
		 
	private void write(int activeUserName2, int[] activeUserTable,
			PrintStream fout) {
		// TODO Auto-generated method stub
		for (int movie = 0; movie < activeUserTable.length; movie++) {
            if (activeUserTable[movie] != -1 && movieToWrite.contains(movie)) {

                fout.println((activeUserName + 1) + " " + (movie + 1) + " " + (activeUserTable[movie]));   //need to add 1
            }
        }
		
	}

	private double predict(int movie) {
		// TODO Auto-generated method stub
		int j = 0;
        double numerator = 0;
        double denominator = 0;
        for (int i = 0; i < k && topUsers[i] != -1; i++) {
            int user = topUsers[i];
            
            numerator += simliarUsers[user] * (trainData[user][movie] - averageOfUser[user]);
            denominator += Math.abs(simliarUsers[user]);
            
        }
        if(numerator == 0 && denominator == 0 ){
        	System.out.println();
        }
        double pr = activeUserAverage + (numerator / denominator);
        return pr;
	}

	private int adjust(int round) {
		// TODO Auto-generated method stub
		if (round > 5) {
            return 5;
        } else if (round <= 0) {   
            return 1;
        } else if(round >=1 && round<=5){
            return round;
        }else {
        	System.out.println("value of d" + round);
        	return round;
        }
	}

	private void findTopKNearestFor(int movie) {
		// TODO Auto-generated method stub
		 int j = 0;

	        for (int i = noOfUsers - 1; j < k && i >= 0; i--) {
	            int user = users[i].id;

	            if (trainData[user][movie] != 0) {
	                topUsers[j] = users[i].id;
	                j++;
	            }
	        }
	        for (; j < k; j++) {
	            topUsers[j] = -1;
	        }
		
	}

	private void findCosineSimilarity(int[] activeUserTable) {
		// TODO Auto-generated method stub
		for (int user = 0; user < noOfUsers; user++) {
            double innerProduct = 0;
            double lengthOfActiveUser = 1;
            double lengthofUser = 1;

           


            for (int movie = 0; movie < noOfMovies; movie++) {

                if (activeUserTable[movie] > 0 && trainData[user][movie] > 0) {  //(activeUser[movie] - activeUserAverage) add logic to rectify this to be 0


                    innerProduct += (activeUserTable[movie] - activeUserAverage) * (trainData[user][movie] - averageOfUser[user]);
                    lengthOfActiveUser += Math.pow((activeUserTable[movie] - activeUserAverage), 2);
                    lengthofUser += Math.pow((trainData[user][movie] - averageOfUser[user]), 2);
                }
            }

            simliarUsers[user] = innerProduct / (Math.sqrt(lengthOfActiveUser) * Math.sqrt(lengthofUser));
            if(simliarUsers[user]==0&&(isElementAverageInActiveUser(activeUserTable)||isElementAverage(user))){
                simliarUsers[user]=cosineSimilarity(user, activeUserTable);
            }                                  
        }
        sortUsers();
	}
	
	private double cosineSimilarity(int user, int[] activeUserTable) {
		// TODO Auto-generated method stub
		double innerProduct = 0;
        double lengthOfActiveUser = 1;
        double lengthofUser = 1;

        for (int movie = 0; movie < noOfUsers; movie++) {
            if (activeUserTable[movie] != 0 && trainData[user][movie] != 0 && activeUserTable[movie] != -1) {
                innerProduct += activeUserTable[movie] * trainData[user][movie];
                lengthOfActiveUser += Math.pow(activeUserTable[movie], 2);
                lengthofUser += Math.pow(trainData[user][movie], 2);
            }
        }

        return  innerProduct / (Math.sqrt(lengthOfActiveUser) * Math.sqrt(lengthofUser));
	}

	private boolean isElementAverage(int user) {
		// TODO Auto-generated method stub
		for (int movie = 0; movie < noOfUsers; movie++) {
            if (trainData[user][movie] == averageOfUser[user]) {
                continue;
            } else if(trainData[user][movie]>0) {
                return false;
            }
        }

        return true;
	}

	private boolean isElementAverageInActiveUser(int[] activeUserTable) {
		// TODO Auto-generated method stub
		for (int movie = 0; movie < noOfUsers; movie++) {
            if (activeUserTable[movie] == activeUserAverage) {
                continue;
            } else if (activeUserTable[movie] > 0) {
                return false;
            }
        }

        return true;
	}

	private void sortUsers() {
        users = new MovieRecommUserData[noOfUsers];

        for (int i = 0; i < noOfUsers; i++) {
            users[i] = new MovieRecommUserData(simliarUsers[i], i);
        }
        Arrays.sort(users, MovieRecommUserData.BySim);


    }

	private void findAverageOfAll(int movie) {
		// TODO Auto-generated method stub
		averageOfUser = new double[noOfUsers];
        for (int i = 0; i < noOfUsers; i++) {
            double sum = 0, count = 0;
            for (int j = 0; j < noOfMovies; j++) {
                if (trainData[i][j] != 0 && j != movie) {
                    sum += trainData[i][j];
                    count++;
                }
            }
            averageOfUser[i] = sum / count;
        }
	}

	private double findAverage(int[] activeUserTable) {
		// TODO Auto-generated method stub
		double sum = 0, count = 0;
        for (int i = 0; i < activeUserTable.length; i++) {
            if (activeUserTable[i] >= 1) {
                sum += activeUserTable[i];
                count++;
            }
        }
        return sum / count;
	}

	private int getNextUser(int[] activeUserTable, Scanner s1) {
		// TODO Auto-generated method stub
		int name = activeUserName;

        while (name == activeUserName) {

            try {
            	activeUserTable[s1.nextInt() - 1] = s1.nextInt();
                if (s1.hasNext()) {
                    name = s1.nextInt() - 1;

                }
            } catch (Exception e) {
                break;
            }

        }
        return name;
	}

	public static void main(String args[]) throws FileNotFoundException, IOException {
		RecommendationPearson_old r = new RecommendationPearson_old();
        r.predictBasedOnPearsonSimilarity("test10.txt", "result10My.txt");
        
    }

	
}
