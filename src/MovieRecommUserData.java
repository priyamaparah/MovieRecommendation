


import java.util.Comparator;




public class MovieRecommUserData {
public Double sim;
public Double absSim;
public int id;

public MovieRecommUserData(double similarity,int name){
    this.sim=similarity;
    this.id=name;
}

public MovieRecommUserData(Double sim, Double absSim, int id) {
	this.sim = sim;
	this.absSim = absSim;
	this.id = id;
}

public static Comparator<MovieRecommUserData> BySim=new BySim();

public static Comparator<MovieRecommUserData> MySim=new MySim();
        
 private static class BySim implements Comparator<MovieRecommUserData>{

        public int compare(MovieRecommUserData o1, MovieRecommUserData o2) {
            return o1.sim.compareTo(o2.sim);
            
        }
    
} 
 
 private static class MySim implements Comparator<MovieRecommUserData>{

     public int compare(MovieRecommUserData o1, MovieRecommUserData o2) {
    	 
         return o1.absSim.compareTo(o2.absSim);
         
     }
 
}
    
    
    
    
}
