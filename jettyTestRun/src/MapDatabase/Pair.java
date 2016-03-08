package MapDatabase;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;


public class Pair<L,R> implements Serializable{
    /**
	 * 
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
		return Objects.hash(l, r);
	  }
	@Override 
	public boolean equals(Object node) {
		if(node == null)
			return false;
		Pair<L, R> node_x = (Pair<L, R>) node;
        return ((node_x.l == this.l) && (node_x.r == this.r));
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
    
/* ANONYMOUS CLASS IMPLEMENTATION
 * ------------------------------   
 * Collections.sort(yourList, new Comparator<YourClass>(){
        public int compare(YourClass one, YourClass two) {
            // compare using whichever properties of ListType you need
        }
    });
    Collections.sort(yourList, new CustomComparator<YourClass>());*/
}
