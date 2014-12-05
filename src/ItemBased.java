import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Scanner;

public class ItemBased {

	private int simNum = 30;
	private MovieRecommUserData[] movies;
	private int UserAc;
	private double[] avgOfAllUsers;
	private double[] sim;
	private double[][] ratings;
	private int[] topSimUsers;
	private Hashtable<Integer, Integer> toWrite;
	private double[] currentUser;
	public int noOfUsers = 200;
	private int noOfMovies = 1000;

	public ItemBased() throws FileNotFoundException {
		sim = new double[noOfMovies];
		ratings = new double[noOfUsers][noOfMovies];
		topSimUsers = new int[simNum];
		Scanner s = new Scanner(new BufferedReader(new FileReader(new File(
				"train.txt"))));
		for (int i = 0; i < noOfUsers; i++) {
			for (int j = 0; j < noOfMovies; j++) {
				ratings[i][j] = s.nextInt();
			}
		}
		s.close();
	}

	
	


	private double getAvg(double[] currentUser) {
		double sum = 0, count = 0;
		for (int i = 0; i < currentUser.length; i++) {
			if (currentUser[i] >= 1) {
				sum += currentUser[i];
				count++;
			}
		}
		return sum / count;
	}
	public int getNext(double[] currentUser, Scanner s) {
		int name = UserAc;
		while (name == UserAc) {
			try {
				currentUser[s.nextInt() - 1] = s.nextInt();
				if (s.hasNext()) {
					name = s.nextInt() - 1;
				}
			} catch (Exception e) {
				break;
			}
		}
		return name;
	}
	private int refactored(double d) {
		if (d > 5) {
			return 5;
		} else if (d <= 0) {
			int temp = (int) (d + 5);
			if (temp > 5)
				return 5;
			else if (temp == 0)
				return 1;
			else
				return temp;
		} else {
			return (int) d;
		}
	}

	private void getAvgOfUsers() {
		avgOfAllUsers = new double[noOfUsers];
		for (int i = 0; i < noOfUsers; i++) {
			double sum = 0, count = 0;
			for (int j = 0; j < noOfMovies; j++) {
				if (ratings[i][j] != 0) {
					sum += ratings[i][j];
					count++;
				}
			}
			avgOfAllUsers[i] = sum / count;
		}
	}
	
	

	private void sortUsers() {

		movies = new MovieRecommUserData[noOfMovies];

		for (int i = 0; i < noOfMovies; i++) {

			movies[i] = new MovieRecommUserData(Math.abs(sim[i]), i);
		}
		Arrays.sort(movies, MovieRecommUserData.BySim);

	}

	private void findSimilarMoviesTo(int predictForMovie) { 

		for (int movie = 0; movie < noOfMovies; movie++) {
			if (movie == predictForMovie) {
				continue;
			}
			double XY = 0;
			double X = 1;
			double Y = 1;

			for (int user = 0; user < noOfUsers; user++) {

				if (ratings[user][movie] > 0
						&& ratings[user][predictForMovie] > 0) { 

					XY += (ratings[user][movie] - avgOfAllUsers[user])
							* (ratings[user][predictForMovie] - avgOfAllUsers[user]);
					X += Math.pow(
							(ratings[user][movie] - avgOfAllUsers[user]), 2);
					Y += Math
							.pow((ratings[user][predictForMovie] - avgOfAllUsers[user]),
									2);
				}
			}

			sim[movie] = XY / (Math.sqrt(X) * Math.sqrt(Y));

		}
		sortUsers();
	}
	
	private void findTopSimUsers() {
		int j = 0;

		for (int i = noOfMovies - 1; j < simNum && i >= 0; i--) {
			int movie = movies[i].id;

			if (currentUser[movie] > 0) {
				topSimUsers[j] = movies[i].id;
				j++;
			}

		}
		for (; j < simNum; j++) {
			topSimUsers[j] = -1;
		}
	}

	private void writeToFile(int currUSerName, double[] currUser, PrintStream  out) {
		for (int movie = 0; movie < currUser.length; movie++) {
			if (currUser[movie] != -1 && toWrite.get(movie) != null) {

				out.println((currUSerName + 1) + " " + (movie + 1) + " "
						+ (int) (Math.round(currUser[movie]))); 
			}
		}

	}

	private double getRating() {
		double numerator = 0;
		double denominator = 0;
		for (int i = 0; i < simNum && topSimUsers[i] != -1; i++) {
			int movie = topSimUsers[i];

			if (currentUser[movie] > 0) {

				numerator += sim[movie] * (currentUser[movie]);

				denominator += Math.abs(sim[movie]);
			}

		}
		return (numerator / denominator);
	}
	
	public void getRatingItemBased(String dataset, String toWriteFile)
			throws FileNotFoundException, IOException {
		getAvgOfUsers();
		Scanner s = new Scanner(new BufferedReader(new FileReader(new File(
				dataset))));
		PrintStream out = new PrintStream(new File(toWriteFile));
		UserAc = s.nextInt() - 1; 
		while (s.hasNext()) {
			
			currentUser = new double[noOfMovies];
			Arrays.fill(currentUser, -1);
			toWrite = new Hashtable<Integer, Integer>();
			int next = getNext(currentUser, s);
			for (int movie = 0; movie < currentUser.length; movie++) {
				if (currentUser[movie] == 0) {				
					getAvg(currentUser);
					findSimilarMoviesTo(movie);
					toWrite.put(movie, 1);
					findTopSimUsers();
					double refectored = refactored(Math.round(getRating()));
					currentUser[movie] = refectored;													
				}
					
			}
			
			writeToFile(UserAc, currentUser, out);
			UserAc = next;
		}

		out.flush();
		out.close();
	}

	public static void main(String args[]) throws FileNotFoundException,
			IOException {
		ItemBased im = new ItemBased(); 
		int num = 20;
		im.getRatingItemBased("test"+num+".txt", "ItemBasedResult"+num+".txt");

	}
}
