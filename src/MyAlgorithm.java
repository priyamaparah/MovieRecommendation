//Got Best 20 By rating as correct and mul = 0.75 no pr changed
//Got Best 5  rating not proper assigned, pr adjusted to not proper rating , !=5 condition
//Got Best 10 rating proper assigned , pr changed
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class MyAlgorithm {

	private double g_val;
	private MovieRecommUserData[] userPearson;
	private MovieRecommUserData[] userCos;
	private int currentUser;
	private int simNum = 33;
	private double[] simPearson;
	private double[] simCos;
	private double[][] rating;
	private int[] SimUsersPearsonTop;
	private int[] SimUsersCosTop;
	private double[] acgOfAllUsers;
	public int noOfUsers = 200;
	private int noOfMovies = 1000;
	private double userAvgAc;
	private int dataSet;
	private ArrayList<Integer> movieToWrite;
	private int[] currentUserRatings;
	private int[] currentUserMovies;
	private int maxRated;
	private int maxCount;

	public MyAlgorithm() throws FileNotFoundException {

		simPearson = new double[noOfUsers];
		simCos = new double[noOfUsers];
		rating = new double[noOfUsers][noOfMovies];
		SimUsersPearsonTop = new int[simNum];
		SimUsersCosTop = new int[simNum];
		Scanner s1 = new Scanner(new BufferedReader(new FileReader(new File(
				"train.txt"))));
		for (int i = 0; i < noOfUsers; i++) {
			for (int j = 0; j < noOfMovies; j++) {
				rating[i][j] = s1.nextInt();
			}
		}
	}

	public void pearsonSimprediction(String testData, String resFile, int data)
			throws FileNotFoundException, IOException {
		dataSet = data;
		currentUserRatings = new int[dataSet];
		currentUserMovies = new int[dataSet];
		g_val = getValueOf_g();
		Scanner s = new Scanner(new BufferedReader(new FileReader(new File(
				testData))));
		PrintStream fout = new PrintStream(new File(resFile));
		currentUser = s.nextInt() - 1;
		while (s.hasNext()) {
			movieToWrite = new ArrayList<Integer>();
			int[] currUser = new int[noOfMovies];
			Arrays.fill(currUser, -1);
			int nextUser = getNextUserId(currUser, s);
			findIfAllMoviesHasSameRatings_Old(currUser);
			double mul = 0.75;
			if (dataSet != 5) {
				maxRated = currentUserRatings[0];
			}
			if(dataSet == 20) {
				mul = 0.6;
			}
			if (maxCount >= Math.round(mul * dataSet)) {
				for (int movie = 0; movie < 1000; movie++) {
					if (currUser[movie] == 0) {
						movieToWrite.add(movie);
						currUser[movie] = maxRated;
					}
				}
			} else {
				userAvgAc = findAverage(currUser);
				for (int movie = 0; movie < 1000; movie++) {
					if (currUser[movie] == 0) {
						getAvgOfAllBasedOnMovie(movie);
						applySmoothing(movie);
						getCosineSim(currUser);
						getPearson(currUser);
						getTopSimiliarCosUsers(movie);
						getTopSimiliarPearsonUsers(movie);
						movieToWrite.add(movie);
						double pr1 = predictPearson(movie);
						double pr2 = predictCost(movie);
						double pr;
						if(pr1 == 0){
							pr = pr2;
						}
						else if(pr2 == 0) {
							pr = pr1;
						}
						else {
							pr = (pr1 + pr2)/2.0;
						}
						currUser[movie] = refactor((int) Math
								.round(pr));
					}
				}
			}
			toWriteOut(currentUser, currUser, fout);
			currentUser = nextUser;
			if (currentUser == 203) {
				System.out.println("   ");
			}
		}

		fout.flush();
		fout.close();
	}

	

	private double predictCost(int movie) {
		// TODO Auto-generated method stub
		int j = 0;
		double numerator = 0;
		double denominator = 0;
		for (int i = 0; i < simNum && SimUsersCosTop[i] != -1; i++) {
			int user = SimUsersCosTop[i];

			numerator += simCos[user] * rating[user][movie];
			denominator += simCos[user];
		}

		return numerator / denominator;
	}

	private void applySmoothing(int movie) {
		// TODO Auto-generated method stub
		final int beta = 1;
		for (int i = 0; i < noOfUsers; i++) {
			if (acgOfAllUsers[i] != 0) {
				double nU = 0;
				double value1 = 0.0;
				double value2 = 0.0;
				for (int j = 0; j < noOfMovies; j++) {
					if (rating[i][j] != 0 && j != movie) {
						nU++;
					}
				}

				value1 = (nU / (beta + nU)) * acgOfAllUsers[i];
				value2 = (beta / (beta + nU)) * g_val;
				acgOfAllUsers[i] = value1 + value2;
			}
		}
	}

	private double getValueOf_g() {
		double sum = 0;
		double count = 0;
		for (int i = 0; i < noOfUsers; i++) {
			for (int j = 0; j < noOfMovies; j++) {
				if (rating[i][j] != 0) {
					sum = sum + rating[i][j];
					count++;
				}
			}
		}
		return sum / count;

	}

	

	private void findIfAllMoviesHasSameRatings(int[] currUser) {
		// TODO Auto-generated method stub
		int count = 1;
		maxRated = currentUserRatings[0];
		maxCount = 1;
		int rating = currentUserRatings[0];
		int prevCount = 0;
		for (int movie = 0; movie < dataSet; movie++) {
			if (prevCount == 1 && count > 1) {
				maxCount = count;
				maxRated = rating;
			}

			rating = currentUserRatings[movie];
			prevCount = count;
			if (count > maxCount) {
				maxCount = count;

			}
			count = 1;
			for (int mv2 = movie + 1; mv2 < dataSet; mv2++) {
				if (rating == currentUserRatings[mv2]) {
					count++;
				}
			}

		}
	}

	private void findIfAllMoviesHasSameRatings_Old(int[] currrUser) {
		// TODO Auto-generated method stub
		int count = 1;
		maxRated = currentUserRatings[0];
		maxCount = 1;
		int prevCount = 0;
		for (int movie = 0; movie < dataSet; movie++) {
			int rating = currentUserRatings[movie];
			if (prevCount == 1 && count > 1) {
				maxCount = count;
				maxRated = rating;
			}
			prevCount = count;
			if (count > maxCount) {
				maxCount = count;

			}
			count = 1;
			for (int mv2 = movie + 1; mv2 < dataSet; mv2++) {
				if (rating == currentUserRatings[mv2]) {
					count++;
				}
			}
		}
	}

	public int getNextUserId(int[] currUser, Scanner s) {
		int id = currentUser;
		int i = 0;
		while (id == currentUser) {
			try {
				int movie = s.nextInt() - 1;
				int rating = s.nextInt();
				if (rating > 0) {
					currentUserRatings[i] = rating;
					currentUserMovies[i] = movie;
					i++;
				}
				currUser[movie] = rating;
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
			// return refactor((int) Math.round(userAvgAc));
			return refactor(maxRated);
		} else if (round >= 1 && round <= 5) {
			return round;
		} else {
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

	private void getTopSimiliarPearsonUsers(int movie) {
		int j = 0;

		for (int i = noOfUsers - 1; j < simNum && i >= 0; i--) {
			int user = userPearson[i].id;

			if (rating[user][movie] != 0) {
				SimUsersPearsonTop[j] = userPearson[i].id;
				j++;
			}
		}
		for (; j < simNum; j++) {
			SimUsersPearsonTop[j] = -1;
		}
	}
	
	private void getTopSimiliarCosUsers(int movie) {
		int j = 0;

		for (int i = noOfUsers - 1; j < simNum && i >= 0; i--) {
			int user = userCos[i].id;

			if (rating[user][movie] != 0) {
				SimUsersCosTop[j] = userCos[i].id;
				j++;
			}
		}
		for (; j < simNum; j++) {
			SimUsersCosTop[j] = -1;
		}
	}
	
	private void sortAllUserCos(double sim[]) {
		userCos = new MovieRecommUserData[noOfUsers];

		for (int i = 0; i < noOfUsers; i++) {
			userCos[i] = new MovieRecommUserData(sim[i], i);
		}
		Arrays.sort(userCos, MovieRecommUserData.BySim);
	}
	
	private void sortAllUserPearson(double sim[]) {
		userPearson = new MovieRecommUserData[noOfUsers];

		for (int i = 0; i < noOfUsers; i++) {
			userPearson[i] = new MovieRecommUserData(sim[i], i);
		}
		Arrays.sort(userPearson, MovieRecommUserData.BySim);
	}

	/*
	 * private void getCosineSim(int[] userAc) { double cosine = 0; double
	 * pearson = 0; int count; for (int user = 0; user < noOfUsers; user++) {
	 * count = 0; for (int i = 0; i < dataSet; i++) { int movie =
	 * currentUserMovies[i]; if(rating[user][movie] != 0) { count++; } }
	 * 
	 * pearson = getPearson(userAc,user); cosine = cosineSimilarity(user,
	 * userAc); if (count > 0) { if(pearson == 0) { simPearson[user] = cosine; }
	 * else if(cosine ==0 ){ simPearson[user] = pearson; } else {
	 * simPearson[user] = (cosine + pearson)/2.0; } } else { simPearson[user] =
	 * pearson; } } sortAllUser(); }
	 */

	private void getPearson(int[] userAc) {
		for (int user = 0; user < noOfUsers; user++) {
			double XY = 0;
			double X = 1;
			double Y = 1;

			for (int movie = 0; movie < noOfMovies; movie++) {
				if (userAc[movie] > 0 && rating[user][movie] > 0) {
					XY += (userAc[movie] - userAvgAc)
							* (rating[user][movie] - acgOfAllUsers[user]);
					X += Math.pow((userAc[movie] - userAvgAc), 2);
					Y += Math.pow((rating[user][movie] - acgOfAllUsers[user]),
							2);
				}
			}
			simPearson[user] = XY / (Math.sqrt(X) * Math.sqrt(Y));
			if (simPearson[user] == 0
					&& (isAvginCurr(userAc) || isAvgInUser(user))) {
				simPearson[user] = cosineSimilarity(user, userAc);
			}
		}
		sortAllUserPearson(simPearson);
	}

	private double caseAmpf(double sim) {
		double r = 1.5; // last best 1.05
		double absRating = Math.abs(sim);
		sim = sim * Math.pow(absRating, r - 1);
		return sim;
	}

	private double cosineSimilarity(int user, int[] currUser) {
			double XY = 0;
			double X = 1;
			double Y = 1;

			for (int movie = 0; movie < noOfMovies; movie++) {
				if (currUser[movie] != 0 && rating[user][movie] != 0
						&& currUser[movie] != -1) {
					XY += currUser[movie] * rating[user][movie];
					X += Math.pow(currUser[movie], 2);
					Y += Math.pow(rating[user][movie], 2);
				}
			}
			return XY / (Math.sqrt(X) * Math.sqrt(Y));
	}
	
	private void getCosineSim(int[] currUser) {
		// TODO Auto-generated method stub
		for(int user = 0; user < noOfUsers; user++) {
			double XY = 0;
			double X = 1;
			double Y = 1;

			for (int movie = 0; movie < noOfMovies; movie++) {
				if (currUser[movie] != 0 && rating[user][movie] != 0
						&& currUser[movie] != -1) {
					XY += currUser[movie] * rating[user][movie];
					X += Math.pow(currUser[movie], 2);
					Y += Math.pow(rating[user][movie], 2);
				}
			}
			simCos[user] =  XY / (Math.sqrt(X) * Math.sqrt(Y));
		}
		sortAllUserCos(simCos);
	}

	private boolean isAvgInUser(int user) {

		for (int movie = 0; movie < noOfMovies; movie++) {
			if (rating[user][movie] == acgOfAllUsers[user]) {
				continue;
			} else if (rating[user][movie] > 0) {
				return false;
			}
		}
		return true;
	}

	private boolean isAvginCurr(int[] currUser) {

		for (int movie = 0; movie < noOfMovies; movie++) {
			if (currUser[movie] == userAvgAc) {
				continue;
			} else if (currUser[movie] > 0) {
				return false;
			}
		}

		return true;
	}

	private void toWriteOut(int currUser, int[] curr, PrintStream fout) {
		for (int movie = 0; movie < curr.length; movie++) {
			if (curr[movie] != -1 && movieToWrite.contains(movie)) {

				fout.println((currUser + 1) + " " + (movie + 1) + " "
						+ (curr[movie])); // need to add 1
			}
		}

	}

	private double predictPearson(int movie) {
		int j = 0;
		double numerator = 0;
		double denominator = 0;
		for (int i = 0; i < simNum && SimUsersPearsonTop[i] != -1; i++) {
			int user = SimUsersPearsonTop[i];

			numerator += simPearson[user]
					* (rating[user][movie] - acgOfAllUsers[user]);
			denominator += Math.abs(simPearson[user]);

		}
		if (numerator == 0 && denominator == 0) {
			System.out.println();
		}
		double pr = userAvgAc + (numerator / denominator);
		return pr;

	}

	private double findAverage(int[] currUser) {
		double sum = 0, count = 0;
		for (int i = 0; i < currUser.length; i++) {
			if (currUser[i] >= 1) {
				sum += currUser[i];
				count++;
			}
		}
		return sum / count;
	}

	public static void main(String args[]) throws FileNotFoundException,
			IOException {
		MyAlgorithm my = new MyAlgorithm();
		int num = 20;
		my.pearsonSimprediction("test" + num + ".txt", "myresult" + num
				+ ".txt", num);

	}
}
