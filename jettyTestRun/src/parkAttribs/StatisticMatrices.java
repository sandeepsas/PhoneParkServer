package parkAttribs;

public class StatisticMatrices {
	
	
	/*Statistical Matrices*/
	private double [][] probability;
	private double [][] avail;
	
	public StatisticMatrices(int n){
		
		/*Initialize the probability matrix with 0.5*/
		probability = new double[n][n];
		
		/*Initialize availability for all blocks a random value between 0 and 20*/
		//@TODO - This need to be changed as per discussion on 07 Mar 2016
		avail = new double[n][n];
		
	}
	
/*STATISTICAL DEPENDENCY FUNCTIONS TO CALCULATE PROBABILITY FROM EXPECTANCY*/
	
	public static double phi(double x) {
		return Math.exp(-x * x / 2) / Math.sqrt(2 * Math.PI);
	}

	// return phi(x, mu, signma) = Gaussian pdf with mean mu and stddev sigma
	public static double phi(double x, double mu, double sigma) {
		return phi((x - mu) / sigma) / sigma;
	}

	// return Phi(z) = standard Gaussian cdf using Taylor approximation
	public static double Phi(double z) {
		if (z < -8.0)
			return 0.0;
		if (z > 8.0)
			return 1.0;
		double sum = 0.0, term = z;
		for (int i = 3; sum + term != sum; i += 2) {
			sum = sum + term;
			term = term * z * z / i;
		}
		return 0.5 + sum * phi(z);
	}

	// return Phi(z, mu, sigma) = Gaussian cdf with mean mu and stddev sigma
	public static double Phi(double z, double mu, double sigma) {
		return Phi((z - mu) / sigma);
	}

	public double[][] getProbabilityMatrix() {
		return probability;
	}

	public void setProbabilityMatrix(double[][] probability) {
		this.probability = probability;
	}

	public double[][] getAvailMatrix() {
		return avail;
	}

	public void setAvailMatrix(double[][] avail) {
		this.avail = avail;
	}

}
