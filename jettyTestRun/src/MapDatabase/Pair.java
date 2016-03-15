package MapDatabase;
/**
 * @author Sandeep
 * 
 * Template Class to Store a pair of values
 *
 */
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;


public class Pair<L,R> implements Serializable{
    /**
	 * Template Class to Store a pair of values
	 */
	private static final long serialVersionUID = 2088625713495844100L;
	private L l;
    private R r;
    public Pair(L l, R r){
        this.l = l;
        this.r = r;
    }
    public Pair() {
		// TODO Auto-generated constructor stub
    	this.l = null;
        this.r = null;
	}
	public L getL(){ return l; }
    public R getR(){ return r; }
    public void setL(L l){ this.l = l; }
    public void setR(R r){ this.r = r; }
    


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((l == null) ? 0 : l.hashCode());
		result = prime * result + ((r == null) ? 0 : r.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair<?, ?> other = (Pair<?, ?>) obj;
		if (l == null) {
			if (other.l != null)
				return false;
		} else if (!l.equals(other.l))
			return false;
		if (r == null) {
			if (other.r != null)
				return false;
		} else if (!r.equals(other.r))
			return false;
		return true;
	}
	public String toString(){
		return Objects.toString(this.l)+"-"+Objects.toString(this.r);
	}   
	
    public static Comparator<Pair<Integer,Double> > getAttribute2Comparator() {
        return new Comparator<Pair<Integer,Double> >() {

			@Override
			public int compare(Pair<Integer, Double> o1, Pair<Integer, Double> o2) {
				// TODO Auto-generated method stub
				return o2.getR().compareTo(o1.getR());
			}
            // compare using attribute 1
        };
    }
    
/*    @Override 
	public boolean equals(Object node) {
		if(node == null)
			return false;
		Pair<L, R> node_x = (Pair<L, R>) node;
        return ((node_x.l == this.l) && (node_x.r == this.r));
    }
	
	    
	@Override 
	public int hashCode() { 
		return Objects.hash(l, r);
	  }*/
    
/* ANONYMOUS CLASS IMPLEMENTATION
 * ------------------------------   
 * Collections.sort(yourList, new Comparator<YourClass>(){
        public int compare(YourClass one, YourClass two) {
            // compare using whichever properties of ListType you need
        }
    });
    Collections.sort(yourList, new CustomComparator<YourClass>());*/
}
